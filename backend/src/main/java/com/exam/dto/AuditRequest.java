package com.exam.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 审核请求DTO
 *
 * @author system
 * @since 2024-10-17
 */
@Data
public class AuditRequest {

    /**
     * 审核结果(2-通过 3-驳回)
     */
    @NotNull(message = "审核结果不能为空")
    private Integer auditStatus;

    /**
     * 审核备注/驳回原因
     */
    private String auditRemark;
}
