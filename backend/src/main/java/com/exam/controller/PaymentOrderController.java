package com.exam.controller;

import com.exam.common.Result;
import com.exam.service.PaymentOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 支付订单控制器
 *
 * @author system
 * @since 2024-10-16
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
public class PaymentOrderController {

    @Autowired
    private PaymentOrderService paymentOrderService;

    /**
     * 创建支付订单（通常由系统自动调用，报名审核通过后）
     * 这里也提供手动调用接口，方便测试
     *
     * @param registrationId 报名ID
     * @param authentication 认证信息
     * @return Result
     */
    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public Result createOrder(@RequestParam @NotNull(message = "报名ID不能为空") Long registrationId,
                             Authentication authentication) {
        log.info("创建支付订单，报名ID：{}", registrationId);
        return paymentOrderService.createPaymentOrder(registrationId);
    }

    /**
     * 执行支付（Mock支付）
     *
     * @param request 支付请求
     * @param authentication 认证信息
     * @return Result
     */
    @PostMapping("/pay")
    @PreAuthorize("hasRole('USER')")
    public Result pay(@RequestBody PayRequest request, Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        log.info("执行支付，订单号：{}，支付方式：{}", request.getOrderNo(), request.getPaymentMethod());
        return paymentOrderService.executePay(request.getOrderNo(), request.getPaymentMethod(), userId);
    }

    /**
     * 根据订单号查询订单详情
     *
     * @param orderNo 订单号
     * @param authentication 认证信息
     * @return Result
     */
    @GetMapping("/{orderNo}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public Result getOrderDetail(@PathVariable @NotBlank(message = "订单号不能为空") String orderNo,
                                 Authentication authentication) {
        log.info("查询订单详情，订单号：{}", orderNo);

        // 如果是普通用户，传入用户ID进行权限验证
        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER"))) {
            Long userId = Long.parseLong(authentication.getName());
            return paymentOrderService.getOrderDetail(orderNo, userId);
        } else {
            // 管理员可以查看所有订单
            return paymentOrderService.getOrderDetail(orderNo, null);
        }
    }

    /**
     * 根据报名ID查询订单
     *
     * @param registrationId 报名ID
     * @return Result
     */
    @GetMapping("/registration/{registrationId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public Result getOrderByRegistrationId(@PathVariable @NotNull(message = "报名ID不能为空") Long registrationId) {
        log.info("根据报名ID查询订单，报名ID：{}", registrationId);
        return paymentOrderService.getOrderByRegistrationId(registrationId);
    }

    /**
     * 查询我的订单列表（考生端）
     *
     * @param authentication 认证信息
     * @return Result
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    public Result getMyOrders(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        log.info("查询我的订单列表，用户ID：{}", userId);
        return paymentOrderService.getMyOrders(userId);
    }

    /**
     * 分页查询订单列表（管理员端）
     *
     * @param page 页码
     * @param size 每页大小
     * @param userId 用户ID（可选）
     * @param examId 考试ID（可选）
     * @param status 订单状态（可选）
     * @return Result
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result getOrderList(@RequestParam(defaultValue = "1") Integer page,
                              @RequestParam(defaultValue = "10") Integer size,
                              @RequestParam(required = false) Long userId,
                              @RequestParam(required = false) Long examId,
                              @RequestParam(required = false) Integer status) {
        log.info("分页查询订单列表，page：{}，size：{}，userId：{}，examId：{}，status：{}",
                page, size, userId, examId, status);
        return paymentOrderService.getOrderList(page, size, userId, examId, status);
    }

    /**
     * 申请退款（考生端或管理员端）
     *
     * @param orderNo 订单号
     * @param request 退款请求
     * @param authentication 认证信息
     * @return Result
     */
    @PutMapping("/{orderNo}/refund")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public Result applyRefund(@PathVariable @NotBlank(message = "订单号不能为空") String orderNo,
                             @RequestBody RefundRequest request,
                             Authentication authentication) {
        log.info("申请退款，订单号：{}，退款原因：{}", orderNo, request.getRefundReason());
        return paymentOrderService.applyRefund(orderNo, request.getRefundReason());
    }

    /**
     * 关闭过期订单（管理员端，可定时任务调用）
     *
     * @return Result
     */
    @PutMapping("/close-expired")
    @PreAuthorize("hasRole('ADMIN')")
    public Result closeExpiredOrders() {
        log.info("关闭过期订单");
        return paymentOrderService.closeExpiredOrders();
    }

    /**
     * 统计缴费数据（管理员端）
     *
     * @param examId 考试ID（可选）
     * @return Result
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public Result getPaymentStats(@RequestParam(required = false) Long examId) {
        log.info("统计缴费数据，examId：{}", examId);
        return paymentOrderService.getPaymentStats(examId);
    }

    // ==================== 内部类：请求对象 ====================

    /**
     * 支付请求
     */
    public static class PayRequest {
        @NotBlank(message = "订单号不能为空")
        private String orderNo;

        private String paymentMethod = "mock"; // 支付方式，默认mock

        public String getOrderNo() {
            return orderNo;
        }

        public void setOrderNo(String orderNo) {
            this.orderNo = orderNo;
        }

        public String getPaymentMethod() {
            return paymentMethod;
        }

        public void setPaymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
        }
    }

    /**
     * 退款请求
     */
    public static class RefundRequest {
        @NotBlank(message = "退款原因不能为空")
        private String refundReason;

        public String getRefundReason() {
            return refundReason;
        }

        public void setRefundReason(String refundReason) {
            this.refundReason = refundReason;
        }
    }
}
