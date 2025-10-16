package com.exam.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.exam.dto.LoginRequest;
import com.exam.dto.RegisterRequest;
import com.exam.dto.UserUpdateRequest;
import com.exam.entity.SysUser;
import com.exam.exception.BusinessException;
import com.exam.mapper.SysUserMapper;
import com.exam.security.JwtTokenProvider;
import com.exam.service.SysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 用户服务实现类
 * 
 * @author system
 * @since 2024-10-16
 */
@Slf4j
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysUser register(RegisterRequest request) {
        // 检查用户名是否已存在
        if (existsByUsername(request.getUsername())) {
            throw new BusinessException("用户名已存在");
        }

        // 检查手机号是否已存在
        if (StrUtil.isNotBlank(request.getPhone()) && existsByPhone(request.getPhone())) {
            throw new BusinessException("手机号已被注册");
        }

        // 检查身份证号是否已存在
        if (StrUtil.isNotBlank(request.getIdCard()) && existsByIdCard(request.getIdCard())) {
            throw new BusinessException("身份证号已被注册");
        }

        // 创建用户对象
        SysUser user = new SysUser();
        BeanUtil.copyProperties(request, user);
        
        // 加密密码
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        
        // 加密敏感信息
        if (StrUtil.isNotBlank(request.getIdCard())) {
            user.setIdCard(encrypt(request.getIdCard()));
        }
        if (StrUtil.isNotBlank(request.getPhone())) {
            user.setPhone(encrypt(request.getPhone()));
        }
        
        // 设置默认值
        user.setRole("user");
        user.setStatus(1);
        user.setLoginFailCount(0);
        
        // 处理生日
        if (StrUtil.isNotBlank(request.getBirthday())) {
            user.setBirthday(LocalDate.parse(request.getBirthday(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }

        // 保存用户
        save(user);
        log.info("用户注册成功: {}", request.getUsername());
        
        return user;
    }

    @Override
    public String login(LoginRequest request) {
        SysUser user = getByUsername(request.getUsername());
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        // 检查账号状态
        if (user.getStatus() != 1) {
            throw new BusinessException("账号已被禁用");
        }

        // 检查账号是否被锁定
        if (isAccountLocked(user.getId())) {
            throw new BusinessException("账号已被锁定，请稍后再试");
        }

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            incrementLoginFailCount(user.getId());
            throw new BusinessException("用户名或密码错误");
        }

        // 登录成功，重置失败次数
        resetLoginFailCount(user.getId());
        
        // 生成JWT Token
        String token = jwtTokenProvider.generateToken(user.getUsername(), user.getRole());
        
        log.info("用户登录成功: {}", request.getUsername());
        return token;
    }

    @Override
    public SysUser getByUsername(String username) {
        return baseMapper.selectByUsername(username);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUserInfo(Long userId, UserUpdateRequest request) {
        SysUser user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 检查手机号是否被其他用户使用
        if (StrUtil.isNotBlank(request.getPhone()) && !request.getPhone().equals(decrypt(user.getPhone()))) {
            if (existsByPhone(request.getPhone())) {
                throw new BusinessException("手机号已被其他用户使用");
            }
        }

        // 检查身份证号是否被其他用户使用
        if (StrUtil.isNotBlank(request.getIdCard()) && !request.getIdCard().equals(decrypt(user.getIdCard()))) {
            if (existsByIdCard(request.getIdCard())) {
                throw new BusinessException("身份证号已被其他用户使用");
            }
        }

        // 更新用户信息
        BeanUtil.copyProperties(request, user, "id", "username", "password", "role", "status");
        
        // 加密敏感信息
        if (StrUtil.isNotBlank(request.getIdCard())) {
            user.setIdCard(encrypt(request.getIdCard()));
        }
        if (StrUtil.isNotBlank(request.getPhone())) {
            user.setPhone(encrypt(request.getPhone()));
        }
        
        // 处理生日
        if (StrUtil.isNotBlank(request.getBirthday())) {
            user.setBirthday(LocalDate.parse(request.getBirthday(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }

        return updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        SysUser user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        return updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean resetPassword(String username, String newPassword) {
        SysUser user = getByUsername(username);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        return updateById(user);
    }

    @Override
    public boolean existsByUsername(String username) {
        return count(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username)) > 0;
    }

    @Override
    public void updateLoginInfo(Long userId, String loginIp) {
        baseMapper.updateLoginInfo(userId, loginIp);
    }

    @Override
    public void incrementLoginFailCount(Long userId) {
        baseMapper.incrementLoginFailCount(userId);
    }

    @Override
    public void resetLoginFailCount(Long userId) {
        baseMapper.resetLoginFailCount(userId);
    }

    @Override
    public boolean isAccountLocked(Long userId) {
        SysUser user = getById(userId);
        if (user == null) {
            return false;
        }
        
        if (user.getLockUntil() != null) {
            return user.getLockUntil().isAfter(LocalDateTime.now());
        }
        
        return false;
    }

    /**
     * 检查手机号是否存在
     */
    private boolean existsByPhone(String phone) {
        String encryptedPhone = encrypt(phone);
        return count(new LambdaQueryWrapper<SysUser>().eq(SysUser::getPhone, encryptedPhone)) > 0;
    }

    /**
     * 检查身份证号是否存在
     */
    private boolean existsByIdCard(String idCard) {
        String encryptedIdCard = encrypt(idCard);
        return count(new LambdaQueryWrapper<SysUser>().eq(SysUser::getIdCard, encryptedIdCard)) > 0;
    }

    /**
     * 加密敏感信息
     */
    private String encrypt(String data) {
        if (StrUtil.isBlank(data)) {
            return data;
        }
        return SecureUtil.aes("exam-registration-system-key".getBytes()).encryptBase64(data);
    }

    /**
     * 解密敏感信息
     */
    private String decrypt(String data) {
        if (StrUtil.isBlank(data)) {
            return data;
        }
        try {
            return SecureUtil.aes("exam-registration-system-key".getBytes()).decryptStr(data);
        } catch (Exception e) {
            log.warn("解密失败: {}", e.getMessage());
            return data;
        }
    }
}
