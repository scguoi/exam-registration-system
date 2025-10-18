package com.exam.service;

import com.exam.common.Result;
import com.exam.entity.Exam;
import com.exam.entity.PaymentOrder;
import com.exam.entity.Registration;
import com.exam.mapper.ExamMapper;
import com.exam.mapper.PaymentOrderMapper;
import com.exam.mapper.RegistrationMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * PaymentOrderService 单元测试
 *
 * @author system
 * @since 2024-10-18
 */
@ExtendWith(MockitoExtension.class)
class PaymentOrderServiceTest {

    @Mock
    private PaymentOrderMapper paymentOrderMapper;

    @Mock
    private RegistrationMapper registrationMapper;

    @Mock
    private ExamMapper examMapper;

    @InjectMocks
    private PaymentOrderService paymentOrderService;

    private Registration mockRegistration;
    private Exam mockExam;
    private PaymentOrder mockOrder;

    @BeforeEach
    void setUp() {
        // 初始化模拟报名记录
        mockRegistration = new Registration();
        mockRegistration.setId(1L);
        mockRegistration.setUserId(1L);
        mockRegistration.setExamId(1L);
        mockRegistration.setAuditStatus(2); // 已审核通过
        mockRegistration.setPaymentStatus(1); // 未缴费

        // 初始化模拟考试
        mockExam = new Exam();
        mockExam.setId(1L);
        mockExam.setExamName("2025年成人高考");
        mockExam.setFee(new BigDecimal("100.00"));

        // 初始化模拟订单
        mockOrder = new PaymentOrder();
        mockOrder.setId(1L);
        mockOrder.setOrderNo("PO20241018123456123456");
        mockOrder.setRegistrationId(1L);
        mockOrder.setUserId(1L);
        mockOrder.setExamId(1L);
        mockOrder.setAmount(new BigDecimal("100.00"));
        mockOrder.setStatus(1); // 待支付
        mockOrder.setExpireTime(LocalDateTime.now().plusMinutes(30));
    }

    @Test
    void testCreatePaymentOrder_Success() {
        // Mock 数据
        when(registrationMapper.selectById(1L)).thenReturn(mockRegistration);
        when(paymentOrderMapper.selectByRegistrationId(1L)).thenReturn(null); // 未存在订单
        when(examMapper.selectById(1L)).thenReturn(mockExam);
        when(paymentOrderMapper.insert(any(PaymentOrder.class))).thenReturn(1);

        // 执行测试
        Result result = paymentOrderService.createPaymentOrder(1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(200, result.getCode());

        // 验证方法调用
        verify(registrationMapper, times(1)).selectById(1L);
        verify(examMapper, times(1)).selectById(1L);
        verify(paymentOrderMapper, times(1)).insert(any(PaymentOrder.class));
    }

    @Test
    void testCreatePaymentOrder_RegistrationNotFound() {
        // Mock 报名记录不存在
        when(registrationMapper.selectById(1L)).thenReturn(null);

        // 执行测试
        Result result = paymentOrderService.createPaymentOrder(1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertEquals("报名记录不存在", result.getMessage());

        // 验证未调用插入方法
        verify(paymentOrderMapper, never()).insert(any(PaymentOrder.class));
    }

    @Test
    void testCreatePaymentOrder_NotApproved() {
        // Mock 报名未审核通过
        mockRegistration.setAuditStatus(1); // 待审核
        when(registrationMapper.selectById(1L)).thenReturn(mockRegistration);

        // 执行测试
        Result result = paymentOrderService.createPaymentOrder(1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertTrue(result.getMessage().contains("未审核通过"));

        // 验证未调用插入方法
        verify(paymentOrderMapper, never()).insert(any(PaymentOrder.class));
    }

    @Test
    void testCreatePaymentOrder_OrderAlreadyExists() {
        // Mock 订单已存在
        when(registrationMapper.selectById(1L)).thenReturn(mockRegistration);
        when(paymentOrderMapper.selectByRegistrationId(1L)).thenReturn(mockOrder);

        // 执行测试
        Result result = paymentOrderService.createPaymentOrder(1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());

        // 验证未调用插入方法（因为订单已存在）
        verify(paymentOrderMapper, never()).insert(any(PaymentOrder.class));
    }

    @Test
    void testExecutePay_Success() {
        // Mock 数据
        when(paymentOrderMapper.selectByOrderNo("PO20241018123456123456")).thenReturn(mockOrder);
        when(paymentOrderMapper.updateById(any(PaymentOrder.class))).thenReturn(1);
        when(registrationMapper.selectById(1L)).thenReturn(mockRegistration);
        when(registrationMapper.updateById(any(Registration.class))).thenReturn(1);

        // 执行测试
        Result result = paymentOrderService.executePay("PO20241018123456123456", "mock", 1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertTrue(result.getMessage().contains("成功"));

        // 验证方法调用
        verify(paymentOrderMapper, times(1)).updateById(any(PaymentOrder.class));
        verify(registrationMapper, times(1)).updateById(any(Registration.class));
    }

    @Test
    void testExecutePay_OrderNotFound() {
        // Mock 订单不存在
        when(paymentOrderMapper.selectByOrderNo("PO20241018123456123456")).thenReturn(null);

        // 执行测试
        Result result = paymentOrderService.executePay("PO20241018123456123456", "mock", 1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertEquals("订单不存在", result.getMessage());

        // 验证未调用更新方法
        verify(paymentOrderMapper, never()).updateById(any(PaymentOrder.class));
    }

    @Test
    void testExecutePay_Unauthorized() {
        // Mock 数据
        when(paymentOrderMapper.selectByOrderNo("PO20241018123456123456")).thenReturn(mockOrder);

        // 执行测试：用户2尝试支付用户1的订单
        Result result = paymentOrderService.executePay("PO20241018123456123456", "mock", 2L);

        // 验证结果
        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertTrue(result.getMessage().contains("无权"));

        // 验证未调用更新方法
        verify(paymentOrderMapper, never()).updateById(any(PaymentOrder.class));
    }

    @Test
    void testExecutePay_InvalidStatus() {
        // Mock 订单已支付
        mockOrder.setStatus(2);
        when(paymentOrderMapper.selectByOrderNo("PO20241018123456123456")).thenReturn(mockOrder);

        // 执行测试
        Result result = paymentOrderService.executePay("PO20241018123456123456", "mock", 1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertTrue(result.getMessage().contains("状态不正确"));

        // 验证未调用更新方法
        verify(paymentOrderMapper, never()).updateById(any(PaymentOrder.class));
    }

    @Test
    void testExecutePay_OrderExpired() {
        // Mock 订单已过期
        mockOrder.setExpireTime(LocalDateTime.now().minusMinutes(1));
        when(paymentOrderMapper.selectByOrderNo("PO20241018123456123456")).thenReturn(mockOrder);
        when(paymentOrderMapper.updateById(any(PaymentOrder.class))).thenReturn(1);

        // 执行测试
        Result result = paymentOrderService.executePay("PO20241018123456123456", "mock", 1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertTrue(result.getMessage().contains("已过期"));

        // 验证调用了更新方法（关闭订单）
        verify(paymentOrderMapper, times(1)).updateById(any(PaymentOrder.class));
    }

    @Test
    void testGetOrderDetail_Success() {
        // Mock 数据
        when(paymentOrderMapper.selectByOrderNo("PO20241018123456123456")).thenReturn(mockOrder);

        // 执行测试：管理员查询
        Result result = paymentOrderService.getOrderDetail("PO20241018123456123456", null);

        // 验证结果
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
    }

    @Test
    void testGetOrderDetail_UserUnauthorized() {
        // Mock 数据
        when(paymentOrderMapper.selectByOrderNo("PO20241018123456123456")).thenReturn(mockOrder);

        // 执行测试：用户2查询用户1的订单
        Result result = paymentOrderService.getOrderDetail("PO20241018123456123456", 2L);

        // 验证结果
        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertTrue(result.getMessage().contains("无权"));
    }

    @Test
    void testApplyRefund_Success() {
        // Mock 已支付的订单
        mockOrder.setStatus(2);
        when(paymentOrderMapper.selectByOrderNo("PO20241018123456123456")).thenReturn(mockOrder);
        when(paymentOrderMapper.updateById(any(PaymentOrder.class))).thenReturn(1);
        when(registrationMapper.selectById(1L)).thenReturn(mockRegistration);
        when(registrationMapper.updateById(any(Registration.class))).thenReturn(1);

        // 执行测试
        Result result = paymentOrderService.applyRefund("PO20241018123456123456", "考试取消");

        // 验证结果
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertTrue(result.getMessage().contains("成功"));

        // 验证方法调用
        verify(paymentOrderMapper, times(1)).updateById(any(PaymentOrder.class));
        verify(registrationMapper, times(1)).updateById(any(Registration.class));
    }

    @Test
    void testApplyRefund_InvalidStatus() {
        // Mock 未支付的订单
        mockOrder.setStatus(1);
        when(paymentOrderMapper.selectByOrderNo("PO20241018123456123456")).thenReturn(mockOrder);

        // 执行测试
        Result result = paymentOrderService.applyRefund("PO20241018123456123456", "考试取消");

        // 验证结果
        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertTrue(result.getMessage().contains("状态不正确"));

        // 验证未调用更新方法
        verify(paymentOrderMapper, never()).updateById(any(PaymentOrder.class));
    }
}
