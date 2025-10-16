package com.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.entity.PaymentOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 缴费订单表Mapper接口
 * 
 * @author system
 * @since 2024-10-16
 */
@Mapper
public interface PaymentOrderMapper extends BaseMapper<PaymentOrder> {

    /**
     * 根据订单号查询订单
     * 
     * @param orderNo 订单号
     * @return 订单信息
     */
    PaymentOrder selectByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 根据报名ID查询订单
     * 
     * @param registrationId 报名ID
     * @return 订单信息
     */
    PaymentOrder selectByRegistrationId(@Param("registrationId") Long registrationId);

    /**
     * 分页查询订单列表
     * 
     * @param page 分页对象
     * @param userId 用户ID
     * @param examId 考试ID
     * @param status 订单状态
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 订单列表
     */
    IPage<PaymentOrder> selectOrderPage(Page<PaymentOrder> page,
                                       @Param("userId") Long userId,
                                       @Param("examId") Long examId,
                                       @Param("status") Integer status,
                                       @Param("startTime") LocalDateTime startTime,
                                       @Param("endTime") LocalDateTime endTime);

    /**
     * 查询用户的订单列表
     * 
     * @param userId 用户ID
     * @return 订单列表
     */
    List<PaymentOrder> selectByUserId(@Param("userId") Long userId);

    /**
     * 统计缴费数据
     * 
     * @param examId 考试ID
     * @param status 订单状态
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计金额
     */
    BigDecimal sumAmountByCondition(@Param("examId") Long examId,
                                   @Param("status") Integer status,
                                   @Param("startTime") LocalDateTime startTime,
                                   @Param("endTime") LocalDateTime endTime);

    /**
     * 查询过期的订单
     * 
     * @param currentTime 当前时间
     * @return 过期订单列表
     */
    List<PaymentOrder> selectExpiredOrders(@Param("currentTime") LocalDateTime currentTime);
}
