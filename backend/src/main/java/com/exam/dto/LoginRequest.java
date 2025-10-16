package com.exam.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 登录请求DTO
 * 
 * @author system
 * @since 2024-10-16
 */
@Data
public class LoginRequest {

    /**
     * 用户名(手机号)
     */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;
}
