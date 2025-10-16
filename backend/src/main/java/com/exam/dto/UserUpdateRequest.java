package com.exam.dto;

import lombok.Data;

import javax.validation.constraints.Pattern;

/**
 * 用户信息更新请求DTO
 * 
 * @author system
 * @since 2024-10-16
 */
@Data
public class UserUpdateRequest {

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 身份证号
     */
    @Pattern(regexp = "^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$", 
             message = "身份证号格式不正确")
    private String idCard;

    /**
     * 手机号
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 性别(1-男 2-女)
     */
    private Integer gender;

    /**
     * 出生日期
     */
    private String birthday;

    /**
     * 学历
     */
    private String education;

    /**
     * 工作单位
     */
    private String workUnit;

    /**
     * 联系地址
     */
    private String address;
}
