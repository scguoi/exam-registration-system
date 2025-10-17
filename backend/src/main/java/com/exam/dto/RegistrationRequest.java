package com.exam.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * 报名请求DTO
 *
 * @author system
 * @since 2024-10-17
 */
@Data
public class RegistrationRequest {

    /**
     * 考试ID
     */
    @NotNull(message = "考试ID不能为空")
    private Long examId;

    /**
     * 考点ID
     */
    @NotNull(message = "考点ID不能为空")
    private Long examSiteId;

    /**
     * 身份证号
     */
    @NotBlank(message = "身份证号不能为空")
    @Pattern(regexp = "^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[0-9Xx]$",
            message = "身份证号格式不正确")
    private String idCard;

    /**
     * 手机号
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 报考科目
     */
    private String subject;

    /**
     * 证明材料URL（JSON格式）
     */
    private String materials;

    /**
     * 用户ID（从JWT Token中获取，不需要前端传递）
     */
    private Long userId;
}
