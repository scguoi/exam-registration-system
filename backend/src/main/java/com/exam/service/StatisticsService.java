package com.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.exam.common.Result;
import com.exam.entity.Exam;
import com.exam.entity.PaymentOrder;
import com.exam.entity.Registration;
import com.exam.entity.SysUser;
import com.exam.mapper.ExamMapper;
import com.exam.mapper.PaymentOrderMapper;
import com.exam.mapper.RegistrationMapper;
import com.exam.mapper.SysUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据统计服务
 *
 * @author system
 * @since 2024-10-17
 */
@Slf4j
@Service
public class StatisticsService {

    @Autowired
    private ExamMapper examMapper;

    @Autowired
    private RegistrationMapper registrationMapper;

    @Autowired
    private PaymentOrderMapper paymentOrderMapper;

    @Autowired
    private SysUserMapper userMapper;

    /**
     * 获取管理端仪表盘统计数据
     *
     * @return 统计数据
     */
    public Result getDashboardStats() {
        try {
            log.info("获取管理端仪表盘统计数据");

            Map<String, Object> result = new HashMap<>();

            // 1. 考试统计
            Map<String, Object> examStats = getExamStatistics();
            result.put("examStats", examStats);

            // 2. 报名统计
            Map<String, Object> registrationStats = getRegistrationStatistics(null);
            result.put("registrationStats", registrationStats);

            // 3. 缴费统计
            Map<String, Object> paymentStats = getPaymentStatistics(null);
            result.put("paymentStats", paymentStats);

            // 4. 用户统计
            Map<String, Object> userStats = getUserStatistics();
            result.put("userStats", userStats);

            // 5. 最近30天报名趋势
            List<Map<String, Object>> registrationTrend = getRegistrationTrend(30);
            result.put("registrationTrend", registrationTrend);

            // 6. 最近30天缴费趋势
            List<Map<String, Object>> paymentTrend = getPaymentTrend(30);
            result.put("paymentTrend", paymentTrend);

            return Result.success(result);

        } catch (Exception e) {
            log.error("获取仪表盘统计数据异常", e);
            return Result.error("获取统计数据失败：" + e.getMessage());
        }
    }

    /**
     * 考试统计
     *
     * @return 考试统计数据
     */
    public Map<String, Object> getExamStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // 总考试数
        Long totalCount = examMapper.selectCount(null);
        stats.put("totalCount", totalCount);

        // 按状态统计
        // 1-草稿 2-已发布 3-报名中 4-报名结束 5-已完成 6-已取消
        stats.put("draftCount", examMapper.selectCount(new LambdaQueryWrapper<Exam>().eq(Exam::getStatus, 1)));
        stats.put("publishedCount", examMapper.selectCount(new LambdaQueryWrapper<Exam>().eq(Exam::getStatus, 2)));
        stats.put("registrationOpenCount", examMapper.selectCount(new LambdaQueryWrapper<Exam>().eq(Exam::getStatus, 3)));
        stats.put("registrationClosedCount", examMapper.selectCount(new LambdaQueryWrapper<Exam>().eq(Exam::getStatus, 4)));
        stats.put("completedCount", examMapper.selectCount(new LambdaQueryWrapper<Exam>().eq(Exam::getStatus, 5)));
        stats.put("cancelledCount", examMapper.selectCount(new LambdaQueryWrapper<Exam>().eq(Exam::getStatus, 6)));

        return stats;
    }

    /**
     * 报名统计
     *
     * @param examId 考试ID（可选）
     * @return 报名统计数据
     */
    public Map<String, Object> getRegistrationStatistics(Long examId) {
        Map<String, Object> stats = new HashMap<>();

        // 总报名数
        LambdaQueryWrapper<Registration> totalWrapper = new LambdaQueryWrapper<>();
        if (examId != null) {
            totalWrapper.eq(Registration::getExamId, examId);
        }
        Long totalCount = registrationMapper.selectCount(totalWrapper);
        stats.put("totalCount", totalCount);

        // 按审核状态统计
        // 1-待审核 2-审核通过 3-审核驳回
        LambdaQueryWrapper<Registration> pendingWrapper = new LambdaQueryWrapper<>();
        if (examId != null) {
            pendingWrapper.eq(Registration::getExamId, examId);
        }
        stats.put("pendingCount", registrationMapper.selectCount(pendingWrapper.eq(Registration::getAuditStatus, 1)));

        LambdaQueryWrapper<Registration> approvedWrapper = new LambdaQueryWrapper<>();
        if (examId != null) {
            approvedWrapper.eq(Registration::getExamId, examId);
        }
        stats.put("approvedCount", registrationMapper.selectCount(approvedWrapper.eq(Registration::getAuditStatus, 2)));

        LambdaQueryWrapper<Registration> rejectedWrapper = new LambdaQueryWrapper<>();
        if (examId != null) {
            rejectedWrapper.eq(Registration::getExamId, examId);
        }
        stats.put("rejectedCount", registrationMapper.selectCount(rejectedWrapper.eq(Registration::getAuditStatus, 3)));

        // 按缴费状态统计
        // 1-未缴费 2-已缴费 3-已退费
        LambdaQueryWrapper<Registration> unpaidWrapper = new LambdaQueryWrapper<>();
        if (examId != null) {
            unpaidWrapper.eq(Registration::getExamId, examId);
        }
        stats.put("unpaidCount", registrationMapper.selectCount(unpaidWrapper.eq(Registration::getPaymentStatus, 1)));

        LambdaQueryWrapper<Registration> paidWrapper = new LambdaQueryWrapper<>();
        if (examId != null) {
            paidWrapper.eq(Registration::getExamId, examId);
        }
        stats.put("paidCount", registrationMapper.selectCount(paidWrapper.eq(Registration::getPaymentStatus, 2)));

        LambdaQueryWrapper<Registration> refundedWrapper = new LambdaQueryWrapper<>();
        if (examId != null) {
            refundedWrapper.eq(Registration::getExamId, examId);
        }
        stats.put("refundedPaymentCount", registrationMapper.selectCount(refundedWrapper.eq(Registration::getPaymentStatus, 3)));

        return stats;
    }

    /**
     * 缴费统计
     *
     * @param examId 考试ID（可选）
     * @return 缴费统计数据
     */
    public Map<String, Object> getPaymentStatistics(Long examId) {
        Map<String, Object> stats = new HashMap<>();

        // 总订单数
        LambdaQueryWrapper<PaymentOrder> totalWrapper = new LambdaQueryWrapper<>();
        if (examId != null) {
            totalWrapper.eq(PaymentOrder::getExamId, examId);
        }
        Long totalCount = paymentOrderMapper.selectCount(totalWrapper);
        stats.put("totalCount", totalCount);

        // 按状态统计订单数
        // 1-待支付 2-已支付 3-已关闭 4-已退款
        LambdaQueryWrapper<PaymentOrder> unpaidWrapper = new LambdaQueryWrapper<>();
        if (examId != null) {
            unpaidWrapper.eq(PaymentOrder::getExamId, examId);
        }
        stats.put("unpaidCount", paymentOrderMapper.selectCount(unpaidWrapper.eq(PaymentOrder::getStatus, 1)));

        LambdaQueryWrapper<PaymentOrder> paidWrapper = new LambdaQueryWrapper<>();
        if (examId != null) {
            paidWrapper.eq(PaymentOrder::getExamId, examId);
        }
        stats.put("paidCount", paymentOrderMapper.selectCount(paidWrapper.eq(PaymentOrder::getStatus, 2)));

        LambdaQueryWrapper<PaymentOrder> closedWrapper = new LambdaQueryWrapper<>();
        if (examId != null) {
            closedWrapper.eq(PaymentOrder::getExamId, examId);
        }
        stats.put("closedCount", paymentOrderMapper.selectCount(closedWrapper.eq(PaymentOrder::getStatus, 3)));

        LambdaQueryWrapper<PaymentOrder> refundedWrapper = new LambdaQueryWrapper<>();
        if (examId != null) {
            refundedWrapper.eq(PaymentOrder::getExamId, examId);
        }
        stats.put("refundedCount", paymentOrderMapper.selectCount(refundedWrapper.eq(PaymentOrder::getStatus, 4)));

        // 统计金额
        BigDecimal totalAmount = paymentOrderMapper.sumAmountByCondition(examId, null, null, null);
        stats.put("totalAmount", totalAmount != null ? totalAmount : BigDecimal.ZERO);

        BigDecimal paidAmount = paymentOrderMapper.sumAmountByCondition(examId, 2, null, null);
        stats.put("paidAmount", paidAmount != null ? paidAmount : BigDecimal.ZERO);

        BigDecimal unpaidAmount = paymentOrderMapper.sumAmountByCondition(examId, 1, null, null);
        stats.put("unpaidAmount", unpaidAmount != null ? unpaidAmount : BigDecimal.ZERO);

        BigDecimal refundedAmount = paymentOrderMapper.sumAmountByCondition(examId, 4, null, null);
        stats.put("refundedAmount", refundedAmount != null ? refundedAmount : BigDecimal.ZERO);

        return stats;
    }

    /**
     * 用户统计
     *
     * @return 用户统计数据
     */
    public Map<String, Object> getUserStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // 总用户数
        Long totalCount = userMapper.selectCount(null);
        stats.put("totalCount", totalCount);

        // 按角色统计
        Long userCount = userMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getRole, "user"));
        stats.put("userCount", userCount);

        Long adminCount = userMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getRole, "admin"));
        stats.put("adminCount", adminCount);

        // 按状态统计
        // 1-正常 2-禁用
        Long activeCount = userMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getStatus, 1));
        stats.put("activeCount", activeCount);

        Long disabledCount = userMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getStatus, 2));
        stats.put("disabledCount", disabledCount);

        return stats;
    }

    /**
     * 获取报名趋势（最近N天）
     *
     * @param days 天数
     * @return 趋势数据
     */
    public List<Map<String, Object>> getRegistrationTrend(Integer days) {
        List<Map<String, Object>> trend = new ArrayList<>();

        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(days);

        // 查询时间范围内的所有报名记录
        LambdaQueryWrapper<Registration> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(Registration::getCreateTime, startDate);
        wrapper.le(Registration::getCreateTime, endDate);
        wrapper.orderByAsc(Registration::getCreateTime);

        List<Registration> registrations = registrationMapper.selectList(wrapper);

        // 按天统计
        Map<String, Integer> dailyCount = new HashMap<>();
        for (Registration registration : registrations) {
            String date = registration.getCreateTime().toLocalDate().toString();
            dailyCount.put(date, dailyCount.getOrDefault(date, 0) + 1);
        }

        // 填充所有日期（包括没有数据的日期）
        for (int i = 0; i < days; i++) {
            String date = startDate.plusDays(i).toLocalDate().toString();
            Map<String, Object> item = new HashMap<>();
            item.put("date", date);
            item.put("count", dailyCount.getOrDefault(date, 0));
            trend.add(item);
        }

        return trend;
    }

    /**
     * 获取缴费趋势（最近N天）
     *
     * @param days 天数
     * @return 趋势数据
     */
    public List<Map<String, Object>> getPaymentTrend(Integer days) {
        List<Map<String, Object>> trend = new ArrayList<>();

        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(days);

        // 查询时间范围内的所有已支付订单
        LambdaQueryWrapper<PaymentOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentOrder::getStatus, 2); // 已支付
        wrapper.ge(PaymentOrder::getPayTime, startDate);
        wrapper.le(PaymentOrder::getPayTime, endDate);
        wrapper.orderByAsc(PaymentOrder::getPayTime);

        List<PaymentOrder> payments = paymentOrderMapper.selectList(wrapper);

        // 按天统计
        Map<String, BigDecimal> dailyAmount = new HashMap<>();
        Map<String, Integer> dailyCount = new HashMap<>();

        for (PaymentOrder payment : payments) {
            if (payment.getPayTime() != null) {
                String date = payment.getPayTime().toLocalDate().toString();
                dailyAmount.put(date, dailyAmount.getOrDefault(date, BigDecimal.ZERO).add(payment.getAmount()));
                dailyCount.put(date, dailyCount.getOrDefault(date, 0) + 1);
            }
        }

        // 填充所有日期（包括没有数据的日期）
        for (int i = 0; i < days; i++) {
            String date = startDate.plusDays(i).toLocalDate().toString();
            Map<String, Object> item = new HashMap<>();
            item.put("date", date);
            item.put("amount", dailyAmount.getOrDefault(date, BigDecimal.ZERO));
            item.put("count", dailyCount.getOrDefault(date, 0));
            trend.add(item);
        }

        return trend;
    }

    /**
     * 按考试统计详细数据
     *
     * @return 每个考试的统计数据
     */
    public Result getExamDetailStats() {
        try {
            log.info("获取按考试统计的详细数据");

            List<Exam> exams = examMapper.selectList(
                    new LambdaQueryWrapper<Exam>()
                            .in(Exam::getStatus, 2, 3, 4, 5) // 已发布、报名中、报名结束、已完成
                            .orderByDesc(Exam::getCreateTime)
            );

            List<Map<String, Object>> result = new ArrayList<>();

            for (Exam exam : exams) {
                Map<String, Object> item = new HashMap<>();
                item.put("examId", exam.getId());
                item.put("examName", exam.getExamName());
                item.put("examDate", exam.getExamDate());
                item.put("examType", exam.getExamType());
                item.put("fee", exam.getFee());
                item.put("status", exam.getStatus());

                // 报名统计
                Map<String, Object> registrationStats = getRegistrationStatistics(exam.getId());
                item.put("registrationStats", registrationStats);

                // 缴费统计
                Map<String, Object> paymentStats = getPaymentStatistics(exam.getId());
                item.put("paymentStats", paymentStats);

                result.add(item);
            }

            return Result.success(result);

        } catch (Exception e) {
            log.error("获取按考试统计的详细数据异常", e);
            return Result.error("获取统计数据失败：" + e.getMessage());
        }
    }
}
