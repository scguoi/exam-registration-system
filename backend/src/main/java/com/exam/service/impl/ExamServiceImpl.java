package com.exam.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.exam.dto.ExamCreateRequest;
import com.exam.dto.ExamUpdateRequest;
import com.exam.entity.Exam;
import com.exam.exception.BusinessException;
import com.exam.mapper.ExamMapper;
import com.exam.service.ExamService;
import com.exam.common.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 考试服务实现类
 * 
 * @author system
 * @since 2024-10-16
 */
@Slf4j
@Service
public class ExamServiceImpl extends ServiceImpl<ExamMapper, Exam> implements ExamService {

    @Override
    public PageResult<Exam> getExamPage(Integer current, Integer size, String examName, String examType, Integer status) {
        Page<Exam> page = new Page<>(current, size);
        IPage<Exam> result = baseMapper.selectExamPage(page, examName, examType, status);
        return PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public PageResult<Exam> getAvailableExamPage(Integer current, Integer size, String examName, String examType) {
        Page<Exam> page = new Page<>(current, size);
        IPage<Exam> result = baseMapper.selectAvailableExamPage(page, examName, examType, LocalDateTime.now());
        return PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Exam createExam(ExamCreateRequest request, Long createBy) {
        // 检查考试名称是否重复
        if (count(new LambdaQueryWrapper<Exam>().eq(Exam::getExamName, request.getExamName())) > 0) {
            throw new BusinessException("考试名称已存在");
        }

        // 检查时间设置是否合理
        if (request.getRegistrationStart().isAfter(request.getRegistrationEnd())) {
            throw new BusinessException("报名开始时间不能晚于结束时间");
        }

        if (request.getRegistrationEnd().isAfter(request.getExamDate().atStartOfDay())) {
            throw new BusinessException("报名结束时间不能晚于考试日期");
        }

        Exam exam = new Exam();
        BeanUtil.copyProperties(request, exam);
        exam.setCreateBy(createBy);
        exam.setStatus(1); // 草稿状态
        exam.setCurrentCount(0);

        save(exam);
        log.info("创建考试成功: {}", request.getExamName());
        return exam;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateExam(Long examId, ExamUpdateRequest request) {
        Exam exam = getById(examId);
        if (exam == null) {
            throw new BusinessException("考试不存在");
        }

        // 检查考试名称是否重复
        if (request.getExamName() != null && !request.getExamName().equals(exam.getExamName())) {
            if (count(new LambdaQueryWrapper<Exam>().eq(Exam::getExamName, request.getExamName())) > 0) {
                throw new BusinessException("考试名称已存在");
            }
        }

        // 检查时间设置是否合理
        if (request.getRegistrationStart() != null && request.getRegistrationEnd() != null) {
            if (request.getRegistrationStart().isAfter(request.getRegistrationEnd())) {
                throw new BusinessException("报名开始时间不能晚于结束时间");
            }
        }

        BeanUtil.copyProperties(request, exam, "id", "createBy", "createTime", "currentCount");
        return updateById(exam);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean publishExam(Long examId) {
        Exam exam = getById(examId);
        if (exam == null) {
            throw new BusinessException("考试不存在");
        }

        if (exam.getStatus() != 1) {
            throw new BusinessException("只有草稿状态的考试才能发布");
        }

        exam.setStatus(2); // 已发布
        return updateById(exam);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unpublishExam(Long examId) {
        Exam exam = getById(examId);
        if (exam == null) {
            throw new BusinessException("考试不存在");
        }

        if (exam.getStatus() == 5) {
            throw new BusinessException("已结束的考试不能下架");
        }

        exam.setStatus(1); // 草稿
        return updateById(exam);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteExam(Long examId) {
        Exam exam = getById(examId);
        if (exam == null) {
            throw new BusinessException("考试不存在");
        }

        if (exam.getCurrentCount() > 0) {
            throw new BusinessException("已有报名的考试不能删除");
        }

        return removeById(examId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean incrementCurrentCount(Long examId) {
        return baseMapper.incrementCurrentCount(examId) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean decrementCurrentCount(Long examId) {
        return baseMapper.decrementCurrentCount(examId) > 0;
    }

    @Override
    public boolean canRegister(Long examId) {
        Exam exam = getById(examId);
        if (exam == null) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        
        // 检查考试状态
        if (exam.getStatus() != 2 && exam.getStatus() != 3) {
            return false;
        }

        // 检查报名时间
        if (now.isBefore(exam.getRegistrationStart()) || now.isAfter(exam.getRegistrationEnd())) {
            return false;
        }

        // 检查报名名额
        if (exam.getTotalQuota() != null && exam.getCurrentCount() >= exam.getTotalQuota()) {
            return false;
        }

        return true;
    }
}
