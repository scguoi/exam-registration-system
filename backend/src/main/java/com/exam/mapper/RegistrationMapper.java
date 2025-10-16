package com.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.entity.Registration;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 报名表Mapper接口
 * 
 * @author system
 * @since 2024-10-16
 */
@Mapper
public interface RegistrationMapper extends BaseMapper<Registration> {

    /**
     * 分页查询报名列表
     * 
     * @param page 分页对象
     * @param examId 考试ID
     * @param auditStatus 审核状态
     * @param realName 真实姓名
     * @param idCard 身份证号
     * @return 报名列表
     */
    IPage<Registration> selectRegistrationPage(Page<Registration> page,
                                              @Param("examId") Long examId,
                                              @Param("auditStatus") Integer auditStatus,
                                              @Param("realName") String realName,
                                              @Param("idCard") String idCard);

    /**
     * 查询用户的报名记录
     * 
     * @param userId 用户ID
     * @return 报名记录列表
     */
    List<Registration> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询用户指定考试的报名记录
     * 
     * @param userId 用户ID
     * @param examId 考试ID
     * @return 报名记录
     */
    Registration selectByUserIdAndExamId(@Param("userId") Long userId, @Param("examId") Long examId);

    /**
     * 查询待审核的报名列表
     * 
     * @param page 分页对象
     * @param examId 考试ID
     * @return 待审核报名列表
     */
    IPage<Registration> selectPendingAuditPage(Page<Registration> page, @Param("examId") Long examId);

    /**
     * 统计报名数据
     * 
     * @param examId 考试ID
     * @param auditStatus 审核状态
     * @return 统计数量
     */
    int countByExamIdAndAuditStatus(@Param("examId") Long examId, @Param("auditStatus") Integer auditStatus);

    /**
     * 更新准考证下载次数
     * 
     * @param registrationId 报名ID
     * @return 更新行数
     */
    int updateDownloadCount(@Param("registrationId") Long registrationId);
}
