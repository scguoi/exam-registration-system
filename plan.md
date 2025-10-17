# 在线考试报名系统 - 完整实现计划

> **项目状态**: 已完成 70%，剩余 30% 核心功能待实现
> **目标**: 完成所有核心功能，达到毕业设计答辩标准
> **预计工期**: 6-8 天（每天 4-6 小时）

---

## 📊 当前完成度概览

### ✅ 已完成（70%）
- ✅ 项目架构搭建（前后端分离）
- ✅ 数据库设计（6张表 + 索引）
- ✅ Spring Boot 后端基础（39个Java文件）
  - Entity、Mapper、Service（部分）、Controller（部分）
  - Spring Security + JWT 认证
  - 用户管理、考试管理、公告管理、文件上传
- ✅ React 前端基础（20个TypeScript文件）
  - 登录、注册、考生端和管理端布局
  - 路由系统、权限控制、API封装
- ✅ Docker Compose 配置
- ✅ 完整的项目文档（1500+行）

### 🚧 待完成（30%）
- ⏳ 报名审核模块（最重要）
- ⏳ 支付模块（Mock方式）
- ⏳ 数据统计和可视化
- ⏳ 测试和文档完善

---

## 🎯 实现计划（12个任务）

---

## 📅 第一阶段：环境准备（0.5天）

### 任务 1: 启动 MySQL 数据库容器并验证连接

**优先级**: 🔴 高
**预计时间**: 1小时
**负责人**: 开发者

#### 任务目标
确保 MySQL 8.4 容器正常运行，数据库初始化完成，可以正常连接。

#### 执行步骤

```bash
# 1. 启动 MySQL 容器
cd /Users/scguo/OpenSource/exam-registration-system
docker-compose up -d

# 2. 检查容器状态
docker-compose ps

# 3. 查看容器日志
docker-compose logs mysql

# 4. 验证数据库连接
docker-compose exec mysql mysql -uroot -p123456 exam_registration_system

# 5. 检查表结构
SHOW TABLES;

# 6. 检查测试数据
SELECT * FROM sys_user;
SELECT * FROM exam;
SELECT * FROM exam_site;
```

#### 验收标准
- ✓ MySQL 容器状态为 `Up`
- ✓ 可以正常连接数据库
- ✓ 6张表全部存在：`sys_user`, `exam`, `exam_site`, `registration`, `payment_order`, `notice`
- ✓ 测试账号存在：admin、13800138000
- ✓ 测试考试数据存在

#### 可能遇到的问题

**问题1: 端口 3306 被占用**
```bash
# 检查本地 MySQL 服务
brew services list
brew services stop mysql  # 停止本地 MySQL
```

**问题2: 容器启动失败**
```bash
# 完全重置
docker-compose down -v
rm -rf data/mysql
docker-compose up -d
```

**问题3: 无法连接数据库**
- 确保使用 `127.0.0.1` 而不是 `localhost`
- 检查密码是否为 `123456`
- 等待容器完全启动（约10-30秒）

---

## 🔴 第二阶段：报名审核模块（2-3天）

这是整个项目的**核心模块**，必须完整实现。

---

### 任务 2: 实现后端报名审核模块 - Service层

**优先级**: 🔴 高
**预计时间**: 4小时
**文件路径**: `backend/src/main/java/com/exam/service/RegistrationService.java`

#### 任务目标
实现完整的报名业务逻辑，包括报名提交、审核、查询等功能。

#### 核心功能

##### 2.1 考生提交报名
```java
/**
 * 考生提交报名
 * @param request 报名请求
 * @return 报名记录
 */
@Transactional
public Result<Registration> submitRegistration(RegistrationRequest request) {
    // 1. 验证考试是否可报名
    Exam exam = examMapper.selectById(request.getExamId());
    if (!exam.getStatus().equals("registration_open")) {
        return Result.error("该考试暂未开放报名");
    }

    // 2. 检查报名时间
    LocalDateTime now = LocalDateTime.now();
    if (now.isBefore(exam.getRegistrationStart()) || now.isAfter(exam.getRegistrationEnd())) {
        return Result.error("不在报名时间范围内");
    }

    // 3. 检查是否重复报名
    QueryWrapper<Registration> wrapper = new QueryWrapper<>();
    wrapper.eq("exam_id", request.getExamId())
           .eq("user_id", request.getUserId());
    if (registrationMapper.selectCount(wrapper) > 0) {
        return Result.error("您已报名该考试，请勿重复报名");
    }

    // 4. 检查考点容量
    ExamSite site = examSiteMapper.selectById(request.getSiteId());
    Integer currentCount = registrationMapper.countBySiteId(request.getSiteId());
    if (currentCount >= site.getCapacity()) {
        return Result.error("该考点报名人数已满，请选择其他考点");
    }

    // 5. 加密敏感信息（身份证、手机号）
    String encryptedIdCard = AESUtil.encrypt(request.getIdCard());
    String encryptedPhone = AESUtil.encrypt(request.getPhone());

    // 6. 保存报名记录
    Registration registration = new Registration();
    registration.setExamId(request.getExamId());
    registration.setUserId(request.getUserId());
    registration.setSiteId(request.getSiteId());
    registration.setIdCard(encryptedIdCard);
    registration.setPhone(encryptedPhone);
    registration.setStatus("pending");  // 待审核
    registration.setAuditResult("pending");

    registrationMapper.insert(registration);

    return Result.success("报名提交成功，请等待审核", registration);
}
```

##### 2.2 管理员审核报名
```java
/**
 * 管理员审核报名
 * @param id 报名ID
 * @param auditResult 审核结果: approved/rejected
 * @param auditReason 审核原因（驳回时必填）
 * @return 审核结果
 */
@Transactional
public Result auditRegistration(Long id, String auditResult, String auditReason, Long auditorId) {
    // 1. 查询报名记录
    Registration registration = registrationMapper.selectById(id);
    if (registration == null) {
        return Result.error("报名记录不存在");
    }

    // 2. 验证状态（只能审核待审核的记录）
    if (!registration.getStatus().equals("pending")) {
        return Result.error("该报名已审核，无法重复审核");
    }

    // 3. 验证审核结果
    if (!auditResult.equals("approved") && !auditResult.equals("rejected")) {
        return Result.error("审核结果参数错误");
    }

    // 4. 驳回时必须填写原因
    if (auditResult.equals("rejected") && (auditReason == null || auditReason.isEmpty())) {
        return Result.error("驳回时必须填写原因");
    }

    // 5. 更新审核结果
    registration.setStatus(auditResult);
    registration.setAuditResult(auditResult);
    registration.setAuditReason(auditReason);
    registration.setAuditTime(LocalDateTime.now());
    registration.setAuditorId(auditorId);

    registrationMapper.updateById(registration);

    String message = auditResult.equals("approved") ? "审核通过" : "审核驳回";
    return Result.success(message);
}
```

##### 2.3 考生查询自己的报名记录
```java
/**
 * 考生查询自己的报名记录
 * @param userId 用户ID
 * @return 报名列表
 */
public Result<List<RegistrationVO>> getMyRegistrations(Long userId) {
    List<Registration> registrations = registrationMapper.selectByUserId(userId);

    // 转换为VO并解密敏感信息
    List<RegistrationVO> vos = registrations.stream().map(r -> {
        RegistrationVO vo = new RegistrationVO();
        BeanUtils.copyProperties(r, vo);

        // 查询关联的考试信息
        Exam exam = examMapper.selectById(r.getExamId());
        vo.setExamName(exam.getExamName());
        vo.setExamDate(exam.getExamDate());

        // 查询考点信息
        ExamSite site = examSiteMapper.selectById(r.getSiteId());
        vo.setSiteName(site.getSiteName());
        vo.setSiteAddress(site.getAddress());

        return vo;
    }).collect(Collectors.toList());

    return Result.success(vos);
}
```

##### 2.4 管理员查询待审核列表
```java
/**
 * 管理员查询待审核报名列表
 * @param page 页码
 * @param size 每页数量
 * @param examId 考试ID（可选）
 * @return 分页数据
 */
public Result<PageResult<RegistrationVO>> getPendingRegistrations(Integer page, Integer size, Long examId) {
    Page<Registration> pageObj = new Page<>(page, size);

    QueryWrapper<Registration> wrapper = new QueryWrapper<>();
    wrapper.eq("status", "pending");
    if (examId != null) {
        wrapper.eq("exam_id", examId);
    }
    wrapper.orderByAsc("created_at");

    Page<Registration> resultPage = registrationMapper.selectPage(pageObj, wrapper);

    // 转换为VO
    List<RegistrationVO> vos = resultPage.getRecords().stream().map(r -> {
        RegistrationVO vo = new RegistrationVO();
        BeanUtils.copyProperties(r, vo);

        // 查询关联信息
        Exam exam = examMapper.selectById(r.getExamId());
        vo.setExamName(exam.getExamName());

        SysUser user = userMapper.selectById(r.getUserId());
        vo.setUserRealName(user.getRealName());

        // 解密敏感信息供管理员查看
        vo.setIdCard(AESUtil.decrypt(r.getIdCard()));
        vo.setPhone(AESUtil.decrypt(r.getPhone()));

        return vo;
    }).collect(Collectors.toList());

    PageResult<RegistrationVO> pageResult = new PageResult<>(
        resultPage.getTotal(),
        resultPage.getCurrent(),
        resultPage.getSize(),
        vos
    );

    return Result.success(pageResult);
}
```

##### 2.5 考生取消报名
```java
/**
 * 考生取消报名（仅限待审核状态）
 * @param id 报名ID
 * @param userId 用户ID
 * @return 取消结果
 */
@Transactional
public Result cancelRegistration(Long id, Long userId) {
    Registration registration = registrationMapper.selectById(id);

    // 1. 验证权限
    if (!registration.getUserId().equals(userId)) {
        return Result.error("无权操作");
    }

    // 2. 验证状态（只能取消待审核的报名）
    if (!registration.getStatus().equals("pending")) {
        return Result.error("只能取消待审核的报名");
    }

    // 3. 更新状态
    registration.setStatus("cancelled");
    registrationMapper.updateById(registration);

    return Result.success("取消报名成功");
}
```

##### 2.6 获取报名详情
```java
/**
 * 获取报名详情（包含所有关联信息）
 * @param id 报名ID
 * @return 详情信息
 */
public Result<RegistrationDetailVO> getRegistrationDetail(Long id) {
    Registration registration = registrationMapper.selectById(id);
    if (registration == null) {
        return Result.error("报名记录不存在");
    }

    RegistrationDetailVO vo = new RegistrationDetailVO();
    BeanUtils.copyProperties(registration, vo);

    // 查询考试信息
    Exam exam = examMapper.selectById(registration.getExamId());
    vo.setExam(exam);

    // 查询考点信息
    ExamSite site = examSiteMapper.selectById(registration.getSiteId());
    vo.setSite(site);

    // 查询考生信息
    SysUser user = userMapper.selectById(registration.getUserId());
    vo.setUser(user);

    // 解密敏感信息
    vo.setIdCard(AESUtil.decrypt(registration.getIdCard()));
    vo.setPhone(AESUtil.decrypt(registration.getPhone()));

    return Result.success(vo);
}
```

#### 需要创建的DTO和VO

**RegistrationRequest.java** (DTO)
```java
@Data
public class RegistrationRequest {
    @NotNull(message = "考试ID不能为空")
    private Long examId;

    @NotNull(message = "考点ID不能为空")
    private Long siteId;

    @NotBlank(message = "身份证号不能为空")
    @Pattern(regexp = "^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[0-9Xx]$",
             message = "身份证号格式不正确")
    private String idCard;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    private String materialUrl;  // 证明材料URL

    private Long userId;  // 从JWT Token中获取
}
```

**RegistrationVO.java** (VO)
```java
@Data
public class RegistrationVO {
    private Long id;
    private Long examId;
    private String examName;
    private LocalDateTime examDate;
    private Long siteId;
    private String siteName;
    private String siteAddress;
    private String status;
    private String statusText;  // 状态中文描述
    private String auditResult;
    private String auditReason;
    private LocalDateTime auditTime;
    private LocalDateTime createdAt;
    private String userRealName;
    private String phone;  // 脱敏显示
}
```

#### 需要的工具类

**AESUtil.java** - AES加密工具
```java
public class AESUtil {
    private static final String KEY = "ExamSystem2025!@"; // 16位密钥
    private static final String ALGORITHM = "AES";

    public static String encrypt(String content) {
        // AES加密实现
    }

    public static String decrypt(String encryptedContent) {
        // AES解密实现
    }
}
```

#### 验收标准
- ✓ 所有方法编译通过，无语法错误
- ✓ 事务注解正确使用
- ✓ 敏感信息正确加密/解密
- ✓ 业务逻辑完整，边界条件处理完善
- ✓ 错误提示信息友好

---

### 任务 3: 实现后端报名审核模块 - Controller层

**优先级**: 🔴 高
**预计时间**: 2小时
**文件路径**: `backend/src/main/java/com/exam/controller/RegistrationController.java`

#### API接口设计

```java
package com.exam.controller;

import com.exam.common.Result;
import com.exam.dto.RegistrationRequest;
import com.exam.service.RegistrationService;
import com.exam.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/registrations")
public class RegistrationController {

    @Autowired
    private RegistrationService registrationService;

    // ==================== 考生端接口 ====================

    /**
     * 提交报名
     * POST /api/v1/registrations
     */
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public Result submitRegistration(
            @Valid @RequestBody RegistrationRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        request.setUserId(userDetails.getUserId());
        return registrationService.submitRegistration(request);
    }

    /**
     * 查询我的报名记录
     * GET /api/v1/registrations/my
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    public Result getMyRegistrations(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return registrationService.getMyRegistrations(userDetails.getUserId());
    }

    /**
     * 获取报名详情
     * GET /api/v1/registrations/{id}
     */
    @GetMapping("/{id}")
    public Result getRegistrationDetail(@PathVariable Long id) {
        return registrationService.getRegistrationDetail(id);
    }

    /**
     * 取消报名
     * PUT /api/v1/registrations/{id}/cancel
     */
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('USER')")
    public Result cancelRegistration(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return registrationService.cancelRegistration(id, userDetails.getUserId());
    }

    // ==================== 管理员端接口 ====================

    /**
     * 获取待审核报名列表（分页）
     * GET /api/v1/registrations/pending?page=1&size=10&examId=1
     */
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public Result getPendingRegistrations(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long examId) {
        return registrationService.getPendingRegistrations(page, size, examId);
    }

    /**
     * 审核报名（通过/驳回）
     * PUT /api/v1/registrations/{id}/audit
     * Body: { "auditResult": "approved", "auditReason": "符合条件" }
     */
    @PutMapping("/{id}/audit")
    @PreAuthorize("hasRole('ADMIN')")
    public Result auditRegistration(
            @PathVariable Long id,
            @RequestBody AuditRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return registrationService.auditRegistration(
            id,
            request.getAuditResult(),
            request.getAuditReason(),
            userDetails.getUserId()
        );
    }

    /**
     * 获取所有报名列表（管理员查看）
     * GET /api/v1/registrations?page=1&size=10&status=approved
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result getAllRegistrations(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long examId) {
        return registrationService.getAllRegistrations(page, size, status, examId);
    }

    /**
     * 报名统计
     * GET /api/v1/registrations/stats?examId=1
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public Result getRegistrationStats(@RequestParam(required = false) Long examId) {
        return registrationService.getRegistrationStats(examId);
    }
}

// DTO for audit
@Data
class AuditRequest {
    @NotBlank(message = "审核结果不能为空")
    private String auditResult;  // approved / rejected

    private String auditReason;
}
```

#### 验收标准
- ✓ 所有接口路径符合RESTful规范
- ✓ 权限控制正确（`@PreAuthorize`）
- ✓ 参数校验完整（`@Valid`）
- ✓ 从JWT Token中正确获取用户信息
- ✓ 可以通过Postman测试所有接口

---

### 任务 4: 实现前端报名审核页面（管理端）

**优先级**: 🔴 高
**预计时间**: 4小时
**文件路径**: `frontend/src/pages/admin/AuditManagement.tsx`

#### 页面功能

1. **待审核列表页**
   - 表格展示待审核报名
   - 支持按考试筛选
   - 支持搜索考生姓名
   - 查看详情、审核操作

2. **审核详情弹窗**
   - 展示完整的报名信息
   - 考生信息、考试信息、考点信息
   - 审核操作：通过、驳回（需填写原因）

#### 实现代码

```typescript
// frontend/src/pages/admin/AuditManagement.tsx
import React, { useState, useEffect } from 'react';
import {
  Table, Button, Tag, Modal, Form, Input, Select, message,
  Space, Card, Row, Col, Descriptions, Radio
} from 'antd';
import { EyeOutlined, CheckOutlined, CloseOutlined } from '@ant-design/icons';
import { getPendingRegistrations, auditRegistration, getRegistrationDetail } from '@/services/registration';
import type { Registration } from '@/types';

const { TextArea } = Input;
const { Option } = Select;

export default function AuditManagement() {
  const [loading, setLoading] = useState(false);
  const [dataSource, setDataSource] = useState<Registration[]>([]);
  const [pagination, setPagination] = useState({ current: 1, pageSize: 10, total: 0 });
  const [filters, setFilters] = useState({ examId: undefined });

  // 审核弹窗
  const [auditModalVisible, setAuditModalVisible] = useState(false);
  const [currentRecord, setCurrentRecord] = useState<Registration | null>(null);
  const [auditForm] = Form.useForm();

  // 详情弹窗
  const [detailModalVisible, setDetailModalVisible] = useState(false);
  const [detailData, setDetailData] = useState<any>(null);

  // 加载数据
  const loadData = async (page = 1, pageSize = 10) => {
    setLoading(true);
    try {
      const response = await getPendingRegistrations(page, pageSize, filters.examId);
      if (response.success) {
        setDataSource(response.data.records);
        setPagination({
          current: response.data.current,
          pageSize: response.data.size,
          total: response.data.total
        });
      }
    } catch (error) {
      message.error('加载数据失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [filters]);

  // 查看详情
  const handleViewDetail = async (record: Registration) => {
    try {
      const response = await getRegistrationDetail(record.id);
      if (response.success) {
        setDetailData(response.data);
        setDetailModalVisible(true);
      }
    } catch (error) {
      message.error('获取详情失败');
    }
  };

  // 打开审核弹窗
  const handleAudit = (record: Registration) => {
    setCurrentRecord(record);
    setAuditModalVisible(true);
    auditForm.resetFields();
  };

  // 提交审核
  const handleSubmitAudit = async () => {
    try {
      const values = await auditForm.validateFields();

      if (values.auditResult === 'rejected' && !values.auditReason) {
        message.error('驳回时必须填写原因');
        return;
      }

      const response = await auditRegistration(currentRecord!.id, {
        auditResult: values.auditResult,
        auditReason: values.auditReason
      });

      if (response.success) {
        message.success(values.auditResult === 'approved' ? '审核通过' : '已驳回');
        setAuditModalVisible(false);
        loadData(pagination.current, pagination.pageSize);
      } else {
        message.error(response.message);
      }
    } catch (error) {
      message.error('审核失败');
    }
  };

  // 表格列定义
  const columns = [
    {
      title: 'ID',
      dataIndex: 'id',
      width: 80,
    },
    {
      title: '考生姓名',
      dataIndex: 'userRealName',
      width: 120,
    },
    {
      title: '手机号',
      dataIndex: 'phone',
      width: 130,
      render: (phone: string) => phone?.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2') // 脱敏
    },
    {
      title: '考试名称',
      dataIndex: 'examName',
      width: 200,
    },
    {
      title: '考点',
      dataIndex: 'siteName',
      width: 150,
    },
    {
      title: '报名时间',
      dataIndex: 'createdAt',
      width: 180,
    },
    {
      title: '状态',
      dataIndex: 'status',
      width: 100,
      render: (status: string) => {
        const statusMap: any = {
          pending: { text: '待审核', color: 'orange' },
          approved: { text: '已通过', color: 'green' },
          rejected: { text: '已驳回', color: 'red' },
          cancelled: { text: '已取消', color: 'gray' }
        };
        const config = statusMap[status] || { text: status, color: 'default' };
        return <Tag color={config.color}>{config.text}</Tag>;
      }
    },
    {
      title: '操作',
      width: 200,
      fixed: 'right' as const,
      render: (text: any, record: Registration) => (
        <Space>
          <Button
            type="link"
            icon={<EyeOutlined />}
            onClick={() => handleViewDetail(record)}
          >
            查看
          </Button>
          <Button
            type="primary"
            size="small"
            icon={<CheckOutlined />}
            onClick={() => handleAudit(record)}
          >
            审核
          </Button>
        </Space>
      )
    }
  ];

  return (
    <div>
      <Card title="报名审核管理" extra={
        <Space>
          <Select
            placeholder="选择考试"
            style={{ width: 200 }}
            allowClear
            onChange={(value) => setFilters({ examId: value })}
          >
            {/* 这里应该从API获取考试列表 */}
            <Option value={1}>2025年成人高考</Option>
          </Select>
          <Button onClick={() => loadData(1, pagination.pageSize)}>刷新</Button>
        </Space>
      }>
        <Table
          columns={columns}
          dataSource={dataSource}
          loading={loading}
          rowKey="id"
          pagination={pagination}
          onChange={(page) => loadData(page.current, page.pageSize)}
          scroll={{ x: 1200 }}
        />
      </Card>

      {/* 审核弹窗 */}
      <Modal
        title="审核报名"
        open={auditModalVisible}
        onOk={handleSubmitAudit}
        onCancel={() => setAuditModalVisible(false)}
        width={500}
      >
        <Form form={auditForm} layout="vertical">
          <Form.Item
            name="auditResult"
            label="审核结果"
            rules={[{ required: true, message: '请选择审核结果' }]}
          >
            <Radio.Group>
              <Radio value="approved">通过</Radio>
              <Radio value="rejected">驳回</Radio>
            </Radio.Group>
          </Form.Item>

          <Form.Item
            noStyle
            shouldUpdate={(prevValues, currentValues) =>
              prevValues.auditResult !== currentValues.auditResult
            }
          >
            {({ getFieldValue }) =>
              getFieldValue('auditResult') === 'rejected' ? (
                <Form.Item
                  name="auditReason"
                  label="驳回原因"
                  rules={[{ required: true, message: '请填写驳回原因' }]}
                >
                  <TextArea rows={4} placeholder="请详细说明驳回原因" />
                </Form.Item>
              ) : null
            }
          </Form.Item>
        </Form>
      </Modal>

      {/* 详情弹窗 */}
      <Modal
        title="报名详情"
        open={detailModalVisible}
        onCancel={() => setDetailModalVisible(false)}
        footer={null}
        width={800}
      >
        {detailData && (
          <Descriptions bordered column={2}>
            <Descriptions.Item label="考生姓名" span={1}>
              {detailData.user?.realName}
            </Descriptions.Item>
            <Descriptions.Item label="手机号" span={1}>
              {detailData.phone}
            </Descriptions.Item>
            <Descriptions.Item label="身份证号" span={2}>
              {detailData.idCard}
            </Descriptions.Item>
            <Descriptions.Item label="考试名称" span={2}>
              {detailData.exam?.examName}
            </Descriptions.Item>
            <Descriptions.Item label="考试时间" span={2}>
              {detailData.exam?.examDate}
            </Descriptions.Item>
            <Descriptions.Item label="考点名称" span={2}>
              {detailData.site?.siteName}
            </Descriptions.Item>
            <Descriptions.Item label="考点地址" span={2}>
              {detailData.site?.address}
            </Descriptions.Item>
            <Descriptions.Item label="报名时间" span={2}>
              {detailData.createdAt}
            </Descriptions.Item>
          </Descriptions>
        )}
      </Modal>
    </div>
  );
}
```

#### API Service封装

```typescript
// frontend/src/services/registration.ts
import api from './api';

export interface RegistrationRequest {
  examId: number;
  siteId: number;
  idCard: string;
  phone: string;
  materialUrl?: string;
}

export interface AuditRequest {
  auditResult: 'approved' | 'rejected';
  auditReason?: string;
}

// 提交报名
export const submitRegistration = (data: RegistrationRequest) =>
  api.post('/registrations', data);

// 我的报名列表
export const getMyRegistrations = () =>
  api.get('/registrations/my');

// 待审核列表
export const getPendingRegistrations = (page: number, size: number, examId?: number) =>
  api.get('/registrations/pending', { params: { page, size, examId } });

// 审核报名
export const auditRegistration = (id: number, data: AuditRequest) =>
  api.put(`/registrations/${id}/audit`, data);

// 报名详情
export const getRegistrationDetail = (id: number) =>
  api.get(`/registrations/${id}`);

// 取消报名
export const cancelRegistration = (id: number) =>
  api.put(`/registrations/${id}/cancel`);
```

#### 路由配置

```typescript
// frontend/src/App.tsx
import AuditManagement from './pages/admin/AuditManagement';

// 在管理端路由中添加
{
  path: '/admin/audit',
  element: <AuditManagement />
}
```

#### 验收标准
- ✓ 页面可以正常显示待审核列表
- ✓ 筛选功能正常工作
- ✓ 查看详情弹窗正常显示
- ✓ 审核功能正常（通过/驳回）
- ✓ 审核后列表自动刷新
- ✓ 错误提示友好

---

### 任务 5: 实现考生报名流程页面（前端）

**优先级**: 🔴 高
**预计时间**: 4小时
**文件路径**: `frontend/src/pages/user/ApplyExam.tsx`

#### 页面流程
1. 选择考试
2. 选择考点
3. 填写个人信息
4. 确认提交

#### 实现代码

```typescript
// frontend/src/pages/user/ApplyExam.tsx
import React, { useState, useEffect } from 'react';
import {
  Steps, Card, Form, Input, Select, Button, message,
  Descriptions, Space, Cascader
} from 'antd';
import { useNavigate, useParams } from 'react-router-dom';
import { submitRegistration } from '@/services/registration';
import { getExamDetail, getExamSites } from '@/services/exam';

const { Step } = Steps;
const { Option } = Select;

export default function ApplyExam() {
  const navigate = useNavigate();
  const { examId } = useParams<{ examId: string }>();

  const [current, setCurrent] = useState(0);
  const [form] = Form.useForm();

  const [exam, setExam] = useState<any>(null);
  const [sites, setSites] = useState<any[]>([]);
  const [selectedSite, setSelectedSite] = useState<any>(null);
  const [submitting, setSubmitting] = useState(false);

  // 加载考试信息
  useEffect(() => {
    loadExamInfo();
    loadSites();
  }, [examId]);

  const loadExamInfo = async () => {
    const response = await getExamDetail(Number(examId));
    if (response.success) {
      setExam(response.data);
    }
  };

  const loadSites = async () => {
    const response = await getExamSites(Number(examId));
    if (response.success) {
      setSites(response.data);
    }
  };

  // 提交报名
  const handleSubmit = async () => {
    try {
      setSubmitting(true);
      const values = await form.validateFields();

      const response = await submitRegistration({
        examId: Number(examId),
        siteId: values.siteId,
        idCard: values.idCard,
        phone: values.phone,
        materialUrl: values.materialUrl
      });

      if (response.success) {
        message.success('报名成功，请等待审核！');
        navigate('/user/registrations');
      } else {
        message.error(response.message);
      }
    } catch (error) {
      message.error('报名失败');
    } finally {
      setSubmitting(false);
    }
  };

  const steps = [
    {
      title: '考试信息',
      content: (
        <Card title="考试详情">
          {exam && (
            <Descriptions bordered column={2}>
              <Descriptions.Item label="考试名称" span={2}>
                {exam.examName}
              </Descriptions.Item>
              <Descriptions.Item label="考试类型" span={1}>
                {exam.examType}
              </Descriptions.Item>
              <Descriptions.Item label="考试费用" span={1}>
                ¥{exam.fee}
              </Descriptions.Item>
              <Descriptions.Item label="考试时间" span={2}>
                {exam.examDate}
              </Descriptions.Item>
              <Descriptions.Item label="报名时间" span={2}>
                {exam.registrationStart} 至 {exam.registrationEnd}
              </Descriptions.Item>
            </Descriptions>
          )}
        </Card>
      )
    },
    {
      title: '选择考点',
      content: (
        <Card title="选择考点">
          <Form form={form} layout="vertical">
            <Form.Item
              name="siteId"
              label="考点"
              rules={[{ required: true, message: '请选择考点' }]}
            >
              <Select
                placeholder="请选择考点"
                onChange={(value) => {
                  const site = sites.find(s => s.id === value);
                  setSelectedSite(site);
                }}
              >
                {sites.map(site => (
                  <Option key={site.id} value={site.id}>
                    {site.siteName} - {site.address} (剩余名额: {site.capacity - site.currentCount})
                  </Option>
                ))}
              </Select>
            </Form.Item>
          </Form>
        </Card>
      )
    },
    {
      title: '填写信息',
      content: (
        <Card title="个人信息">
          <Form form={form} layout="vertical">
            <Form.Item
              name="idCard"
              label="身份证号"
              rules={[
                { required: true, message: '请输入身份证号' },
                {
                  pattern: /^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[0-9Xx]$/,
                  message: '身份证号格式不正确'
                }
              ]}
            >
              <Input placeholder="请输入18位身份证号" />
            </Form.Item>

            <Form.Item
              name="phone"
              label="手机号"
              rules={[
                { required: true, message: '请输入手机号' },
                { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确' }
              ]}
            >
              <Input placeholder="请输入11位手机号" />
            </Form.Item>
          </Form>
        </Card>
      )
    },
    {
      title: '确认提交',
      content: (
        <Card title="确认报名信息">
          <Descriptions bordered column={1}>
            <Descriptions.Item label="考试名称">{exam?.examName}</Descriptions.Item>
            <Descriptions.Item label="考点">{selectedSite?.siteName}</Descriptions.Item>
            <Descriptions.Item label="考点地址">{selectedSite?.address}</Descriptions.Item>
            <Descriptions.Item label="身份证号">{form.getFieldValue('idCard')}</Descriptions.Item>
            <Descriptions.Item label="手机号">{form.getFieldValue('phone')}</Descriptions.Item>
            <Descriptions.Item label="考试费用">¥{exam?.fee}</Descriptions.Item>
          </Descriptions>
        </Card>
      )
    }
  ];

  return (
    <div>
      <Card>
        <Steps current={current}>
          {steps.map(item => (
            <Step key={item.title} title={item.title} />
          ))}
        </Steps>

        <div style={{ marginTop: 24 }}>
          {steps[current].content}
        </div>

        <div style={{ marginTop: 24, textAlign: 'right' }}>
          <Space>
            {current > 0 && (
              <Button onClick={() => setCurrent(current - 1)}>上一步</Button>
            )}
            {current < steps.length - 1 && (
              <Button type="primary" onClick={() => setCurrent(current + 1)}>
                下一步
              </Button>
            )}
            {current === steps.length - 1 && (
              <Button type="primary" onClick={handleSubmit} loading={submitting}>
                提交报名
              </Button>
            )}
          </Space>
        </div>
      </Card>
    </div>
  );
}
```

#### 验收标准
- ✓ 步骤条正常切换
- ✓ 表单验证正常工作
- ✓ 考点列表正常显示
- ✓ 提交报名成功后跳转到我的报名页面
- ✓ 错误提示友好

---

## 💳 第三阶段：支付模块（1-2天）

### 任务 6: 实现后端支付订单模块

**优先级**: 🔴 高
**预计时间**: 3小时
**实现方式**: **Mock 支付（不对接真实支付接口）**

#### 文件清单
- `backend/src/main/java/com/exam/service/PaymentService.java`
- `backend/src/main/java/com/exam/controller/PaymentController.java`

#### PaymentService实现

```java
package com.exam.service;

import com.exam.entity.PaymentOrder;
import com.exam.entity.Registration;
import com.exam.mapper.PaymentOrderMapper;
import com.exam.mapper.RegistrationMapper;
import com.exam.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class PaymentService {

    @Autowired
    private PaymentOrderMapper paymentOrderMapper;

    @Autowired
    private RegistrationMapper registrationMapper;

    /**
     * 创建支付订单
     */
    @Transactional
    public Result<PaymentOrder> createOrder(Long registrationId) {
        // 1. 查询报名记录
        Registration registration = registrationMapper.selectById(registrationId);
        if (registration == null) {
            return Result.error("报名记录不存在");
        }

        // 2. 验证状态（只有审核通过的才能支付）
        if (!registration.getStatus().equals("approved")) {
            return Result.error("报名未审核通过，无法支付");
        }

        // 3. 检查是否已支付
        PaymentOrder existingOrder = paymentOrderMapper.selectByRegistrationId(registrationId);
        if (existingOrder != null && existingOrder.getStatus().equals("paid")) {
            return Result.error("该报名已支付，请勿重复支付");
        }

        // 4. 创建订单
        PaymentOrder order = new PaymentOrder();
        order.setRegistrationId(registrationId);
        order.setOrderNo(generateOrderNo());
        order.setAmount(registration.getExam().getFee());  // 从考试信息获取金额
        order.setStatus("unpaid");
        order.setPaymentMethod("mock");

        paymentOrderMapper.insert(order);

        return Result.success("订单创建成功", order);
    }

    /**
     * 模拟支付（直接标记为已支付）
     */
    @Transactional
    public Result mockPayment(String orderNo) {
        // 1. 查询订单
        PaymentOrder order = paymentOrderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            return Result.error("订单不存在");
        }

        // 2. 验证状态
        if (order.getStatus().equals("paid")) {
            return Result.error("订单已支付，请勿重复支付");
        }

        // 3. 更新订单状态为已支付
        order.setStatus("paid");
        order.setPaidAt(LocalDateTime.now());
        paymentOrderMapper.updateById(order);

        // 4. 更新报名记录状态
        Registration registration = registrationMapper.selectById(order.getRegistrationId());
        registration.setPaymentStatus("paid");
        registrationMapper.updateById(registration);

        return Result.success("支付成功", order);
    }

    /**
     * 查询订单状态
     */
    public Result<PaymentOrder> queryOrder(String orderNo) {
        PaymentOrder order = paymentOrderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            return Result.error("订单不存在");
        }
        return Result.success(order);
    }

    /**
     * 查询用户的所有订单
     */
    public Result getMyOrders(Long userId) {
        List<PaymentOrder> orders = paymentOrderMapper.selectByUserId(userId);
        return Result.success(orders);
    }

    /**
     * 生成订单号: PAY + 时间戳 + 4位随机数
     */
    private String generateOrderNo() {
        long timestamp = System.currentTimeMillis();
        int random = (int)(Math.random() * 9000) + 1000;
        return "PAY" + timestamp + random;
    }
}
```

#### PaymentController实现

```java
package com.exam.controller;

import com.exam.common.Result;
import com.exam.service.PaymentService;
import com.exam.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    /**
     * 创建支付订单
     * POST /api/v1/payments/create
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('USER')")
    public Result createOrder(@RequestParam Long registrationId) {
        return paymentService.createOrder(registrationId);
    }

    /**
     * 模拟支付（点击即完成）
     * POST /api/v1/payments/{orderNo}/pay
     */
    @PostMapping("/{orderNo}/pay")
    @PreAuthorize("hasRole('USER')")
    public Result mockPayment(@PathVariable String orderNo) {
        return paymentService.mockPayment(orderNo);
    }

    /**
     * 查询订单状态
     * GET /api/v1/payments/{orderNo}
     */
    @GetMapping("/{orderNo}")
    public Result queryOrder(@PathVariable String orderNo) {
        return paymentService.queryOrder(orderNo);
    }

    /**
     * 我的订单列表
     * GET /api/v1/payments/my
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    public Result getMyOrders(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return paymentService.getMyOrders(userDetails.getUserId());
    }
}
```

#### 需要补充的Mapper方法

```java
// PaymentOrderMapper.java
PaymentOrder selectByOrderNo(String orderNo);
PaymentOrder selectByRegistrationId(Long registrationId);
List<PaymentOrder> selectByUserId(Long userId);
```

#### 验收标准
- ✓ 创建订单成功
- ✓ 模拟支付直接成功（无需对接真实支付）
- ✓ 订单状态正确更新
- ✓ 报名状态同步更新
- ✓ 事务控制正确

---

### 任务 7: 实现前端支付页面和支付状态查询

**优先级**: 🔴 高
**预计时间**: 3小时

#### 页面清单
1. 支付确认页 (`/user/payment/:orderId`)
2. 我的订单页 (`/user/orders`)

#### 支付确认页实现

```typescript
// frontend/src/pages/user/Payment.tsx
import React, { useState, useEffect } from 'react';
import { Card, Descriptions, Button, Result, message, Spin } from 'antd';
import { useParams, useNavigate } from 'react-router-dom';
import { createOrder, mockPayment, queryOrder } from '@/services/payment';
import { getRegistrationDetail } from '@/services/registration';

export default function Payment() {
  const { registrationId } = useParams<{ registrationId: string }>();
  const navigate = useNavigate();

  const [loading, setLoading] = useState(false);
  const [paying, setPaying] = useState(false);
  const [registration, setRegistration] = useState<any>(null);
  const [order, setOrder] = useState<any>(null);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    setLoading(true);
    try {
      // 获取报名详情
      const regResponse = await getRegistrationDetail(Number(registrationId));
      if (regResponse.success) {
        setRegistration(regResponse.data);
      }

      // 创建订单
      const orderResponse = await createOrder(Number(registrationId));
      if (orderResponse.success) {
        setOrder(orderResponse.data);
      } else {
        message.error(orderResponse.message);
      }
    } catch (error) {
      message.error('加载失败');
    } finally {
      setLoading(false);
    }
  };

  // 模拟支付（点击即完成）
  const handleMockPay = async () => {
    setPaying(true);
    try {
      // 模拟加载效果
      await new Promise(resolve => setTimeout(resolve, 1000));

      const response = await mockPayment(order.orderNo);

      if (response.success) {
        message.success('支付成功！');
        // 跳转到支付结果页
        navigate('/user/payment/result?status=success&orderNo=' + order.orderNo);
      } else {
        message.error(response.message);
      }
    } catch (error) {
      message.error('支付失败');
    } finally {
      setPaying(false);
    }
  };

  if (loading) {
    return <Spin size="large" style={{ display: 'block', margin: '100px auto' }} />;
  }

  return (
    <div style={{ maxWidth: 800, margin: '0 auto' }}>
      <Card title="支付确认">
        <Descriptions bordered column={1}>
          <Descriptions.Item label="订单号">{order?.orderNo}</Descriptions.Item>
          <Descriptions.Item label="考试名称">{registration?.exam?.examName}</Descriptions.Item>
          <Descriptions.Item label="考点">{registration?.site?.siteName}</Descriptions.Item>
          <Descriptions.Item label="考点地址">{registration?.site?.address}</Descriptions.Item>
          <Descriptions.Item label="考试时间">{registration?.exam?.examDate}</Descriptions.Item>
          <Descriptions.Item label="应付金额">
            <span style={{ fontSize: 24, color: '#ff4d4f', fontWeight: 'bold' }}>
              ¥{order?.amount}
            </span>
          </Descriptions.Item>
        </Descriptions>

        <div style={{ marginTop: 24, textAlign: 'center' }}>
          <Button
            type="primary"
            size="large"
            onClick={handleMockPay}
            loading={paying}
            style={{ width: 200 }}
          >
            {paying ? '支付处理中...' : '确认支付（模拟）'}
          </Button>
          <div style={{ marginTop: 12, color: '#999' }}>
            此为模拟支付，点击按钮即完成支付
          </div>
        </div>
      </Card>
    </div>
  );
}
```

#### 支付结果页

```typescript
// frontend/src/pages/user/PaymentResult.tsx
import React from 'react';
import { Result, Button } from 'antd';
import { useNavigate, useSearchParams } from 'react-router-dom';

export default function PaymentResult() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();

  const status = searchParams.get('status');
  const orderNo = searchParams.get('orderNo');

  return (
    <div style={{ maxWidth: 600, margin: '50px auto' }}>
      {status === 'success' ? (
        <Result
          status="success"
          title="支付成功！"
          subTitle={`订单号：${orderNo}，您已成功完成支付，请等待审核结果。`}
          extra={[
            <Button type="primary" key="orders" onClick={() => navigate('/user/orders')}>
              查看订单
            </Button>,
            <Button key="home" onClick={() => navigate('/user/dashboard')}>
              返回首页
            </Button>
          ]}
        />
      ) : (
        <Result
          status="error"
          title="支付失败"
          subTitle="请重试或联系客服"
          extra={[
            <Button type="primary" key="retry" onClick={() => navigate(-1)}>
              重新支付
            </Button>,
            <Button key="home" onClick={() => navigate('/user/dashboard')}>
              返回首页
            </Button>
          ]}
        />
      )}
    </div>
  );
}
```

#### 我的订单页

```typescript
// frontend/src/pages/user/MyOrders.tsx
import React, { useState, useEffect } from 'react';
import { Table, Tag, Card, Button, message } from 'antd';
import { getMyOrders } from '@/services/payment';

export default function MyOrders() {
  const [loading, setLoading] = useState(false);
  const [dataSource, setDataSource] = useState([]);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    setLoading(true);
    try {
      const response = await getMyOrders();
      if (response.success) {
        setDataSource(response.data);
      }
    } catch (error) {
      message.error('加载失败');
    } finally {
      setLoading(false);
    }
  };

  const columns = [
    { title: '订单号', dataIndex: 'orderNo', width: 200 },
    { title: '考试名称', dataIndex: ['registration', 'exam', 'examName'], width: 200 },
    { title: '金额', dataIndex: 'amount', width: 100, render: (val: number) => `¥${val}` },
    {
      title: '状态',
      dataIndex: 'status',
      width: 100,
      render: (status: string) => {
        const map: any = {
          unpaid: { text: '未支付', color: 'orange' },
          paid: { text: '已支付', color: 'green' },
          refunded: { text: '已退款', color: 'gray' }
        };
        return <Tag color={map[status]?.color}>{map[status]?.text}</Tag>;
      }
    },
    { title: '支付时间', dataIndex: 'paidAt', width: 180 },
    { title: '创建时间', dataIndex: 'createdAt', width: 180 }
  ];

  return (
    <Card title="我的订单">
      <Table
        columns={columns}
        dataSource={dataSource}
        loading={loading}
        rowKey="id"
      />
    </Card>
  );
}
```

#### API Service封装

```typescript
// frontend/src/services/payment.ts
import api from './api';

// 创建订单
export const createOrder = (registrationId: number) =>
  api.post('/payments/create', null, { params: { registrationId } });

// 模拟支付
export const mockPayment = (orderNo: string) =>
  api.post(`/payments/${orderNo}/pay`);

// 查询订单
export const queryOrder = (orderNo: string) =>
  api.get(`/payments/${orderNo}`);

// 我的订单列表
export const getMyOrders = () =>
  api.get('/payments/my');
```

#### 验收标准
- ✓ 支付确认页正常显示订单信息
- ✓ 点击支付按钮后立即成功
- ✓ 支付成功后跳转到结果页
- ✓ 我的订单页正常显示订单列表
- ✓ 订单状态正确显示

---

## 📊 第四阶段：数据统计（1-2天）

### 任务 8: 实现后端数据统计API

**优先级**: 🟡 中
**预计时间**: 3小时

#### 统计维度
1. 总览数据（考试、报名、收入）
2. 报名趋势（月度）
3. 考试类型分布
4. 地区分布

#### StatisticsService实现

```java
package com.exam.service;

import com.exam.mapper.*;
import com.exam.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class StatisticsService {

    @Autowired
    private ExamMapper examMapper;

    @Autowired
    private RegistrationMapper registrationMapper;

    @Autowired
    private PaymentOrderMapper paymentOrderMapper;

    /**
     * 总览数据
     */
    public Result getOverview() {
        Map<String, Object> data = new HashMap<>();

        // 考试统计
        data.put("totalExams", examMapper.selectCount(null));
        data.put("activeExams", examMapper.countByStatus("registration_open"));

        // 报名统计
        data.put("totalRegistrations", registrationMapper.selectCount(null));
        data.put("pendingAudit", registrationMapper.countByStatus("pending"));
        data.put("approved", registrationMapper.countByStatus("approved"));

        // 支付统计
        data.put("totalRevenue", paymentOrderMapper.sumAmount("paid"));
        data.put("todayRevenue", paymentOrderMapper.sumAmountToday());
        data.put("monthRevenue", paymentOrderMapper.sumAmountThisMonth());

        return Result.success(data);
    }

    /**
     * 报名趋势（按月）
     */
    public Result getRegistrationTrend() {
        List<Map<String, Object>> trend = registrationMapper.selectMonthlyTrend();
        return Result.success(trend);
    }

    /**
     * 考试类型分布
     */
    public Result getExamTypeDistribution() {
        List<Map<String, Object>> distribution = examMapper.selectTypeDistribution();
        return Result.success(distribution);
    }

    /**
     * 地区分布
     */
    public Result getProvinceDistribution() {
        List<Map<String, Object>> distribution = examSiteMapper.selectProvinceDistribution();
        return Result.success(distribution);
    }
}
```

#### StatisticsController实现

```java
@RestController
@RequestMapping("/api/v1/statistics")
@PreAuthorize("hasRole('ADMIN')")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping("/overview")
    public Result getOverview() {
        return statisticsService.getOverview();
    }

    @GetMapping("/registration-trend")
    public Result getRegistrationTrend() {
        return statisticsService.getRegistrationTrend();
    }

    @GetMapping("/exam-type")
    public Result getExamTypeDistribution() {
        return statisticsService.getExamTypeDistribution();
    }

    @GetMapping("/province")
    public Result getProvinceDistribution() {
        return statisticsService.getProvinceDistribution();
    }
}
```

#### 需要的SQL查询（在Mapper.xml中实现）

```xml
<!-- RegistrationMapper.xml -->
<select id="selectMonthlyTrend" resultType="map">
    SELECT
        DATE_FORMAT(created_at, '%Y-%m') as month,
        COUNT(*) as count
    FROM registration
    WHERE created_at >= DATE_SUB(NOW(), INTERVAL 12 MONTH)
    GROUP BY DATE_FORMAT(created_at, '%Y-%m')
    ORDER BY month
</select>

<!-- ExamMapper.xml -->
<select id="selectTypeDistribution" resultType="map">
    SELECT
        exam_type as type,
        COUNT(*) as count
    FROM exam
    GROUP BY exam_type
</select>

<!-- ExamSiteMapper.xml -->
<select id="selectProvinceDistribution" resultType="map">
    SELECT
        province,
        COUNT(*) as count
    FROM exam_site
    GROUP BY province
    ORDER BY count DESC
    LIMIT 10
</select>
```

#### 验收标准
- ✓ 所有统计接口正常返回数据
- ✓ SQL查询正确
- ✓ 数据格式符合前端需求

---

### 任务 9: 实现前端管理端数据可视化

**优先级**: 🟡 中
**预计时间**: 4小时
**文件路径**: `frontend/src/pages/admin/Dashboard.tsx`

#### 使用ECharts实现数据可视化

```typescript
// frontend/src/pages/admin/Dashboard.tsx
import React, { useState, useEffect } from 'react';
import { Card, Row, Col, Statistic, Table } from 'antd';
import ReactECharts from 'echarts-for-react';
import {
  getOverview,
  getRegistrationTrend,
  getExamTypeDistribution,
  getProvinceDistribution
} from '@/services/statistics';

export default function AdminDashboard() {
  const [overview, setOverview] = useState<any>({});
  const [trendData, setTrendData] = useState<any[]>([]);
  const [typeData, setTypeData] = useState<any[]>([]);
  const [provinceData, setProvinceData] = useState<any[]>([]);

  useEffect(() => {
    loadAllData();
  }, []);

  const loadAllData = async () => {
    const [overviewRes, trendRes, typeRes, provinceRes] = await Promise.all([
      getOverview(),
      getRegistrationTrend(),
      getExamTypeDistribution(),
      getProvinceDistribution()
    ]);

    if (overviewRes.success) setOverview(overviewRes.data);
    if (trendRes.success) setTrendData(trendRes.data);
    if (typeRes.success) setTypeData(typeRes.data);
    if (provinceRes.success) setProvinceData(provinceRes.data);
  };

  // 报名趋势折线图配置
  const trendOption = {
    title: { text: '报名趋势（月度）' },
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: trendData.map(item => item.month)
    },
    yAxis: { type: 'value' },
    series: [{
      data: trendData.map(item => item.count),
      type: 'line',
      smooth: true,
      areaStyle: {}
    }]
  };

  // 考试类型饼图配置
  const typeOption = {
    title: { text: '考试类型分布' },
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie',
      radius: '50%',
      data: typeData.map(item => ({ name: item.type, value: item.count }))
    }]
  };

  // 地区分布柱状图配置
  const provinceOption = {
    title: { text: '地区分布（Top 10）' },
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: provinceData.map(item => item.province)
    },
    yAxis: { type: 'value' },
    series: [{
      data: provinceData.map(item => item.count),
      type: 'bar'
    }]
  };

  return (
    <div>
      {/* 数据卡片 */}
      <Row gutter={16}>
        <Col span={6}>
          <Card>
            <Statistic title="总考试数" value={overview.totalExams} />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic title="总报名数" value={overview.totalRegistrations} />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic title="待审核" value={overview.pendingAudit} valueStyle={{ color: '#faad14' }} />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="总收入"
              value={overview.totalRevenue}
              prefix="¥"
              valueStyle={{ color: '#3f8600' }}
            />
          </Card>
        </Col>
      </Row>

      {/* 图表 */}
      <Row gutter={16} style={{ marginTop: 16 }}>
        <Col span={24}>
          <Card>
            <ReactECharts option={trendOption} style={{ height: 300 }} />
          </Card>
        </Col>
      </Row>

      <Row gutter={16} style={{ marginTop: 16 }}>
        <Col span={12}>
          <Card>
            <ReactECharts option={typeOption} style={{ height: 300 }} />
          </Card>
        </Col>
        <Col span={12}>
          <Card>
            <ReactECharts option={provinceOption} style={{ height: 300 }} />
          </Card>
        </Col>
      </Row>
    </div>
  );
}
```

#### API Service封装

```typescript
// frontend/src/services/statistics.ts
import api from './api';

export const getOverview = () => api.get('/statistics/overview');
export const getRegistrationTrend = () => api.get('/statistics/registration-trend');
export const getExamTypeDistribution = () => api.get('/statistics/exam-type');
export const getProvinceDistribution = () => api.get('/statistics/province');
```

#### 验收标准
- ✓ 数据卡片正常显示
- ✓ 图表正常渲染
- ✓ 数据实时更新
- ✓ 响应式布局

---

## 🧪 第五阶段：测试与文档（1-2天）

### 任务 10: 前后端联调测试

**优先级**: 🔴 高
**预计时间**: 3小时

#### 测试流程

1. **启动后端服务**
```bash
cd backend
mvn spring-boot:run
# 或
java -jar target/exam-registration-system-0.0.1-SNAPSHOT.jar
```

2. **启动前端服务**
```bash
cd frontend
npm run dev
```

3. **完整流程测试**

##### 考生端流程
```
1. 注册账号 → 2. 登录 → 3. 浏览考试 → 4. 报名 →
5. 等待审核 → 6. 支付 → 7. 查看订单
```

##### 管理端流程
```
1. 管理员登录 → 2. 查看待审核列表 → 3. 审核报名 →
4. 查看数据统计 → 5. 发布公告
```

#### 测试用例清单

| 功能模块 | 测试场景 | 预期结果 |
|---------|---------|---------|
| 用户注册 | 正常注册 | 成功 |
| 用户登录 | 正确账号密码 | 登录成功，获取Token |
| 报名提交 | 选择考试和考点 | 提交成功，状态为待审核 |
| 重复报名 | 同一考试重复报名 | 提示已报名 |
| 考点容量 | 选择已满考点 | 提示人数已满 |
| 报名审核 | 管理员审核通过 | 状态变为已通过 |
| 报名驳回 | 管理员驳回并填写原因 | 状态变为已驳回 |
| 支付 | 点击模拟支付 | 立即支付成功 |
| 重复支付 | 已支付订单再次支付 | 提示已支付 |
| 数据统计 | 查看管理端统计 | 图表正常显示 |

#### 验收标准
- ✓ 所有核心流程可以走通
- ✓ 无严重Bug
- ✓ 错误提示友好
- ✓ 用户体验流畅

---

### 任务 11: 编写核心模块单元测试

**优先级**: 🟡 中
**预计时间**: 3小时

#### 测试框架
- JUnit 5
- Mockito
- Spring Boot Test

#### 测试文件清单

```java
// src/test/java/com/exam/service/RegistrationServiceTest.java
@SpringBootTest
public class RegistrationServiceTest {

    @Autowired
    private RegistrationService registrationService;

    @Test
    public void testSubmitRegistration_Success() {
        // 测试正常报名
    }

    @Test
    public void testSubmitRegistration_DuplicateRegistration() {
        // 测试重复报名
    }

    @Test
    public void testSubmitRegistration_FullCapacity() {
        // 测试考点已满
    }

    @Test
    public void testAuditRegistration_Approve() {
        // 测试审核通过
    }

    @Test
    public void testAuditRegistration_Reject() {
        // 测试审核驳回
    }
}

// src/test/java/com/exam/service/PaymentServiceTest.java
@SpringBootTest
public class PaymentServiceTest {

    @Autowired
    private PaymentService paymentService;

    @Test
    public void testCreateOrder_Success() {
        // 测试创建订单
    }

    @Test
    public void testMockPayment_Success() {
        // 测试模拟支付
    }

    @Test
    public void testMockPayment_AlreadyPaid() {
        // 测试重复支付
    }
}
```

#### 验收标准
- ✓ 核心业务逻辑测试覆盖率 > 60%
- ✓ 所有测试用例通过
- ✓ 无测试失败

---

### 任务 12: 完善API文档和部署文档

**优先级**: 🟡 中
**预计时间**: 2小时

#### 12.1 集成Swagger API文档

**添加依赖**（pom.xml）
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-ui</artifactId>
    <version>1.7.0</version>
</dependency>
```

**配置类**
```java
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("在线考试报名系统 API")
                .version("1.0.0")
                .description("API接口文档"));
    }
}
```

**访问地址**: http://localhost:8080/swagger-ui.html

#### 12.2 完善部署文档

创建 `docs/部署文档.md`:

```markdown
# 部署文档

## 环境要求
- JDK 11+
- Node.js 16+
- MySQL 8.0+
- Docker & Docker Compose

## 一键部署（推荐）

### 1. 克隆项目
git clone https://github.com/yourusername/exam-registration-system.git
cd exam-registration-system

### 2. 启动数据库
docker-compose up -d

### 3. 启动后端
cd backend
mvn clean package
java -jar target/exam-registration-system-0.0.1-SNAPSHOT.jar

### 4. 启动前端
cd frontend
npm install
npm run build
npm run preview

## 访问地址
- 前端: http://localhost:5173
- 后端: http://localhost:8080
- API文档: http://localhost:8080/swagger-ui.html

## 测试账号
- 管理员: admin / admin123
- 考生: 13800138000 / 123456
```

#### 验收标准
- ✓ Swagger API文档可访问
- ✓ 部署文档清晰完整
- ✓ 测试文档包含测试场景和结果

---

## 📈 进度追踪

| 阶段 | 任务 | 状态 | 预计完成时间 |
|------|------|------|--------------|
| 第一阶段 | 任务1: 启动数据库 | ⏳ 待开始 | Day 1 上午 |
| 第二阶段 | 任务2: 报名审核Service | ⏳ 待开始 | Day 1 下午 |
| 第二阶段 | 任务3: 报名审核Controller | ⏳ 待开始 | Day 2 上午 |
| 第二阶段 | 任务4: 前端审核页面 | ⏳ 待开始 | Day 2 下午 |
| 第二阶段 | 任务5: 考生报名页面 | ⏳ 待开始 | Day 3 |
| 第三阶段 | 任务6: 后端支付模块 | ⏳ 待开始 | Day 4 上午 |
| 第三阶段 | 任务7: 前端支付页面 | ⏳ 待开始 | Day 4 下午 |
| 第四阶段 | 任务8: 后端数据统计 | ⏳ 待开始 | Day 5 上午 |
| 第四阶段 | 任务9: 前端数据可视化 | ⏳ 待开始 | Day 5 下午 |
| 第五阶段 | 任务10: 联调测试 | ⏳ 待开始 | Day 6 |
| 第五阶段 | 任务11: 单元测试 | ⏳ 待开始 | Day 7 上午 |
| 第五阶段 | 任务12: 完善文档 | ⏳ 待开始 | Day 7 下午 |

---

## 🎓 毕业设计答辩准备

### 演示脚本（25分钟）

#### 1. 项目介绍（5分钟）
- 项目背景和意义
- 技术选型
- 系统架构图

#### 2. 功能演示（10分钟）
- 考生端：注册 → 登录 → 报名 → 支付
- 管理端：审核 → 数据统计 → 公告管理

#### 3. 技术亮点（5分钟）
- 前后端分离架构
- Spring Security + JWT 认证
- 敏感信息加密（AES）
- Docker 容器化部署
- RESTful API 设计

#### 4. 代码展示（5分钟）
- 核心业务逻辑（报名审核）
- 安全性设计（加密、认证）
- 数据库设计（ER图）

### 可能的答辩问题

| 问题 | 建议回答 |
|------|---------|
| 为什么选择这些技术栈？ | Spring Boot成熟稳定，React生态丰富，适合快速开发 |
| 如何保证敏感信息安全？ | BCrypt密码加密 + AES敏感数据加密 + JWT Token |
| 系统能支持多少并发？ | 设计支持500并发，通过索引优化和缓存可进一步提升 |
| 如何防止重复报名？ | 数据库唯一索引 + 业务层校验 |
| 支付为什么用Mock？ | 毕业设计重点在业务逻辑，真实支付需要企业资质 |

---

## 🚨 风险提示

### 高风险项
1. **报名审核模块** - 这是核心，必须完整实现
2. **前后端联调** - 预留充足测试时间
3. **数据库连接** - Docker容器可能不稳定

### 建议
- 每完成一个任务立即提交Git
- 遇到问题及时记录和寻求帮助
- 保持数据库定期备份

---

## 📞 联系与支持

- **项目仓库**: https://github.com/scguoi/exam-registration-system
- **开发工具**: IntelliJ IDEA
- **操作系统**: macOS (Darwin 25.0.0)

---

**最后更新**: 2025-10-17
**计划版本**: 1.0
**预计完成日期**: 2025-10-24
