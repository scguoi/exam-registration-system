package com.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 报名表实体类
 * 
 * @author system
 * @since 2024-10-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("registration")
public class Registration implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 考试ID
     */
    @TableField("exam_id")
    private Long examId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 考点ID
     */
    @TableField("exam_site_id")
    private Long examSiteId;

    /**
     * 准考证号
     */
    @TableField("admission_ticket_no")
    private String admissionTicketNo;

    /**
     * 考场号
     */
    @TableField("exam_room")
    private String examRoom;

    /**
     * 座位号
     */
    @TableField("seat_no")
    private String seatNo;

    /**
     * 报考科目
     */
    @TableField("subject")
    private String subject;

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
     * 上传材料(JSON格式存储URL数组)
     */
    @TableField("materials")
    private String materials;

    /**
     * 审核状态(1-待审核 2-审核通过 3-审核驳回)
     */
    @TableField("audit_status")
    private Integer auditStatus;

    /**
     * 审核备注/驳回原因
     */
    @TableField("audit_remark")
    private String auditRemark;

    /**
     * 审核人ID
     */
    @TableField("audit_by")
    private Long auditBy;

    /**
     * 审核时间
     */
    @TableField("audit_time")
    private LocalDateTime auditTime;

    /**
     * 缴费状态(1-未缴费 2-已缴费)
     */
    @TableField("payment_status")
    private Integer paymentStatus;

    /**
     * 缴费时间
     */
    @TableField("payment_time")
    private LocalDateTime paymentTime;

    /**
     * 准考证下载次数
     */
    @TableField("ticket_download_count")
    private Integer ticketDownloadCount;

    /**
     * 首次下载时间
     */
    @TableField("ticket_download_time")
    private LocalDateTime ticketDownloadTime;

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
