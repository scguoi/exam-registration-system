package com.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.common.Result;
import com.exam.dto.RegistrationRequest;
import com.exam.entity.*;
import com.exam.mapper.*;
import com.exam.utils.AESUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 报名服务类
 *
 * @author system
 * @since 2024-10-17
 */
@Slf4j
@Service
public class RegistrationService {

    @Autowired
    private RegistrationMapper registrationMapper;

    @Autowired
    private ExamMapper examMapper;

    @Autowired
    private ExamSiteMapper examSiteMapper;

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private PaymentOrderService paymentOrderService;

    /**
     * 考生提交报名
     *
     * @param request 报名请求
     * @return 报名记录
     */
    @Transactional(rollbackFor = Exception.class)
    public Result submitRegistration(RegistrationRequest request) {
        try {
            log.info("考生提交报名，userId={}, examId={}", request.getUserId(), request.getExamId());

            // 1. 验证考试是否存在
            Exam exam = examMapper.selectById(request.getExamId());
            if (exam == null) {
                return Result.error("考试不存在");
            }

            // 2. 检查考试状态
            if (exam.getStatus() != 2 && exam.getStatus() != 3) {
                return Result.error("该考试暂未开放报名");
            }

            // 3. 检查报名时间
            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(exam.getRegistrationStart())) {
                return Result.error("报名尚未开始");
            }
            if (now.isAfter(exam.getRegistrationEnd())) {
                return Result.error("报名已结束");
            }

            // 4. 检查是否重复报名（通过唯一索引 uk_user_exam）
            Registration existing = registrationMapper.selectByUserIdAndExamId(
                    request.getUserId(), request.getExamId());
            if (existing != null) {
                return Result.error("您已报名该考试，请勿重复报名");
            }

            // 5. 检查考点容量
            ExamSite site = examSiteMapper.selectById(request.getExamSiteId());
            if (site == null) {
                return Result.error("考点不存在");
            }
            if (site.getCurrentCount() >= site.getCapacity()) {
                return Result.error("该考点报名人数已满，请选择其他考点");
            }

            // 6. 加密敏感信息
            String encryptedIdCard = AESUtil.encrypt(request.getIdCard());
            String encryptedPhone = AESUtil.encrypt(request.getPhone());

            // 7. 创建报名记录
            Registration registration = new Registration();
            registration.setExamId(request.getExamId());
            registration.setUserId(request.getUserId());
            registration.setExamSiteId(request.getExamSiteId());
            registration.setIdCard(encryptedIdCard);
            registration.setPhone(encryptedPhone);
            registration.setSubject(request.getSubject());
            registration.setMaterials(request.getMaterials());
            registration.setAuditStatus(1); // 待审核
            registration.setPaymentStatus(1); // 未缴费

            registrationMapper.insert(registration);

            log.info("报名提交成功，registrationId={}", registration.getId());
            return Result.success("报名提交成功，请等待审核", registration);

        } catch (Exception e) {
            log.error("提交报名失败", e);
            return Result.error("提交报名失败：" + e.getMessage());
        }
    }

    /**
     * 管理员审核报名
     *
     * @param registrationId 报名ID
     * @param auditStatus    审核结果(2-通过 3-驳回)
     * @param auditRemark    审核备注/驳回原因
     * @param auditorId      审核人ID
     * @return 审核结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Result auditRegistration(Long registrationId, Integer auditStatus,
                                     String auditRemark, Long auditorId) {
        try {
            log.info("管理员审核报名，registrationId={}, auditStatus={}, auditorId={}",
                    registrationId, auditStatus, auditorId);

            // 1. 查询报名记录
            Registration registration = registrationMapper.selectById(registrationId);
            if (registration == null) {
                return Result.error("报名记录不存在");
            }

            // 2. 验证状态（只能审核待审核的记录）
            if (registration.getAuditStatus() != 1) {
                return Result.error("该报名已审核，无法重复审核");
            }

            // 3. 验证审核结果
            if (auditStatus != 2 && auditStatus != 3) {
                return Result.error("审核结果参数错误");
            }

            // 4. 驳回时必须填写原因
            if (auditStatus == 3 && (auditRemark == null || auditRemark.isEmpty())) {
                return Result.error("驳回时必须填写原因");
            }

            // 5. 更新审核结果
            registration.setAuditStatus(auditStatus);
            registration.setAuditRemark(auditRemark);
            registration.setAuditBy(auditorId);
            registration.setAuditTime(LocalDateTime.now());

            registrationMapper.updateById(registration);

            // 6. 审核通过后自动创建支付订单
            if (auditStatus == 2) {
                Result createOrderResult = paymentOrderService.createPaymentOrder(registrationId);
                if (createOrderResult.getCode() != 200) {
                    log.warn("审核通过后创建支付订单失败：{}", createOrderResult.getMessage());
                    // 不影响审核流程，仅记录日志
                }
            }

            String message = auditStatus == 2 ? "审核通过" : "审核驳回";
            log.info("报名审核完成，registrationId={}, result={}", registrationId, message);
            return Result.success(message);

        } catch (Exception e) {
            log.error("审核报名失败", e);
            return Result.error("审核失败：" + e.getMessage());
        }
    }

    /**
     * 考生查询自己的报名记录
     *
     * @param userId 用户ID
     * @return 报名列表
     */
    public Result getMyRegistrations(Long userId) {
        try {
            log.info("考生查询报名记录，userId={}", userId);

            List<Registration> registrations = registrationMapper.selectByUserId(userId);

            // 转换为VO并补充关联信息
            List<Map<String, Object>> result = registrations.stream().map(r -> {
                Map<String, Object> vo = new HashMap<>();
                vo.put("id", r.getId());
                vo.put("examId", r.getExamId());
                vo.put("examSiteId", r.getExamSiteId());
                vo.put("subject", r.getSubject());
                vo.put("auditStatus", r.getAuditStatus());
                vo.put("auditRemark", r.getAuditRemark());
                vo.put("auditTime", r.getAuditTime());
                vo.put("paymentStatus", r.getPaymentStatus());
                vo.put("paymentTime", r.getPaymentTime());
                vo.put("admissionTicketNo", r.getAdmissionTicketNo());
                vo.put("createTime", r.getCreateTime());

                // 状态文本
                String auditStatusText = getAuditStatusText(r.getAuditStatus());
                vo.put("auditStatusText", auditStatusText);

                String paymentStatusText = r.getPaymentStatus() == 2 ? "已缴费" : "未缴费";
                vo.put("paymentStatusText", paymentStatusText);

                // 查询考试信息
                Exam exam = examMapper.selectById(r.getExamId());
                if (exam != null) {
                    vo.put("examName", exam.getExamName());
                    vo.put("examDate", exam.getExamDate());
                    vo.put("examTime", exam.getExamTime());
                    vo.put("fee", exam.getFee());
                }

                // 查询考点信息
                if (r.getExamSiteId() != null) {
                    ExamSite site = examSiteMapper.selectById(r.getExamSiteId());
                    if (site != null) {
                        vo.put("siteName", site.getSiteName());
                        vo.put("siteAddress", site.getAddress());
                    }
                }

                return vo;
            }).collect(Collectors.toList());

            return Result.success(result);

        } catch (Exception e) {
            log.error("查询报名记录失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 管理员查询待审核报名列表（分页）
     *
     * @param page   页码
     * @param size   每页数量
     * @param examId 考试ID（可选）
     * @return 分页数据
     */
    public Result getPendingRegistrations(Integer page, Integer size, Long examId) {
        try {
            log.info("查询待审核报名列表，page={}, size={}, examId={}", page, size, examId);

            Page<Registration> pageObj = new Page<>(page, size);
            IPage<Registration> resultPage = registrationMapper.selectPendingAuditPage(pageObj, examId);

            // 转换为VO
            List<Map<String, Object>> records = resultPage.getRecords().stream().map(r -> {
                Map<String, Object> vo = new HashMap<>();
                vo.put("id", r.getId());
                vo.put("examId", r.getExamId());
                vo.put("userId", r.getUserId());
                vo.put("examSiteId", r.getExamSiteId());
                vo.put("subject", r.getSubject());
                vo.put("auditStatus", r.getAuditStatus());
                vo.put("createTime", r.getCreateTime());

                // 解密敏感信息供管理员查看
                if (r.getIdCard() != null) {
                    try {
                        String idCard = AESUtil.decrypt(r.getIdCard());
                        vo.put("idCard", idCard);
                        vo.put("idCardMasked", AESUtil.maskIdCard(idCard));
                    } catch (Exception e) {
                        vo.put("idCard", "解密失败");
                        vo.put("idCardMasked", "***");
                    }
                }

                if (r.getPhone() != null) {
                    try {
                        String phone = AESUtil.decrypt(r.getPhone());
                        vo.put("phone", phone);
                        vo.put("phoneMasked", AESUtil.maskPhone(phone));
                    } catch (Exception e) {
                        vo.put("phone", "解密失败");
                        vo.put("phoneMasked", "***");
                    }
                }

                // 查询考生信息
                SysUser user = userMapper.selectById(r.getUserId());
                if (user != null) {
                    vo.put("userRealName", user.getRealName());
                    vo.put("username", user.getUsername());
                }

                // 查询考试信息
                Exam exam = examMapper.selectById(r.getExamId());
                if (exam != null) {
                    vo.put("examName", exam.getExamName());
                }

                // 查询考点信息
                if (r.getExamSiteId() != null) {
                    ExamSite site = examSiteMapper.selectById(r.getExamSiteId());
                    if (site != null) {
                        vo.put("siteName", site.getSiteName());
                    }
                }

                return vo;
            }).collect(Collectors.toList());

            Map<String, Object> result = new HashMap<>();
            result.put("records", records);
            result.put("total", resultPage.getTotal());
            result.put("current", resultPage.getCurrent());
            result.put("size", resultPage.getSize());

            return Result.success(result);

        } catch (Exception e) {
            log.error("查询待审核列表失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 考生取消报名（仅限待审核状态）
     *
     * @param registrationId 报名ID
     * @param userId         用户ID
     * @return 取消结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Result cancelRegistration(Long registrationId, Long userId) {
        try {
            log.info("考生取消报名，registrationId={}, userId={}", registrationId, userId);

            Registration registration = registrationMapper.selectById(registrationId);
            if (registration == null) {
                return Result.error("报名记录不存在");
            }

            // 验证权限
            if (!registration.getUserId().equals(userId)) {
                return Result.error("无权操作");
            }

            // 验证状态（只能取消待审核的报名）
            if (registration.getAuditStatus() != 1) {
                return Result.error("只能取消待审核的报名");
            }

            // 删除报名记录（触发器会自动更新考试和考点的报名人数）
            registrationMapper.deleteById(registrationId);

            log.info("取消报名成功，registrationId={}", registrationId);
            return Result.success("取消报名成功");

        } catch (Exception e) {
            log.error("取消报名失败", e);
            return Result.error("取消失败：" + e.getMessage());
        }
    }

    /**
     * 获取报名详情（包含所有关联信息）
     *
     * @param registrationId 报名ID
     * @return 详情信息
     */
    public Result getRegistrationDetail(Long registrationId) {
        try {
            log.info("查询报名详情，registrationId={}", registrationId);

            Registration registration = registrationMapper.selectById(registrationId);
            if (registration == null) {
                return Result.error("报名记录不存在");
            }

            Map<String, Object> vo = new HashMap<>();
            vo.put("id", registration.getId());
            vo.put("examId", registration.getExamId());
            vo.put("userId", registration.getUserId());
            vo.put("examSiteId", registration.getExamSiteId());
            vo.put("subject", registration.getSubject());
            vo.put("materials", registration.getMaterials());
            vo.put("auditStatus", registration.getAuditStatus());
            vo.put("auditStatusText", getAuditStatusText(registration.getAuditStatus()));
            vo.put("auditRemark", registration.getAuditRemark());
            vo.put("auditTime", registration.getAuditTime());
            vo.put("paymentStatus", registration.getPaymentStatus());
            vo.put("paymentTime", registration.getPaymentTime());
            vo.put("admissionTicketNo", registration.getAdmissionTicketNo());
            vo.put("createTime", registration.getCreateTime());

            // 解密敏感信息
            if (registration.getIdCard() != null) {
                try {
                    vo.put("idCard", AESUtil.decrypt(registration.getIdCard()));
                } catch (Exception e) {
                    vo.put("idCard", "解密失败");
                }
            }

            if (registration.getPhone() != null) {
                try {
                    vo.put("phone", AESUtil.decrypt(registration.getPhone()));
                } catch (Exception e) {
                    vo.put("phone", "解密失败");
                }
            }

            // 查询考试信息
            Exam exam = examMapper.selectById(registration.getExamId());
            vo.put("exam", exam);

            // 查询考点信息
            if (registration.getExamSiteId() != null) {
                ExamSite site = examSiteMapper.selectById(registration.getExamSiteId());
                vo.put("site", site);
            }

            // 查询考生信息
            SysUser user = userMapper.selectById(registration.getUserId());
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("realName", user.getRealName());
            userInfo.put("username", user.getUsername());
            vo.put("user", userInfo);

            // 查询审核人信息
            if (registration.getAuditBy() != null) {
                SysUser auditor = userMapper.selectById(registration.getAuditBy());
                if (auditor != null) {
                    vo.put("auditorName", auditor.getRealName());
                }
            }

            return Result.success(vo);

        } catch (Exception e) {
            log.error("查询报名详情失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 管理员查询所有报名列表（分页，支持多条件筛选）
     *
     * @param page        页码
     * @param size        每页数量
     * @param auditStatus 审核状态（可选）
     * @param examId      考试ID（可选）
     * @return 分页数据
     */
    public Result getAllRegistrations(Integer page, Integer size, Integer auditStatus, Long examId) {
        try {
            log.info("管理员查询报名列表，page={}, size={}, auditStatus={}, examId={}",
                    page, size, auditStatus, examId);

            Page<Registration> pageObj = new Page<>(page, size);

            QueryWrapper<Registration> wrapper = new QueryWrapper<>();
            if (auditStatus != null) {
                wrapper.eq("audit_status", auditStatus);
            }
            if (examId != null) {
                wrapper.eq("exam_id", examId);
            }
            wrapper.orderByDesc("create_time");

            IPage<Registration> resultPage = registrationMapper.selectPage(pageObj, wrapper);

            // 转换为VO（与getPendingRegistrations类似）
            List<Map<String, Object>> records = resultPage.getRecords().stream().map(r -> {
                Map<String, Object> vo = new HashMap<>();
                vo.put("id", r.getId());
                vo.put("examId", r.getExamId());
                vo.put("auditStatus", r.getAuditStatus());
                vo.put("auditStatusText", getAuditStatusText(r.getAuditStatus()));
                vo.put("paymentStatus", r.getPaymentStatus());
                vo.put("createTime", r.getCreateTime());

                // 查询考生信息
                SysUser user = userMapper.selectById(r.getUserId());
                if (user != null) {
                    vo.put("userRealName", user.getRealName());
                }

                // 查询考试信息
                Exam exam = examMapper.selectById(r.getExamId());
                if (exam != null) {
                    vo.put("examName", exam.getExamName());
                }

                return vo;
            }).collect(Collectors.toList());

            Map<String, Object> result = new HashMap<>();
            result.put("records", records);
            result.put("total", resultPage.getTotal());
            result.put("current", resultPage.getCurrent());
            result.put("size", resultPage.getSize());

            return Result.success(result);

        } catch (Exception e) {
            log.error("查询报名列表失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 报名统计
     *
     * @param examId 考试ID（可选）
     * @return 统计数据
     */
    public Result getRegistrationStats(Long examId) {
        try {
            log.info("查询报名统计，examId={}", examId);

            QueryWrapper<Registration> wrapper = new QueryWrapper<>();
            if (examId != null) {
                wrapper.eq("exam_id", examId);
            }

            // 总报名数
            Integer total = registrationMapper.selectCount(wrapper);

            // 待审核数
            wrapper.clear();
            if (examId != null) {
                wrapper.eq("exam_id", examId);
            }
            wrapper.eq("audit_status", 1);
            Integer pending = registrationMapper.selectCount(wrapper);

            // 已通过数
            wrapper.clear();
            if (examId != null) {
                wrapper.eq("exam_id", examId);
            }
            wrapper.eq("audit_status", 2);
            Integer approved = registrationMapper.selectCount(wrapper);

            // 已驳回数
            wrapper.clear();
            if (examId != null) {
                wrapper.eq("exam_id", examId);
            }
            wrapper.eq("audit_status", 3);
            Integer rejected = registrationMapper.selectCount(wrapper);

            // 已缴费数
            wrapper.clear();
            if (examId != null) {
                wrapper.eq("exam_id", examId);
            }
            wrapper.eq("payment_status", 2);
            Integer paid = registrationMapper.selectCount(wrapper);

            Map<String, Object> stats = new HashMap<>();
            stats.put("total", total);
            stats.put("pending", pending);
            stats.put("approved", approved);
            stats.put("rejected", rejected);
            stats.put("paid", paid);

            return Result.success(stats);

        } catch (Exception e) {
            log.error("查询报名统计失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取审核状态文本
     */
    private String getAuditStatusText(Integer auditStatus) {
        switch (auditStatus) {
            case 1:
                return "待审核";
            case 2:
                return "审核通过";
            case 3:
                return "审核驳回";
            default:
                return "未知状态";
        }
    }
}
