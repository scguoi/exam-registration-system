package com.exam.controller;

import com.exam.common.Result;
import com.exam.dto.AuditRequest;
import com.exam.dto.RegistrationRequest;
import com.exam.security.CustomUserDetails;
import com.exam.service.RegistrationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 报名控制器
 * 提供报名相关的REST API接口
 *
 * @author system
 * @since 2024-10-17
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/registrations")
public class RegistrationController {

    @Autowired
    private RegistrationService registrationService;

    // ==================== 考生端接口 ====================

    /**
     * 提交报名
     * POST /api/v1/registrations
     *
     * @param request     报名请求
     * @param userDetails 当前登录用户
     * @return 报名结果
     */
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public Result submitRegistration(
            @Valid @RequestBody RegistrationRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("考生提交报名，username={}, examId={}",
                userDetails.getUsername(), request.getExamId());

        // 从认证信息中获取用户ID
        request.setUserId(userDetails.getUserId());

        return registrationService.submitRegistration(request);
    }

    /**
     * 查询我的报名记录
     * GET /api/v1/registrations/my
     *
     * @param userDetails 当前登录用户
     * @return 报名列表
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    public Result getMyRegistrations(@AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("查询我的报名记录，username={}", userDetails.getUsername());
        return registrationService.getMyRegistrations(userDetails.getUserId());
    }

    /**
     * 获取报名详情
     * GET /api/v1/registrations/{id}
     *
     * @param id 报名ID
     * @return 报名详情
     */
    @GetMapping("/{id}")
    public Result getRegistrationDetail(@PathVariable Long id) {
        log.info("查询报名详情，registrationId={}", id);
        return registrationService.getRegistrationDetail(id);
    }

    /**
     * 取消报名
     * PUT /api/v1/registrations/{id}/cancel
     *
     * @param id          报名ID
     * @param userDetails 当前登录用户
     * @return 取消结果
     */
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('USER')")
    public Result cancelRegistration(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("取消报名，registrationId={}, username={}", id, userDetails.getUsername());
        return registrationService.cancelRegistration(id, userDetails.getUserId());
    }

    // ==================== 管理员端接口 ====================

    /**
     * 获取待审核报名列表（分页）
     * GET /api/v1/registrations/pending?page=1&size=10&examId=1
     *
     * @param page   页码
     * @param size   每页数量
     * @param examId 考试ID（可选）
     * @return 分页数据
     */
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public Result getPendingRegistrations(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long examId) {

        log.info("查询待审核列表，page={}, size={}, examId={}", page, size, examId);
        return registrationService.getPendingRegistrations(page, size, examId);
    }

    /**
     * 审核报名（通过/驳回）
     * PUT /api/v1/registrations/{id}/audit
     *
     * @param id          报名ID
     * @param request     审核请求
     * @param userDetails 当前登录用户（审核人）
     * @return 审核结果
     */
    @PutMapping("/{id}/audit")
    @PreAuthorize("hasRole('ADMIN')")
    public Result auditRegistration(
            @PathVariable Long id,
            @Valid @RequestBody AuditRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("审核报名，registrationId={}, auditStatus={}, auditor={}",
                id, request.getAuditStatus(), userDetails.getUsername());

        return registrationService.auditRegistration(
                id,
                request.getAuditStatus(),
                request.getAuditRemark(),
                userDetails.getUserId()
        );
    }

    /**
     * 获取所有报名列表（管理员查看，支持筛选）
     * GET /api/v1/registrations?page=1&size=10&auditStatus=1&examId=1
     *
     * @param page        页码
     * @param size        每页数量
     * @param auditStatus 审核状态（可选）
     * @param examId      考试ID（可选）
     * @return 分页数据
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result getAllRegistrations(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer auditStatus,
            @RequestParam(required = false) Long examId) {

        log.info("查询所有报名列表，page={}, size={}, auditStatus={}, examId={}",
                page, size, auditStatus, examId);

        return registrationService.getAllRegistrations(page, size, auditStatus, examId);
    }

    /**
     * 报名统计
     * GET /api/v1/registrations/stats?examId=1
     *
     * @param examId 考试ID（可选）
     * @return 统计数据
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public Result getRegistrationStats(@RequestParam(required = false) Long examId) {
        log.info("查询报名统计，examId={}", examId);
        return registrationService.getRegistrationStats(examId);
    }
}
