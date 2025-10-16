import api from './api';
import type { Registration, RegistrationRequest, AuditRequest, ApiResponse, PageResponse } from '../types';

// 提交报名
export const submitRegistration = (data: RegistrationRequest): Promise<ApiResponse<Registration>> => {
  return api.post('/registrations', data);
};

// 获取我的报名记录
export const getMyRegistrations = (params?: {
  pageNum?: number;
  pageSize?: number;
  examId?: number;
  auditStatus?: number;
}): Promise<ApiResponse<PageResponse<Registration>>> => {
  return api.get('/registrations/my', { params });
};

// 获取报名详情
export const getRegistrationDetail = (id: number): Promise<ApiResponse<Registration>> => {
  return api.get(`/registrations/${id}`);
};

// 修改报名信息
export const updateRegistration = (id: number, data: Partial<RegistrationRequest>): Promise<ApiResponse<Registration>> => {
  return api.put(`/registrations/${id}`, data);
};

// 获取待审核列表（管理员）
export const getPendingRegistrations = (params?: {
  pageNum?: number;
  pageSize?: number;
  examId?: number;
  auditStatus?: number;
  keyword?: string;
}): Promise<ApiResponse<PageResponse<Registration>>> => {
  return api.get('/registrations/pending', { params });
};

// 审核报名（管理员）
export const auditRegistration = (data: AuditRequest): Promise<ApiResponse> => {
  return api.post('/registrations/audit', data);
};

// 批量审核（管理员）
export const batchAuditRegistrations = (data: {
  registrationIds: number[];
  auditStatus: number;
  auditRemark?: string;
}): Promise<ApiResponse> => {
  return api.post('/registrations/batch-audit', data);
};

// 获取报名统计（管理员）
export const getRegistrationStatistics = (params?: {
  examId?: number;
  startDate?: string;
  endDate?: string;
}): Promise<ApiResponse<{
  totalCount: number;
  pendingCount: number;
  approvedCount: number;
  rejectedCount: number;
  paidCount: number;
}>> => {
  return api.get('/registrations/statistics', { params });
};
