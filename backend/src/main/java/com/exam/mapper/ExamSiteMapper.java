package com.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exam.entity.ExamSite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 考点表Mapper接口
 * 
 * @author system
 * @since 2024-10-16
 */
@Mapper
public interface ExamSiteMapper extends BaseMapper<ExamSite> {

    /**
     * 根据考试ID查询考点列表
     * 
     * @param examId 考试ID
     * @return 考点列表
     */
    List<ExamSite> selectByExamId(@Param("examId") Long examId);

    /**
     * 根据考试ID查询可用考点列表
     * 
     * @param examId 考试ID
     * @return 可用考点列表
     */
    List<ExamSite> selectAvailableByExamId(@Param("examId") Long examId);

    /**
     * 增加考点报名人数
     * 
     * @param siteId 考点ID
     * @return 更新行数
     */
    int incrementCurrentCount(@Param("siteId") Long siteId);

    /**
     * 减少考点报名人数
     * 
     * @param siteId 考点ID
     * @return 更新行数
     */
    int decrementCurrentCount(@Param("siteId") Long siteId);
}
