import api from './api';

/**
 * 报名相关API
 */

// ==================== 类型定义 ====================

export interface RegistrationRequest {
  examId: number;
  examSiteId: number;
  idCard: string;
  phone: string;
  subject?: string;
  materials?: string;
}

export interface AuditRequest {
  auditStatus: number; // 2-通过 3-驳回
  auditRemark?: string;
}

export interface Registration {
  id: number;
  examId: number;
  examName?: string;
  examDate?: string;
  examTime?: string;
  userId: number;
  userRealName?: string;
  username?: string;
  examSiteId: number;
  siteName?: string;
  siteAddress?: string;
  subject?: string;
  idCard?: string;
  idCardMasked?: string;
  phone?: string;
  phoneMasked?: string;
  materials?: string;
  auditStatus: number;
  auditStatusText?: string;
  auditRemark?: string;
  auditTime?: string;
  paymentStatus: number;
  paymentStatusText?: string;
  paymentTime?: string;
  admissionTicketNo?: string;
  createTime: string;
  fee?: number;
}

export interface PageResult<T> {
  records: T[];
  total: number;
  current: number;
  size: number;
}

// ==================== 考生端接口 ====================

/**
 * 提交报名
 */
export const submitRegistration = (data: RegistrationRequest) =>
  api.post('/registrations', data);

/**
 * 查询我的报名记录
 */
export const getMyRegistrations = () =>
  api.get('/registrations/my');

/**
 * 获取报名详情
 */
export const getRegistrationDetail = (id: number) =>
  api.get(`/registrations/${id}`);

/**
 * 取消报名
 */
export const cancelRegistration = (id: number) =>
  api.put(`/registrations/${id}/cancel`);

// ==================== 管理员端接口 ====================

/**
 * 获取待审核报名列表（分页）
 */
export const getPendingRegistrations = (page: number, size: number, examId?: number) =>
  api.get('/registrations/pending', { params: { page, size, examId } });

/**
 * 审核报名（通过/驳回）
 */
export const auditRegistration = (id: number, data: AuditRequest) =>
  api.put(`/registrations/${id}/audit`, data);

/**
 * 获取所有报名列表（管理员，支持筛选）
 */
export const getAllRegistrations = (
  page: number,
  size: number,
  auditStatus?: number,
  examId?: number
) =>
  api.get('/registrations', {
    params: { page, size, auditStatus, examId }
  });

/**
 * 报名统计
 */
export const getRegistrationStats = (examId?: number) =>
  api.get('/registrations/stats', { params: { examId } });
