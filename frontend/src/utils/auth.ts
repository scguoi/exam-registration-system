import type { User } from '../types';

// Token相关操作
export const TokenUtils = {
  // 获取token
  getToken: (): string | null => {
    return localStorage.getItem('token');
  },

  // 设置token
  setToken: (token: string): void => {
    localStorage.setItem('token', token);
  },

  // 移除token
  removeToken: (): void => {
    localStorage.removeItem('token');
  },

  // 检查token是否存在
  hasToken: (): boolean => {
    return !!TokenUtils.getToken();
  },
};

// 用户信息相关操作
export const UserUtils = {
  // 获取用户信息
  getUserInfo: (): User | null => {
    const userInfo = localStorage.getItem('userInfo');
    return userInfo ? JSON.parse(userInfo) : null;
  },

  // 设置用户信息
  setUserInfo: (userInfo: User): void => {
    localStorage.setItem('userInfo', JSON.stringify(userInfo));
  },

  // 移除用户信息
  removeUserInfo: (): void => {
    localStorage.removeItem('userInfo');
  },

  // 检查是否为管理员
  isAdmin: (): boolean => {
    const userInfo = UserUtils.getUserInfo();
    return userInfo?.role === 'admin';
  },

  // 检查是否为考生
  isUser: (): boolean => {
    const userInfo = UserUtils.getUserInfo();
    return userInfo?.role === 'user';
  },
};

// 权限检查
export const PermissionUtils = {
  // 检查是否有权限访问管理端
  canAccessAdmin: (): boolean => {
    return UserUtils.isAdmin();
  },

  // 检查是否有权限访问考生端
  canAccessUser: (): boolean => {
    return UserUtils.isUser();
  },

  // 检查是否已登录
  isLoggedIn: (): boolean => {
    return TokenUtils.hasToken() && !!UserUtils.getUserInfo();
  },
};

// 登出
export const logout = (): void => {
  TokenUtils.removeToken();
  UserUtils.removeUserInfo();
  // 可以在这里添加其他清理逻辑
};
