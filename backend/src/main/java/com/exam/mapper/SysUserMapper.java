package com.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exam.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户表Mapper接口
 * 
 * @author system
 * @since 2024-10-16
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 根据用户名查询用户
     * 
     * @param username 用户名
     * @return 用户信息
     */
    SysUser selectByUsername(@Param("username") String username);

    /**
     * 更新用户登录信息
     * 
     * @param userId 用户ID
     * @param loginIp 登录IP
     * @return 更新行数
     */
    int updateLoginInfo(@Param("userId") Long userId, @Param("loginIp") String loginIp);

    /**
     * 增加登录失败次数
     * 
     * @param userId 用户ID
     * @return 更新行数
     */
    int incrementLoginFailCount(@Param("userId") Long userId);

    /**
     * 重置登录失败次数
     * 
     * @param userId 用户ID
     * @return 更新行数
     */
    int resetLoginFailCount(@Param("userId") Long userId);
}
