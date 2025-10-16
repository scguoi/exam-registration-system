package com.exam.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.exam.entity.Exam;
import com.exam.dto.ExamCreateRequest;
import com.exam.dto.ExamUpdateRequest;
import com.exam.common.PageResult;

/**
 * 考试服务接口
 * 
 * @author system
 * @since 2024-10-16
 */
public interface ExamService extends IService<Exam> {

    /**
     * 分页查询考试列表
     */
    PageResult<Exam> getExamPage(Integer current, Integer size, String examName, String examType, Integer status);

    /**
     * 分页查询可报名的考试列表
     */
    PageResult<Exam> getAvailableExamPage(Integer current, Integer size, String examName, String examType);

    /**
     * 创建考试
     */
    Exam createExam(ExamCreateRequest request, Long createBy);

    /**
     * 更新考试
     */
    boolean updateExam(Long examId, ExamUpdateRequest request);

    /**
     * 发布考试
     */
    boolean publishExam(Long examId);

    /**
     * 下架考试
     */
    boolean unpublishExam(Long examId);

    /**
     * 删除考试
     */
    boolean deleteExam(Long examId);

    /**
     * 增加报名人数
     */
    boolean incrementCurrentCount(Long examId);

    /**
     * 减少报名人数
     */
    boolean decrementCurrentCount(Long examId);

    /**
     * 检查考试是否可以报名
     */
    boolean canRegister(Long examId);
}
