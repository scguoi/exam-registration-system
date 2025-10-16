package com.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 考试表实体类
 * 
 * @author system
 * @since 2024-10-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("exam")
public class Exam implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 考试名称
     */
    @TableField("exam_name")
    private String examName;

    /**
     * 考试类型(职业资格/学业水平等)
     */
    @TableField("exam_type")
    private String examType;

    /**
     * 考试日期
     */
    @TableField("exam_date")
    private LocalDate examDate;

    /**
     * 考试时间段(如:9:00-11:00)
     */
    @TableField("exam_time")
    private String examTime;

    /**
     * 报名开始时间
     */
    @TableField("registration_start")
    private LocalDateTime registrationStart;

    /**
     * 报名结束时间
     */
    @TableField("registration_end")
    private LocalDateTime registrationEnd;

    /**
     * 报名费用(元)
     */
    @TableField("fee")
    private BigDecimal fee;

    /**
     * 考试简介
     */
    @TableField("description")
    private String description;

    /**
     * 报名须知
     */
    @TableField("notice")
    private String notice;

    /**
     * 考试简章文件URL
     */
    @TableField("file_url")
    private String fileUrl;

    /**
     * 状态(1-草稿 2-已发布 3-报名中 4-报名结束 5-已结束)
     */
    @TableField("status")
    private Integer status;

    /**
     * 总报名名额(NULL表示不限)
     */
    @TableField("total_quota")
    private Integer totalQuota;

    /**
     * 当前报名人数
     */
    @TableField("current_count")
    private Integer currentCount;

    /**
     * 创建人ID
     */
    @TableField("create_by")
    private Long createBy;

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
