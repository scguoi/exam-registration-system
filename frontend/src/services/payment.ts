import api from './api';

/**
 * 支付订单相关API
 */

// ==================== 类型定义 ====================

export interface PaymentOrder {
  id: number;
  orderNo: string;
  registrationId: number;
  userId: number;
  examId: number;
  amount: number;
  paymentMethod: string;
  transactionId?: string;
  status: number; // 1-待支付 2-已支付 3-已关闭 4-已退款
  payTime?: string;
  expireTime?: string;
  callbackData?: string;
  refundReason?: string;
  refundTime?: string;
  createTime: string;
  updateTime: string;
}

export interface PayRequest {
  orderNo: string;
  paymentMethod?: string; // alipay/wechat/mock
}

export interface RefundRequest {
  refundReason: string;
}

// ==================== 考生端接口 ====================

/**
 * 创建支付订单
 */
export const createPaymentOrder = (registrationId: number) =>
  api.post('/payments/create', null, { params: { registrationId } });

/**
 * 执行支付（Mock支付）
 */
export const executePay = (data: PayRequest) =>
  api.post('/payments/pay', data);

/**
 * 根据订单号查询订单详情
 */
export const getOrderDetail = (orderNo: string) =>
  api.get(`/payments/${orderNo}`);

/**
 * 根据报名ID查询订单
 */
export const getOrderByRegistrationId = (registrationId: number) =>
  api.get(`/payments/registration/${registrationId}`);

/**
 * 查询我的订单列表
 */
export const getMyOrders = () =>
  api.get('/payments/my');

/**
 * 申请退款
 */
export const applyRefund = (orderNo: string, data: RefundRequest) =>
  api.put(`/payments/${orderNo}/refund`, data);

// ==================== 管理员端接口 ====================

/**
 * 分页查询订单列表（管理员）
 */
export const getOrderList = (params: {
  page: number;
  size: number;
  userId?: number;
  examId?: number;
  status?: number;
}) =>
  api.get('/payments', { params });

/**
 * 关闭过期订单（管理员）
 */
export const closeExpiredOrders = () =>
  api.put('/payments/close-expired');

/**
 * 统计缴费数据（管理员）
 */
export const getPaymentStats = (examId?: number) =>
  api.get('/payments/stats', { params: { examId } });
