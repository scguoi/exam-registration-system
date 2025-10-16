import React, { useState } from 'react';
import { Form, Input, Button, Card, message, Space } from 'antd';
import { LockOutlined, MobileOutlined, SafetyOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { sendSmsCode } from '../services/user';
import type { RegisterRequest } from '../types';

const Register: React.FC = () => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [smsLoading, setSmsLoading] = useState(false);
  const [countdown, setCountdown] = useState(0);
  const navigate = useNavigate();

  // 发送短信验证码
  const handleSendSms = async () => {
    const phone = form.getFieldValue('username');
    if (!phone) {
      message.error('请先输入手机号');
      return;
    }
    if (!/^1[3-9]\d{9}$/.test(phone)) {
      message.error('请输入正确的手机号格式');
      return;
    }

    setSmsLoading(true);
    try {
      await sendSmsCode(phone);
      message.success('验证码已发送');
      setCountdown(60);
      const timer = setInterval(() => {
        setCountdown((prev) => {
          if (prev <= 1) {
            clearInterval(timer);
            return 0;
          }
          return prev - 1;
        });
      }, 1000);
    } catch (error) {
      console.error('Send SMS error:', error);
    } finally {
      setSmsLoading(false);
    }
  };

  const handleRegister = async (_values: RegisterRequest) => {
    setLoading(true);
    try {
      // 这里应该调用注册接口
      message.success('注册成功，请登录');
      navigate('/login');
    } catch (error) {
      console.error('Register error:', error);
    } finally {
      setLoading(false);
    }
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
          <h2 style={{ color: '#1890ff', marginBottom: '8px' }}>用户注册</h2>
          <p style={{ color: '#666' }}>创建您的考试报名账户</p>
        </div>

        <Form
          form={form}
          name="register"
          onFinish={handleRegister}
          autoComplete="off"
          size="large"
        >
          <Form.Item
            name="username"
            rules={[
              { required: true, message: '请输入手机号' },
              { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式' }
            ]}
          >
            <Input
              prefix={<MobileOutlined />}
              placeholder="手机号"
            />
          </Form.Item>

          <Form.Item
            name="smsCode"
            rules={[
              { required: true, message: '请输入验证码' },
              { len: 6, message: '验证码为6位数字' }
            ]}
          >
            <Input
              prefix={<SafetyOutlined />}
              placeholder="短信验证码"
              suffix={
                <Button
                  type="link"
                  size="small"
                  loading={smsLoading}
                  disabled={countdown > 0}
                  onClick={handleSendSms}
                >
                  {countdown > 0 ? `${countdown}s` : '获取验证码'}
                </Button>
              }
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
              placeholder="设置密码"
            />
          </Form.Item>

          <Form.Item
            name="confirmPassword"
            dependencies={['password']}
            rules={[
              { required: true, message: '请确认密码' },
              ({ getFieldValue }) => ({
                validator(_, value) {
                  if (!value || getFieldValue('password') === value) {
                    return Promise.resolve();
                  }
                  return Promise.reject(new Error('两次输入的密码不一致'));
                },
              }),
            ]}
          >
            <Input.Password
              prefix={<LockOutlined />}
              placeholder="确认密码"
            />
          </Form.Item>

          <Form.Item>
            <Button
              type="primary"
              htmlType="submit"
              loading={loading}
              style={{ width: '100%', height: '44px' }}
            >
              注册
            </Button>
          </Form.Item>
        </Form>

        <div style={{ textAlign: 'center', marginTop: '16px' }}>
          <Space>
            <Button type="link" onClick={() => navigate('/login')}>
              已有账户？立即登录
            </Button>
          </Space>
        </div>
      </Card>
    </div>
  );
};

export default Register;
