package com.exam.dto;

import lombok.Data;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 考试创建请求DTO
 * 
 * @author system
 * @since 2024-10-16
 */
@Data
public class ExamCreateRequest {

    /**
     * 考试名称
     */
    @NotBlank(message = "考试名称不能为空")
    @Size(max = 200, message = "考试名称长度不能超过200个字符")
    private String examName;

    /**
     * 考试类型
     */
    @NotBlank(message = "考试类型不能为空")
    private String examType;

    /**
     * 考试日期
     */
    @NotNull(message = "考试日期不能为空")
    private LocalDate examDate;

    /**
     * 考试时间段
     */
    @NotBlank(message = "考试时间段不能为空")
    private String examTime;

    /**
     * 报名开始时间
     */
    @NotNull(message = "报名开始时间不能为空")
    private LocalDateTime registrationStart;

    /**
     * 报名结束时间
     */
    @NotNull(message = "报名结束时间不能为空")
    private LocalDateTime registrationEnd;

    /**
     * 报名费用
     */
    @NotNull(message = "报名费用不能为空")
    @DecimalMin(value = "0.0", message = "报名费用不能为负数")
    private BigDecimal fee;

    /**
     * 考试简介
     */
    private String description;

    /**
     * 报名须知
     */
    private String notice;

    /**
     * 总报名名额
     */
    @Min(value = 1, message = "总报名名额必须大于0")
    private Integer totalQuota;
}
