package com.exam.security;

import com.exam.entity.SysUser;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * 自定义用户详情
 * 
 * @author system
 * @since 2024-10-16
 */
@Data
public class CustomUserDetails implements UserDetails {

    private static final long serialVersionUID = 1L;

    private SysUser user;

    public CustomUserDetails(SysUser user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 根据用户角色返回权限
        String role = "ROLE_" + user.getRole().toUpperCase();
        return Collections.singletonList(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // 检查账号是否被锁定
        if (user.getLockUntil() != null) {
            return user.getLockUntil().isBefore(java.time.LocalDateTime.now());
        }
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.getStatus() == 1;
    }

    /**
     * 获取用户ID
     */
    public Long getUserId() {
        return user.getId();
    }

    /**
     * 获取用户角色
     */
    public String getRole() {
        return user.getRole();
    }
}
