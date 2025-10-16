package com.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.entity.Exam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * 考试表Mapper接口
 * 
 * @author system
 * @since 2024-10-16
 */
@Mapper
public interface ExamMapper extends BaseMapper<Exam> {

    /**
     * 分页查询考试列表
     * 
     * @param page 分页对象
     * @param examName 考试名称
     * @param examType 考试类型
     * @param status 状态
     * @return 考试列表
     */
    IPage<Exam> selectExamPage(Page<Exam> page, 
                              @Param("examName") String examName,
                              @Param("examType") String examType,
                              @Param("status") Integer status);

    /**
     * 查询可报名的考试列表
     * 
     * @param page 分页对象
     * @param examName 考试名称
     * @param examType 考试类型
     * @param currentTime 当前时间
     * @return 考试列表
     */
    IPage<Exam> selectAvailableExamPage(Page<Exam> page,
                                       @Param("examName") String examName,
                                       @Param("examType") String examType,
                                       @Param("currentTime") LocalDateTime currentTime);

    /**
     * 增加报名人数
     * 
     * @param examId 考试ID
     * @return 更新行数
     */
    int incrementCurrentCount(@Param("examId") Long examId);

    /**
     * 减少报名人数
     * 
     * @param examId 考试ID
     * @return 更新行数
     */
    int decrementCurrentCount(@Param("examId") Long examId);
}
