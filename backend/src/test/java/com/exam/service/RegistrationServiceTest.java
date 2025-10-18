package com.exam.service;

import com.exam.common.Result;
import com.exam.dto.RegistrationRequest;
import com.exam.entity.Exam;
import com.exam.entity.ExamSite;
import com.exam.entity.Registration;
import com.exam.mapper.ExamMapper;
import com.exam.mapper.ExamSiteMapper;
import com.exam.mapper.RegistrationMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * RegistrationService 单元测试
 *
 * @author system
 * @since 2024-10-18
 */
@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private RegistrationMapper registrationMapper;

    @Mock
    private ExamMapper examMapper;

    @Mock
    private ExamSiteMapper examSiteMapper;

    @Mock
    private PaymentOrderService paymentOrderService;

    @InjectMocks
    private RegistrationService registrationService;

    private Exam mockExam;
    private ExamSite mockSite;
    private RegistrationRequest mockRequest;

    @BeforeEach
    void setUp() {
        // 初始化模拟数据
        mockExam = new Exam();
        mockExam.setId(1L);
        mockExam.setExamName("2025年成人高考");
        mockExam.setStatus(3); // 报名中
        mockExam.setRegistrationStart(LocalDateTime.now().minusDays(1));
        mockExam.setRegistrationEnd(LocalDateTime.now().plusDays(30));

        mockSite = new ExamSite();
        mockSite.setId(1L);
        mockSite.setSiteName("测试考点");
        mockSite.setCapacity(100);
        mockSite.setCurrentCount(50);

        mockRequest = new RegistrationRequest();
        mockRequest.setUserId(1L);
        mockRequest.setExamId(1L);
        mockRequest.setExamSiteId(1L);
        mockRequest.setIdCard("110101199001011234");
        mockRequest.setPhone("13800138000");
        mockRequest.setSubject("计算机科学与技术");
    }

    @Test
    void testSubmitRegistration_Success() {
        // Mock 数据
        when(examMapper.selectById(1L)).thenReturn(mockExam);
        when(examSiteMapper.selectById(1L)).thenReturn(mockSite);
        when(registrationMapper.selectByUserIdAndExamId(1L, 1L)).thenReturn(null);
        when(registrationMapper.insert(any(Registration.class))).thenReturn(1);

        // 执行测试
        Result result = registrationService.submitRegistration(mockRequest);

        // 验证结果
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertTrue(result.getMessage().contains("成功"));

        // 验证方法调用
        verify(examMapper, times(1)).selectById(1L);
        verify(examSiteMapper, times(1)).selectById(1L);
        verify(registrationMapper, times(1)).insert(any(Registration.class));
    }

    @Test
    void testSubmitRegistration_ExamNotFound() {
        // Mock 考试不存在
        when(examMapper.selectById(1L)).thenReturn(null);

        // 执行测试
        Result result = registrationService.submitRegistration(mockRequest);

        // 验证结果
        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertEquals("考试不存在", result.getMessage());

        // 验证未调用插入方法
        verify(registrationMapper, never()).insert(any(Registration.class));
    }

    @Test
    void testSubmitRegistration_ExamNotOpen() {
        // Mock 考试状态不是报名中
        mockExam.setStatus(1); // 草稿状态
        when(examMapper.selectById(1L)).thenReturn(mockExam);

        // 执行测试
        Result result = registrationService.submitRegistration(mockRequest);

        // 验证结果
        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertTrue(result.getMessage().contains("未开放报名"));
    }

    @Test
    void testSubmitRegistration_RegistrationNotStarted() {
        // Mock 报名未开始
        mockExam.setRegistrationStart(LocalDateTime.now().plusDays(1));
        when(examMapper.selectById(1L)).thenReturn(mockExam);

        // 执行测试
        Result result = registrationService.submitRegistration(mockRequest);

        // 验证结果
        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertEquals("报名尚未开始", result.getMessage());
    }

    @Test
    void testSubmitRegistration_RegistrationEnded() {
        // Mock 报名已结束
        mockExam.setRegistrationEnd(LocalDateTime.now().minusDays(1));
        when(examMapper.selectById(1L)).thenReturn(mockExam);

        // 执行测试
        Result result = registrationService.submitRegistration(mockRequest);

        // 验证结果
        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertEquals("报名已结束", result.getMessage());
    }

    @Test
    void testSubmitRegistration_DuplicateRegistration() {
        // Mock 已存在报名记录
        Registration existingReg = new Registration();
        existingReg.setId(1L);

        when(examMapper.selectById(1L)).thenReturn(mockExam);
        when(registrationMapper.selectByUserIdAndExamId(1L, 1L)).thenReturn(existingReg);

        // 执行测试
        Result result = registrationService.submitRegistration(mockRequest);

        // 验证结果
        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertTrue(result.getMessage().contains("重复报名"));

        // 验证未调用插入方法
        verify(registrationMapper, never()).insert(any(Registration.class));
    }

    @Test
    void testSubmitRegistration_SiteFull() {
        // Mock 考点已满
        mockSite.setCurrentCount(100); // 容量已满

        when(examMapper.selectById(1L)).thenReturn(mockExam);
        when(registrationMapper.selectByUserIdAndExamId(1L, 1L)).thenReturn(null);
        when(examSiteMapper.selectById(1L)).thenReturn(mockSite);

        // 执行测试
        Result result = registrationService.submitRegistration(mockRequest);

        // 验证结果
        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertTrue(result.getMessage().contains("已满"));
    }

    @Test
    void testAuditRegistration_Approve_Success() {
        // Mock 数据
        Registration registration = new Registration();
        registration.setId(1L);
        registration.setAuditStatus(1); // 待审核
        registration.setExamId(1L);

        when(registrationMapper.selectById(1L)).thenReturn(registration);
        when(registrationMapper.updateById(any(Registration.class))).thenReturn(1);
        when(paymentOrderService.createPaymentOrder(1L)).thenReturn(Result.success());

        // 执行测试：审核通过
        Result result = registrationService.auditRegistration(1L, 2, "审核通过", 100L);

        // 验证结果
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertTrue(result.getMessage().contains("通过"));

        // 验证方法调用
        verify(registrationMapper, times(1)).updateById(any(Registration.class));
        verify(paymentOrderService, times(1)).createPaymentOrder(1L);
    }

    @Test
    void testAuditRegistration_Reject_Success() {
        // Mock 数据
        Registration registration = new Registration();
        registration.setId(1L);
        registration.setAuditStatus(1); // 待审核

        when(registrationMapper.selectById(1L)).thenReturn(registration);
        when(registrationMapper.updateById(any(Registration.class))).thenReturn(1);

        // 执行测试：审核驳回
        Result result = registrationService.auditRegistration(1L, 3, "材料不符合要求", 100L);

        // 验证结果
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertTrue(result.getMessage().contains("驳回"));

        // 验证方法调用
        verify(registrationMapper, times(1)).updateById(any(Registration.class));
        // 驳回时不创建支付订单
        verify(paymentOrderService, never()).createPaymentOrder(anyLong());
    }

    @Test
    void testAuditRegistration_AlreadyAudited() {
        // Mock 已审核的报名
        Registration registration = new Registration();
        registration.setId(1L);
        registration.setAuditStatus(2); // 已通过

        when(registrationMapper.selectById(1L)).thenReturn(registration);

        // 执行测试
        Result result = registrationService.auditRegistration(1L, 2, "审核通过", 100L);

        // 验证结果
        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertTrue(result.getMessage().contains("已审核"));

        // 验证未调用更新方法
        verify(registrationMapper, never()).updateById(any(Registration.class));
    }

    @Test
    void testAuditRegistration_RejectWithoutRemark() {
        // Mock 数据
        Registration registration = new Registration();
        registration.setId(1L);
        registration.setAuditStatus(1);

        when(registrationMapper.selectById(1L)).thenReturn(registration);

        // 执行测试：驳回但未填写原因
        Result result = registrationService.auditRegistration(1L, 3, null, 100L);

        // 验证结果
        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertTrue(result.getMessage().contains("驳回时必须填写原因"));
    }

    @Test
    void testCancelRegistration_Success() {
        // Mock 数据
        Registration registration = new Registration();
        registration.setId(1L);
        registration.setUserId(1L);
        registration.setAuditStatus(1); // 待审核

        when(registrationMapper.selectById(1L)).thenReturn(registration);
        when(registrationMapper.deleteById(1L)).thenReturn(1);

        // 执行测试
        Result result = registrationService.cancelRegistration(1L, 1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertTrue(result.getMessage().contains("成功"));

        // 验证方法调用
        verify(registrationMapper, times(1)).deleteById(1L);
    }

    @Test
    void testCancelRegistration_Unauthorized() {
        // Mock 数据
        Registration registration = new Registration();
        registration.setId(1L);
        registration.setUserId(2L); // 不同的用户ID

        when(registrationMapper.selectById(1L)).thenReturn(registration);

        // 执行测试：用户1尝试取消用户2的报名
        Result result = registrationService.cancelRegistration(1L, 1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertTrue(result.getMessage().contains("无权"));

        // 验证未调用删除方法
        verify(registrationMapper, never()).deleteById(anyLong());
    }

    @Test
    void testCancelRegistration_AlreadyAudited() {
        // Mock 已审核的报名
        Registration registration = new Registration();
        registration.setId(1L);
        registration.setUserId(1L);
        registration.setAuditStatus(2); // 已通过

        when(registrationMapper.selectById(1L)).thenReturn(registration);

        // 执行测试
        Result result = registrationService.cancelRegistration(1L, 1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertTrue(result.getMessage().contains("只能取消待审核的报名"));

        // 验证未调用删除方法
        verify(registrationMapper, never()).deleteById(anyLong());
    }
}
