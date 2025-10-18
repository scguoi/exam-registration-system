import api from './api';

/**
 * 数据统计相关API
 */

// ==================== 管理员端接口 ====================

/**
 * 获取仪表盘统计数据
 */
export const getDashboardStats = () =>
  api.get('/statistics/dashboard');

/**
 * 获取考试统计
 */
export const getExamStats = () =>
  api.get('/statistics/exam');

/**
 * 获取报名统计
 */
export const getRegistrationStats = (examId?: number) =>
  api.get('/statistics/registration', { params: { examId } });

/**
 * 获取缴费统计
 */
export const getPaymentStats = (examId?: number) =>
  api.get('/statistics/payment', { params: { examId } });

/**
 * 获取用户统计
 */
export const getUserStats = () =>
  api.get('/statistics/user');

/**
 * 获取报名趋势（最近N天）
 */
export const getRegistrationTrend = (days: number = 30) =>
  api.get('/statistics/registration-trend', { params: { days } });

/**
 * 获取缴费趋势（最近N天）
 */
export const getPaymentTrend = (days: number = 30) =>
  api.get('/statistics/payment-trend', { params: { days } });

/**
 * 按考试统计详细数据
 */
export const getExamDetailStats = () =>
  api.get('/statistics/exam-detail');
