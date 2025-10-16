package com.exam.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.exam.entity.SysUser;
import com.exam.dto.LoginRequest;
import com.exam.dto.RegisterRequest;
import com.exam.dto.UserUpdateRequest;

/**
 * 用户服务接口
 * 
 * @author system
 * @since 2024-10-16
 */
public interface SysUserService extends IService<SysUser> {

    /**
     * 用户注册
     */
    SysUser register(RegisterRequest request);

    /**
     * 用户登录
     */
    String login(LoginRequest request);

    /**
     * 根据用户名查询用户
     */
    SysUser getByUsername(String username);

    /**
     * 更新用户信息
     */
    boolean updateUserInfo(Long userId, UserUpdateRequest request);

    /**
     * 修改密码
     */
    boolean changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 重置密码
     */
    boolean resetPassword(String username, String newPassword);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 更新登录信息
     */
    void updateLoginInfo(Long userId, String loginIp);

    /**
     * 增加登录失败次数
     */
    void incrementLoginFailCount(Long userId);

    /**
     * 重置登录失败次数
     */
    void resetLoginFailCount(Long userId);

    /**
     * 检查账号是否被锁定
     */
    boolean isAccountLocked(Long userId);
}
