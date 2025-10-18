package com.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.common.Result;
import com.exam.entity.Exam;
import com.exam.entity.PaymentOrder;
import com.exam.entity.Registration;
import com.exam.mapper.ExamMapper;
import com.exam.mapper.PaymentOrderMapper;
import com.exam.mapper.RegistrationMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

/**
 * 支付订单服务
 *
 * @author system
 * @since 2024-10-16
 */
@Slf4j
@Service
public class PaymentOrderService {

    @Autowired
    private PaymentOrderMapper paymentOrderMapper;

    @Autowired
    private RegistrationMapper registrationMapper;

    @Autowired
    private ExamMapper examMapper;

    /**
     * 创建支付订单（报名审核通过后自动调用）
     *
     * @param registrationId 报名ID
     * @return Result
     */
    @Transactional(rollbackFor = Exception.class)
    public Result createPaymentOrder(Long registrationId) {
        try {
            // 1. 查询报名记录
            Registration registration = registrationMapper.selectById(registrationId);
            if (registration == null) {
                return Result.error("报名记录不存在");
            }

            // 2. 检查审核状态
            if (registration.getAuditStatus() != 2) {
                return Result.error("报名未审核通过，无法创建支付订单");
            }

            // 3. 检查是否已存在支付订单
            PaymentOrder existOrder = paymentOrderMapper.selectByRegistrationId(registrationId);
            if (existOrder != null) {
                log.warn("报名ID {} 已存在支付订单，订单号：{}", registrationId, existOrder.getOrderNo());
                return Result.success(existOrder);
            }

            // 4. 查询考试信息获取报名费
            Exam exam = examMapper.selectById(registration.getExamId());
            if (exam == null) {
                return Result.error("考试信息不存在");
            }

            BigDecimal fee = exam.getFee();
            if (fee == null || fee.compareTo(BigDecimal.ZERO) <= 0) {
                return Result.error("考试报名费未设置");
            }

            // 5. 创建支付订单
            PaymentOrder order = new PaymentOrder();
            order.setOrderNo(generateOrderNo());
            order.setRegistrationId(registrationId);
            order.setUserId(registration.getUserId());
            order.setExamId(registration.getExamId());
            order.setAmount(fee);
            order.setStatus(1); // 1-待支付
            order.setExpireTime(LocalDateTime.now().plusMinutes(30)); // 30分钟后过期

            int result = paymentOrderMapper.insert(order);
            if (result > 0) {
                log.info("创建支付订单成功，订单号：{}，报名ID：{}", order.getOrderNo(), registrationId);
                return Result.success(order);
            } else {
                return Result.error("创建支付订单失败");
            }

        } catch (Exception e) {
            log.error("创建支付订单异常，报名ID：{}", registrationId, e);
            throw new RuntimeException("创建支付订单失败：" + e.getMessage());
        }
    }

    /**
     * 执行支付（Mock支付，点击即支付成功）
     *
     * @param orderNo 订单号
     * @param paymentMethod 支付方式（alipay/wechat/mock）
     * @param userId 用户ID
     * @return Result
     */
    @Transactional(rollbackFor = Exception.class)
    public Result executePay(String orderNo, String paymentMethod, Long userId) {
        try {
            // 1. 查询订单
            PaymentOrder order = paymentOrderMapper.selectByOrderNo(orderNo);
            if (order == null) {
                return Result.error("订单不存在");
            }

            // 2. 验证用户权限
            if (!order.getUserId().equals(userId)) {
                return Result.error("无权操作此订单");
            }

            // 3. 检查订单状态
            if (order.getStatus() != 1) {
                String statusText = getStatusText(order.getStatus());
                return Result.error("订单状态不正确，当前状态：" + statusText);
            }

            // 4. 检查订单是否过期
            if (order.getExpireTime() != null && LocalDateTime.now().isAfter(order.getExpireTime())) {
                // 订单已过期，关闭订单
                order.setStatus(3); // 3-已关闭
                paymentOrderMapper.updateById(order);
                return Result.error("订单已过期");
            }

            // 5. Mock支付：直接标记为已支付
            order.setStatus(2); // 2-已支付
            order.setPaymentMethod(paymentMethod == null ? "mock" : paymentMethod);
            order.setPayTime(LocalDateTime.now());
            order.setTransactionId("MOCK_" + System.currentTimeMillis() + "_" + new Random().nextInt(10000));

            int result = paymentOrderMapper.updateById(order);
            if (result > 0) {
                // 6. 更新报名记录的支付状态
                Registration registration = registrationMapper.selectById(order.getRegistrationId());
                if (registration != null) {
                    registration.setPaymentStatus(2); // 2-已缴费
                    registration.setPaymentTime(LocalDateTime.now());
                    // 生成准考证号
                    registration.setAdmissionTicketNo(generateAdmissionTicketNo(registration.getExamId()));
                    registrationMapper.updateById(registration);
                }

                log.info("支付成功，订单号：{}，支付方式：{}", orderNo, paymentMethod);
                return Result.success(order, "支付成功");
            } else {
                return Result.error("支付失败");
            }

        } catch (Exception e) {
            log.error("执行支付异常，订单号：{}", orderNo, e);
            throw new RuntimeException("支付失败：" + e.getMessage());
        }
    }

    /**
     * 根据订单号查询订单详情
     *
     * @param orderNo 订单号
     * @param userId 用户ID（考生端调用时传入，管理员端可为null）
     * @return Result
     */
    public Result getOrderDetail(String orderNo, Long userId) {
        try {
            PaymentOrder order = paymentOrderMapper.selectByOrderNo(orderNo);
            if (order == null) {
                return Result.error("订单不存在");
            }

            // 如果指定了用户ID，验证权限
            if (userId != null && !order.getUserId().equals(userId)) {
                return Result.error("无权查看此订单");
            }

            return Result.success(order);

        } catch (Exception e) {
            log.error("查询订单详情异常，订单号：{}", orderNo, e);
            return Result.error("查询订单详情失败：" + e.getMessage());
        }
    }

    /**
     * 根据报名ID查询订单
     *
     * @param registrationId 报名ID
     * @return Result
     */
    public Result getOrderByRegistrationId(Long registrationId) {
        try {
            PaymentOrder order = paymentOrderMapper.selectByRegistrationId(registrationId);
            if (order == null) {
                return Result.error("订单不存在");
            }
            return Result.success(order);

        } catch (Exception e) {
            log.error("根据报名ID查询订单异常，报名ID：{}", registrationId, e);
            return Result.error("查询订单失败：" + e.getMessage());
        }
    }

    /**
     * 查询用户的订单列表
     *
     * @param userId 用户ID
     * @return Result
     */
    public Result getMyOrders(Long userId) {
        try {
            List<PaymentOrder> orders = paymentOrderMapper.selectByUserId(userId);
            return Result.success(orders);

        } catch (Exception e) {
            log.error("查询用户订单列表异常，用户ID：{}", userId, e);
            return Result.error("查询订单列表失败：" + e.getMessage());
        }
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
    public Result getOrderList(Integer page, Integer size, Long userId, Long examId, Integer status) {
        try {
            Page<PaymentOrder> pageParam = new Page<>(page, size);
            IPage<PaymentOrder> pageResult = paymentOrderMapper.selectOrderPage(pageParam, userId, examId, status, null, null);
            return Result.success(pageResult);

        } catch (Exception e) {
            log.error("分页查询订单列表异常", e);
            return Result.error("查询订单列表失败：" + e.getMessage());
        }
    }

    /**
     * 申请退款（报名取消时调用）
     *
     * @param orderNo 订单号
     * @param refundReason 退款原因
     * @return Result
     */
    @Transactional(rollbackFor = Exception.class)
    public Result applyRefund(String orderNo, String refundReason) {
        try {
            // 1. 查询订单
            PaymentOrder order = paymentOrderMapper.selectByOrderNo(orderNo);
            if (order == null) {
                return Result.error("订单不存在");
            }

            // 2. 检查订单状态（只有已支付的订单才能退款）
            if (order.getStatus() != 2) {
                return Result.error("订单状态不正确，无法退款");
            }

            // 3. Mock退款：直接标记为已退款
            order.setStatus(4); // 4-已退款
            order.setRefundReason(refundReason);
            order.setRefundTime(LocalDateTime.now());

            int result = paymentOrderMapper.updateById(order);
            if (result > 0) {
                // 4. 更新报名记录的支付状态
                Registration registration = registrationMapper.selectById(order.getRegistrationId());
                if (registration != null) {
                    registration.setPaymentStatus(3); // 3-已退费
                    registrationMapper.updateById(registration);
                }

                log.info("退款成功，订单号：{}，退款原因：{}", orderNo, refundReason);
                return Result.success("退款成功");
            } else {
                return Result.error("退款失败");
            }

        } catch (Exception e) {
            log.error("申请退款异常，订单号：{}", orderNo, e);
            throw new RuntimeException("退款失败：" + e.getMessage());
        }
    }

    /**
     * 关闭过期订单（定时任务调用）
     *
     * @return Result
     */
    @Transactional(rollbackFor = Exception.class)
    public Result closeExpiredOrders() {
        try {
            LocalDateTime currentTime = LocalDateTime.now();
            List<PaymentOrder> expiredOrders = paymentOrderMapper.selectExpiredOrders(currentTime);

            int closedCount = 0;
            for (PaymentOrder order : expiredOrders) {
                order.setStatus(3); // 3-已关闭
                int result = paymentOrderMapper.updateById(order);
                if (result > 0) {
                    closedCount++;
                    log.info("关闭过期订单，订单号：{}", order.getOrderNo());
                }
            }

            log.info("关闭过期订单完成，共关闭 {} 个订单", closedCount);
            return Result.success("关闭过期订单完成，共关闭 " + closedCount + " 个订单");

        } catch (Exception e) {
            log.error("关闭过期订单异常", e);
            return Result.error("关闭过期订单失败：" + e.getMessage());
        }
    }

    /**
     * 统计缴费数据
     *
     * @param examId 考试ID（可选）
     * @return Result
     */
    public Result getPaymentStats(Long examId) {
        try {
            // 统计已支付订单金额
            BigDecimal paidAmount = paymentOrderMapper.sumAmountByCondition(examId, 2, null, null);
            if (paidAmount == null) {
                paidAmount = BigDecimal.ZERO;
            }

            // 统计待支付订单金额
            BigDecimal unpaidAmount = paymentOrderMapper.sumAmountByCondition(examId, 1, null, null);
            if (unpaidAmount == null) {
                unpaidAmount = BigDecimal.ZERO;
            }

            // 统计已退款订单金额
            BigDecimal refundedAmount = paymentOrderMapper.sumAmountByCondition(examId, 4, null, null);
            if (refundedAmount == null) {
                refundedAmount = BigDecimal.ZERO;
            }

            // 统计订单数量
            LambdaQueryWrapper<PaymentOrder> wrapper = new LambdaQueryWrapper<>();
            if (examId != null) {
                wrapper.eq(PaymentOrder::getExamId, examId);
            }

            Long totalCount = paymentOrderMapper.selectCount(wrapper);
            Long paidCount = paymentOrderMapper.selectCount(wrapper.eq(PaymentOrder::getStatus, 2));
            Long unpaidCount = paymentOrderMapper.selectCount(wrapper.eq(PaymentOrder::getStatus, 1));
            Long refundedCount = paymentOrderMapper.selectCount(wrapper.eq(PaymentOrder::getStatus, 4));

            // 构造返回结果
            java.util.Map<String, Object> stats = new java.util.HashMap<>();
            stats.put("paidAmount", paidAmount);
            stats.put("unpaidAmount", unpaidAmount);
            stats.put("refundedAmount", refundedAmount);
            stats.put("totalCount", totalCount);
            stats.put("paidCount", paidCount);
            stats.put("unpaidCount", unpaidCount);
            stats.put("refundedCount", refundedCount);

            return Result.success(stats);

        } catch (Exception e) {
            log.error("统计缴费数据异常", e);
            return Result.error("统计缴费数据失败：" + e.getMessage());
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 生成订单号
     * 格式：PO + yyyyMMddHHmmss + 6位随机数
     *
     * @return 订单号
     */
    private String generateOrderNo() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String timestamp = LocalDateTime.now().format(formatter);
        int randomNum = new Random().nextInt(900000) + 100000; // 6位随机数
        return "PO" + timestamp + randomNum;
    }

    /**
     * 生成准考证号
     * 格式：考试ID(补齐4位) + yyyyMMdd + 5位随机数
     *
     * @param examId 考试ID
     * @return 准考证号
     */
    private String generateAdmissionTicketNo(Long examId) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String date = LocalDateTime.now().format(formatter);
        String examIdStr = String.format("%04d", examId); // 考试ID补齐4位
        int randomNum = new Random().nextInt(90000) + 10000; // 5位随机数
        return examIdStr + date + randomNum;
    }

    /**
     * 获取订单状态文本
     *
     * @param status 状态码
     * @return 状态文本
     */
    private String getStatusText(Integer status) {
        switch (status) {
            case 1:
                return "待支付";
            case 2:
                return "已支付";
            case 3:
                return "已关闭";
            case 4:
                return "已退款";
            default:
                return "未知状态";
        }
    }
}
