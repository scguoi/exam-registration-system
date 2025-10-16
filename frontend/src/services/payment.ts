import api from './api';
import type { PaymentOrder, ApiResponse, PageResponse } from '../types';

// 创建支付订单
export const createPaymentOrder = (registrationId: number): Promise<ApiResponse<{
  orderNo: string;
  amount: number;
  payUrl: string;
}>> => {
  return api.post('/payments/create', { registrationId });
};

// 查询订单状态
export const getOrderStatus = (orderNo: string): Promise<ApiResponse<PaymentOrder>> => {
  return api.get(`/payments/${orderNo}`);
};

// 获取我的订单列表
export const getMyOrders = (params?: {
  pageNum?: number;
  pageSize?: number;
  status?: number;
}): Promise<ApiResponse<PageResponse<PaymentOrder>>> => {
  return api.get('/payments/my-orders', { params });
};

// 获取订单详情
export const getOrderDetail = (orderNo: string): Promise<ApiResponse<PaymentOrder>> => {
  return api.get(`/payments/order/${orderNo}`);
};

// 获取缴费记录列表（管理员）
export const getPaymentRecords = (params?: {
  pageNum?: number;
  pageSize?: number;
  examId?: number;
  status?: number;
  startDate?: string;
  endDate?: string;
}): Promise<ApiResponse<PageResponse<PaymentOrder>>> => {
  return api.get('/payments/records', { params });
};

// 导出缴费明细（管理员）
export const exportPaymentRecords = (params?: {
  examId?: number;
  startDate?: string;
  endDate?: string;
}): Promise<Blob> => {
  return api.get('/payments/export', { 
    params,
    responseType: 'blob'
  });
};

// 获取缴费统计（管理员）
export const getPaymentStatistics = (params?: {
  examId?: number;
  startDate?: string;
  endDate?: string;
}): Promise<ApiResponse<{
  totalAmount: number;
  paidAmount: number;
  unpaidAmount: number;
  paidCount: number;
  unpaidCount: number;
}>> => {
  return api.get('/payments/statistics', { params });
};
