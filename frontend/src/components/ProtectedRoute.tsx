import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuthContext } from '../contexts/AuthContext';

interface ProtectedRouteProps {
  children: React.ReactNode;
  requireAuth?: boolean;
  requireAdmin?: boolean;
  requireUser?: boolean;
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({
  children,
  requireAuth = true,
  requireAdmin = false,
  requireUser = false,
}) => {
  const { isLoggedIn, isAdmin, isUser } = useAuthContext();
  const location = useLocation();

  // 需要登录但未登录
  if (requireAuth && !isLoggedIn) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  // 需要管理员权限但不是管理员
  if (requireAdmin && !isAdmin) {
    return <Navigate to="/403" replace />;
  }

  // 需要考生权限但不是考生
  if (requireUser && !isUser) {
    return <Navigate to="/403" replace />;
  }

  return <>{children}</>;
};

export default ProtectedRoute;
