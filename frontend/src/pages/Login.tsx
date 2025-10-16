import React, { useState } from 'react';
import { Form, Input, Button, Card, Space } from 'antd';
import { UserOutlined, LockOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { useAuthContext } from '../contexts/AuthContext';
import type { LoginRequest } from '../types';


const Login: React.FC = () => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const { login } = useAuthContext();

  const validateUsername = async (_: any, value: string) => {
    if (!value) {
      return Promise.reject(new Error('请输入用户名/手机号!'));
    }
    // 如果输入看起来像手机号（以1开头且长度为11位），则验证手机号格式
    const isLikelyPhoneNumber = /^1\d{10}$/.test(value);
    if (isLikelyPhoneNumber) {
      const phonePattern = /^1[3-9]\d{9}$/;
      if (!phonePattern.test(value)) {
        return Promise.reject(new Error('请输入正确的手机号格式!'));
      }
    }
    // 如果不是手机号格式，则允许通过（如admin等用户名）
    return Promise.resolve();
  };


  const handleLogin = async (values: LoginRequest) => {
    setLoading(true);
    try {
      const result = await login(values);
      if (result.success) {
        // 根据用户角色跳转到不同页面
        const userRole = result.user?.role;
        if (userRole === 'admin') {
          navigate('/admin/dashboard');
        } else {
          navigate('/user/dashboard');
        }
      }
    } catch (error) {
      console.error('Login error:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleRegister = () => {
    navigate('/register');
  };

  return (
    <div style={{
      minHeight: '100vh',
      background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      padding: '20px'
    }}>
      <Card 
        style={{ 
          width: '100%', 
          maxWidth: 400,
          boxShadow: '0 8px 32px rgba(0,0,0,0.1)'
        }}
        bodyStyle={{ padding: '40px' }}
      >
        <div style={{ textAlign: 'center', marginBottom: '32px' }}>
          <h2 style={{ color: '#1890ff', marginBottom: '8px' }}>在线考试报名系统</h2>
          <p style={{ color: '#666' }}>请登录您的账户</p>
        </div>

        <Form
          form={form}
          name="login"
          onFinish={handleLogin}
          autoComplete="off"
          size="large"
        >
          <Form.Item
            name="username"
            rules={[
              { required: true, message: '请输入用户名/手机号' },
              { validator: validateUsername }
            ]}
          >
            <Input
              prefix={<UserOutlined />}
              placeholder="用户名/手机号"
            />
          </Form.Item>

          <Form.Item
            name="password"
            rules={[
              { required: true, message: '请输入密码' },
              { min: 6, message: '密码至少6位' }
            ]}
          >
            <Input.Password
              prefix={<LockOutlined />}
              placeholder="密码"
            />
          </Form.Item>

          <Form.Item>
            <Button
              type="primary"
              htmlType="submit"
              loading={loading}
              style={{ width: '100%', height: '44px' }}
            >
              登录
            </Button>
          </Form.Item>
        </Form>

        <div style={{ textAlign: 'center', marginTop: '16px' }}>
          <Space>
            <Button type="link" onClick={handleRegister}>
              注册新账户
            </Button>
            <Button type="link">
              忘记密码？
            </Button>
          </Space>
        </div>

        <div style={{ 
          marginTop: '24px', 
          padding: '16px', 
          background: '#f5f5f5', 
          borderRadius: '6px',
          fontSize: '12px',
          color: '#666'
        }}>
          <p style={{ margin: '0 0 8px 0', fontWeight: 'bold' }}>测试账号：</p>
          <p style={{ margin: '0 0 4px 0' }}>管理员：admin / admin123</p>
          <p style={{ margin: '0' }}>考生：13800138000 / 123456</p>
        </div>
      </Card>
    </div>
  );
};

export default Login;
