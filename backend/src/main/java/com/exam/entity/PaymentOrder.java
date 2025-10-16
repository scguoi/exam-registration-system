package com.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 缴费订单表实体类
 * 
 * @author system
 * @since 2024-10-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("payment_order")
public class PaymentOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 订单号
     */
    @TableField("order_no")
    private String orderNo;

    /**
     * 报名ID
     */
    @TableField("registration_id")
    private Long registrationId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 考试ID(冗余字段,便于统计)
     */
    @TableField("exam_id")
    private Long examId;

    /**
     * 支付金额(元)
     */
    @TableField("amount")
    private BigDecimal amount;

    /**
     * 支付方式(alipay/wechat/union)
     */
    @TableField("payment_method")
    private String paymentMethod;

    /**
     * 第三方交易流水号
     */
    @TableField("transaction_id")
    private String transactionId;

    /**
     * 订单状态(1-待支付 2-已支付 3-已关闭 4-已退款)
     */
    @TableField("status")
    private Integer status;

    /**
     * 支付时间
     */
    @TableField("pay_time")
    private LocalDateTime payTime;

    /**
     * 订单过期时间(创建后30分钟)
     */
    @TableField("expire_time")
    private LocalDateTime expireTime;

    /**
     * 支付回调数据(JSON)
     */
    @TableField("callback_data")
    private String callbackData;

    /**
     * 退款原因
     */
    @TableField("refund_reason")
    private String refundReason;

    /**
     * 退款时间
     */
    @TableField("refund_time")
    private LocalDateTime refundTime;

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
