package com.exam.controller;

import com.exam.common.Result;
import com.exam.dto.LoginRequest;
import com.exam.dto.RegisterRequest;
import com.exam.dto.UserUpdateRequest;
import com.exam.entity.SysUser;
import com.exam.security.CustomUserDetails;
import com.exam.service.SysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 用户控制器
 * 
 * @author system
 * @since 2024-10-16
 */
@Slf4j
@RestController
@RequestMapping("/v1/users")
public class UserController {

    @Autowired
    private SysUserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<SysUser> register(@Valid @RequestBody RegisterRequest request) {
        SysUser user = userService.register(request);
        // 清除敏感信息
        user.setPassword(null);
        user.setIdCard(null);
        user.setPhone(null);
        return Result.success("注册成功", user);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<String> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String token = userService.login(request);
        
        // 更新登录信息
        String loginIp = getClientIp(httpRequest);
        SysUser user = userService.getByUsername(request.getUsername());
        userService.updateLoginInfo(user.getId(), loginIp);
        
        return Result.success("登录成功", token);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public Result<SysUser> getUserInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {
        SysUser user = userService.getById(userDetails.getUserId());
        if (user != null) {
            // 清除敏感信息
            user.setPassword(null);
        }
        return Result.success(user);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/info")
    public Result<Void> updateUserInfo(@AuthenticationPrincipal CustomUserDetails userDetails,
                                      @Valid @RequestBody UserUpdateRequest request) {
        boolean success = userService.updateUserInfo(userDetails.getUserId(), request);
        return success ? Result.success() : Result.error("更新失败");
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    public Result<Void> changePassword(@AuthenticationPrincipal CustomUserDetails userDetails,
                                      @RequestParam String oldPassword,
                                      @RequestParam String newPassword) {
        boolean success = userService.changePassword(userDetails.getUserId(), oldPassword, newPassword);
        return success ? Result.success() : Result.error("密码修改失败");
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0];
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}
