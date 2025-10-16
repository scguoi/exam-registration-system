import { useState, useEffect, useCallback } from 'react';
import { message } from 'antd';
import { login, getUserInfo } from '../services/user';
import { TokenUtils, UserUtils, PermissionUtils, logout } from '../utils/auth';
import type { User, LoginRequest } from '../types';

export const useAuth = () => {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(false);
  const [isLoggedIn, setIsLoggedIn] = useState(false);

  // 初始化用户状态
  useEffect(() => {
    const initAuth = async () => {
      const token = TokenUtils.getToken();
      const userInfo = UserUtils.getUserInfo();
      
      if (token && userInfo) {
        setUser(userInfo);
        setIsLoggedIn(true);
        
        // 验证token是否有效
        try {
          const response = await getUserInfo();
          if (response.data) {
            setUser(response.data);
            UserUtils.setUserInfo(response.data);
          }
        } catch (error) {
          // token无效，清除本地存储
          logout();
          setUser(null);
          setIsLoggedIn(false);
        }
      }
    };

    initAuth();
  }, []);

  // 登录
  const handleLogin = useCallback(async (loginData: LoginRequest) => {
    setLoading(true);
    try {
      const response = await login(loginData);
      const { token, userInfo } = response.data;
      
      // 保存token和用户信息
      TokenUtils.setToken(token);
      UserUtils.setUserInfo(userInfo);
      
      setUser(userInfo);
      setIsLoggedIn(true);
      
      message.success('登录成功');
      return { success: true, user: userInfo };
    } catch (error) {
      message.error('登录失败');
      return { success: false, error };
    } finally {
      setLoading(false);
    }
  }, []);

  // 登出
  const handleLogout = useCallback(() => {
    logout();
    setUser(null);
    setIsLoggedIn(false);
    message.success('已退出登录');
  }, []);

  // 更新用户信息
  const updateUser = useCallback((userInfo: User) => {
    setUser(userInfo);
    UserUtils.setUserInfo(userInfo);
  }, []);

  return {
    user,
    loading,
    isLoggedIn,
    isAdmin: PermissionUtils.canAccessAdmin(),
    isUser: PermissionUtils.canAccessUser(),
    login: handleLogin,
    logout: handleLogout,
    updateUser,
  };
};
