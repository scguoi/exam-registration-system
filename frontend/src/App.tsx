import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ConfigProvider } from 'antd';
import zhCN from 'antd/locale/zh_CN';
import { AuthProvider } from './contexts/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import UserLayout from './layouts/UserLayout';
import AdminLayout from './layouts/AdminLayout';

// 页面组件
import Login from './pages/Login';
import Register from './pages/Register';
import UserDashboard from './pages/UserDashboard';
import AdminDashboard from './pages/AdminDashboard';

// 配置Ant Design主题
const theme = {
  token: {
    colorPrimary: '#1890ff',
    borderRadius: 6,
  },
};

const App: React.FC = () => {
  return (
    <ConfigProvider locale={zhCN} theme={theme}>
      <AuthProvider>
        <Router>
          <Routes>
            {/* 公开路由 */}
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            
            {/* 考生端路由 */}
            <Route path="/user/*" element={
              <ProtectedRoute requireAuth requireUser>
                <UserLayout>
                  <Routes>
                    <Route path="/dashboard" element={<UserDashboard />} />
                    <Route path="/" element={<Navigate to="/user/dashboard" replace />} />
                    {/* 其他考生端路由可以在这里添加 */}
                  </Routes>
                </UserLayout>
              </ProtectedRoute>
            } />
            
            {/* 管理端路由 */}
            <Route path="/admin/*" element={
              <ProtectedRoute requireAuth requireAdmin>
                <AdminLayout>
                  <Routes>
                    <Route path="/dashboard" element={<AdminDashboard />} />
                    <Route path="/" element={<Navigate to="/admin/dashboard" replace />} />
                    {/* 其他管理端路由可以在这里添加 */}
                  </Routes>
                </AdminLayout>
              </ProtectedRoute>
            } />
            
            {/* 默认重定向 */}
            <Route path="/" element={<Navigate to="/login" replace />} />
            <Route path="*" element={<Navigate to="/login" replace />} />
          </Routes>
        </Router>
      </AuthProvider>
    </ConfigProvider>
  );
};

export default App;