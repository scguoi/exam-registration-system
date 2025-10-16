package com.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户表实体类
 * 
 * @author system
 * @since 2024-10-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_user")
public class SysUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户名(手机号)
     */
    @TableField("username")
    private String username;

    /**
     * 密码(BCrypt加密)
     */
    @TableField("password")
    private String password;

    /**
     * 真实姓名
     */
    @TableField("real_name")
    private String realName;

    /**
     * 身份证号(AES加密)
     */
    @TableField("id_card")
    private String idCard;

    /**
     * 手机号(AES加密)
     */
    @TableField("phone")
    private String phone;

    /**
     * 邮箱
     */
    @TableField("email")
    private String email;

    /**
     * 性别(1-男 2-女)
     */
    @TableField("gender")
    private Integer gender;

    /**
     * 出生日期
     */
    @TableField("birthday")
    private LocalDate birthday;

    /**
     * 头像URL
     */
    @TableField("avatar")
    private String avatar;

    /**
     * 学历(高中/大专/本科等)
     */
    @TableField("education")
    private String education;

    /**
     * 工作单位
     */
    @TableField("work_unit")
    private String workUnit;

    /**
     * 联系地址
     */
    @TableField("address")
    private String address;

    /**
     * 角色(user-考生/admin-管理员)
     */
    @TableField("role")
    private String role;

    /**
     * 状态(1-正常 2-禁用)
     */
    @TableField("status")
    private Integer status;

    /**
     * 最后登录时间
     */
    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录IP
     */
    @TableField("last_login_ip")
    private String lastLoginIp;

    /**
     * 登录失败次数
     */
    @TableField("login_fail_count")
    private Integer loginFailCount;

    /**
     * 锁定截止时间
     */
    @TableField("lock_until")
    private LocalDateTime lockUntil;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
