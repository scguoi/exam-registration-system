package com.exam.controller;

import com.exam.common.Result;
import com.exam.service.StatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 数据统计控制器
 *
 * @author system
 * @since 2024-10-17
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/statistics")
@PreAuthorize("hasRole('ADMIN')") // 仅管理员可访问
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    /**
     * 获取管理端仪表盘统计数据
     *
     * @return Result
     */
    @GetMapping("/dashboard")
    public Result getDashboardStats() {
        log.info("获取管理端仪表盘统计数据");
        return statisticsService.getDashboardStats();
    }

    /**
     * 获取考试统计
     *
     * @return Result
     */
    @GetMapping("/exam")
    public Result getExamStats() {
        log.info("获取考试统计");
        return Result.success(statisticsService.getExamStatistics());
    }

    /**
     * 获取报名统计
     *
     * @param examId 考试ID（可选）
     * @return Result
     */
    @GetMapping("/registration")
    public Result getRegistrationStats(@RequestParam(required = false) Long examId) {
        log.info("获取报名统计，examId={}", examId);
        return Result.success(statisticsService.getRegistrationStatistics(examId));
    }

    /**
     * 获取缴费统计
     *
     * @param examId 考试ID（可选）
     * @return Result
     */
    @GetMapping("/payment")
    public Result getPaymentStats(@RequestParam(required = false) Long examId) {
        log.info("获取缴费统计，examId={}", examId);
        return Result.success(statisticsService.getPaymentStatistics(examId));
    }

    /**
     * 获取用户统计
     *
     * @return Result
     */
    @GetMapping("/user")
    public Result getUserStats() {
        log.info("获取用户统计");
        return Result.success(statisticsService.getUserStatistics());
    }

    /**
     * 获取报名趋势（最近N天）
     *
     * @param days 天数（默认30天）
     * @return Result
     */
    @GetMapping("/registration-trend")
    public Result getRegistrationTrend(@RequestParam(defaultValue = "30") Integer days) {
        log.info("获取报名趋势，days={}", days);
        return Result.success(statisticsService.getRegistrationTrend(days));
    }

    /**
     * 获取缴费趋势（最近N天）
     *
     * @param days 天数（默认30天）
     * @return Result
     */
    @GetMapping("/payment-trend")
    public Result getPaymentTrend(@RequestParam(defaultValue = "30") Integer days) {
        log.info("获取缴费趋势，days={}", days);
        return Result.success(statisticsService.getPaymentTrend(days));
    }

    /**
     * 按考试统计详细数据
     *
     * @return Result
     */
    @GetMapping("/exam-detail")
    public Result getExamDetailStats() {
        log.info("获取按考试统计的详细数据");
        return statisticsService.getExamDetailStats();
    }
}
