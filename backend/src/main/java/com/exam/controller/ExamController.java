package com.exam.controller;

import com.exam.common.PageResult;
import com.exam.common.Result;
import com.exam.dto.ExamCreateRequest;
import com.exam.dto.ExamUpdateRequest;
import com.exam.entity.Exam;
import com.exam.security.CustomUserDetails;
import com.exam.service.ExamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 考试控制器
 * 
 * @author system
 * @since 2024-10-16
 */
@Slf4j
@RestController
@RequestMapping("/v1/exams")
public class ExamController {

    @Autowired
    private ExamService examService;

    /**
     * 分页查询考试列表
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<Exam>> getExamPage(@RequestParam(defaultValue = "1") Integer current,
                                               @RequestParam(defaultValue = "10") Integer size,
                                               @RequestParam(required = false) String examName,
                                               @RequestParam(required = false) String examType,
                                               @RequestParam(required = false) Integer status) {
        PageResult<Exam> result = examService.getExamPage(current, size, examName, examType, status);
        return Result.success(result);
    }

    /**
     * 分页查询可报名的考试列表
     */
    @GetMapping("/available")
    public Result<PageResult<Exam>> getAvailableExamPage(@RequestParam(defaultValue = "1") Integer current,
                                                        @RequestParam(defaultValue = "10") Integer size,
                                                        @RequestParam(required = false) String examName,
                                                        @RequestParam(required = false) String examType) {
        PageResult<Exam> result = examService.getAvailableExamPage(current, size, examName, examType);
        return Result.success(result);
    }

    /**
     * 获取考试详情
     */
    @GetMapping("/{id}")
    public Result<Exam> getExamDetail(@PathVariable Long id) {
        Exam exam = examService.getById(id);
        if (exam == null) {
            return Result.notFound("考试不存在");
        }
        return Result.success(exam);
    }

    /**
     * 创建考试
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Exam> createExam(@AuthenticationPrincipal CustomUserDetails userDetails,
                                  @Valid @RequestBody ExamCreateRequest request) {
        Exam exam = examService.createExam(request, userDetails.getUserId());
        return Result.success("创建成功", exam);
    }

    /**
     * 更新考试
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateExam(@PathVariable Long id,
                                  @Valid @RequestBody ExamUpdateRequest request) {
        boolean success = examService.updateExam(id, request);
        return success ? Result.success() : Result.error("更新失败");
    }

    /**
     * 发布考试
     */
    @PutMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> publishExam(@PathVariable Long id) {
        boolean success = examService.publishExam(id);
        return success ? Result.success() : Result.error("发布失败");
    }

    /**
     * 下架考试
     */
    @PutMapping("/{id}/unpublish")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> unpublishExam(@PathVariable Long id) {
        boolean success = examService.unpublishExam(id);
        return success ? Result.success() : Result.error("下架失败");
    }

    /**
     * 删除考试
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteExam(@PathVariable Long id) {
        boolean success = examService.deleteExam(id);
        return success ? Result.success() : Result.error("删除失败");
    }
}
