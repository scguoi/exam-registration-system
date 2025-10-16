import api from './api';
import type { User, LoginRequest, RegisterRequest, ApiResponse } from '../types';

// 用户登录
export const login = (data: LoginRequest): Promise<ApiResponse<{ token: string; userInfo: User }>> => {
  return api.post('/users/login', data);
};

// 用户注册
export const register = (data: RegisterRequest): Promise<ApiResponse<{ token: string; userInfo: User }>> => {
  return api.post('/users/register', data);
};

// 获取用户信息
export const getUserInfo = (): Promise<ApiResponse<User>> => {
  return api.get('/users/info');
};

// 更新用户信息
export const updateUserInfo = (data: Partial<User>): Promise<ApiResponse<User>> => {
  return api.put('/users/info', data);
};

// 上传头像
export const uploadAvatar = (file: File): Promise<ApiResponse<{ url: string }>> => {
  const formData = new FormData();
  formData.append('file', file);
  return api.post('/users/avatar', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
};

// 修改密码
export const changePassword = (data: { oldPassword: string; newPassword: string }): Promise<ApiResponse> => {
  return api.put('/users/password', data);
};

// 发送短信验证码
export const sendSmsCode = (phone: string): Promise<ApiResponse> => {
  return api.post('/users/sms-code', { phone });
};

// 忘记密码
export const forgotPassword = (data: { phone: string; smsCode: string; newPassword: string }): Promise<ApiResponse> => {
  return api.post('/users/forgot-password', data);
};
