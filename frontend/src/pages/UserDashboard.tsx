import React, { useState, useEffect } from 'react';
import { Card, Row, Col, Statistic, List, Avatar, Button, Space, Tag } from 'antd';
import { 
  UserOutlined, 
  BookOutlined, 
  DollarOutlined,
  ClockCircleOutlined,
  CheckCircleOutlined
} from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { useAuthContext } from '../contexts/AuthContext';
import { getMyRegistrations } from '../services/registration';
import { formatDate, formatStatus } from '../utils/format';
import type { Registration } from '../types';

const UserDashboard: React.FC = () => {
  const { user } = useAuthContext();
  const navigate = useNavigate();
  const [registrations, setRegistrations] = useState<Registration[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    loadMyRegistrations();
  }, []);

  const loadMyRegistrations = async () => {
    setLoading(true);
    try {
      const response = await getMyRegistrations({ pageSize: 5 });
      setRegistrations(response.data.list);
    } catch (error) {
      console.error('Load registrations error:', error);
    } finally {
      setLoading(false);
    }
  };

  const getStatusColor = (status: number) => {
    switch (status) {
      case 1: return 'processing';
      case 2: return 'success';
      case 3: return 'error';
      default: return 'default';
    }
  };

  const getPaymentStatusColor = (status: number) => {
    switch (status) {
      case 1: return 'warning';
      case 2: return 'success';
      default: return 'default';
    }
  };

  return (
    <div>
      <h2>个人中心</h2>
      
      {/* 用户信息卡片 */}
      <Card style={{ marginBottom: '24px' }}>
        <Row gutter={16} align="middle">
          <Col>
            <Avatar size={64} src={user?.avatar} icon={<UserOutlined />} />
          </Col>
          <Col flex={1}>
            <h3 style={{ margin: 0 }}>{user?.realName || '未设置姓名'}</h3>
            <p style={{ margin: '8px 0 0 0', color: '#666' }}>
              手机号：{user?.phone || '未设置'}
            </p>
            <p style={{ margin: '4px 0 0 0', color: '#666' }}>
              注册时间：{user?.createTime ? formatDate(user.createTime) : '未知'}
            </p>
          </Col>
          <Col>
            <Button type="primary" onClick={() => navigate('/user/profile')}>
              编辑资料
            </Button>
          </Col>
        </Row>
      </Card>

      {/* 统计卡片 */}
      <Row gutter={16} style={{ marginBottom: '24px' }}>
        <Col span={6}>
          <Card>
            <Statistic
              title="总报名数"
              value={registrations.length}
              prefix={<BookOutlined />}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="待审核"
              value={registrations.filter(r => r.auditStatus === 1).length}
              prefix={<ClockCircleOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="已通过"
              value={registrations.filter(r => r.auditStatus === 2).length}
              prefix={<CheckCircleOutlined />}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="已缴费"
              value={registrations.filter(r => r.paymentStatus === 2).length}
              prefix={<DollarOutlined />}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
      </Row>

      {/* 最近报名记录 */}
      <Card 
        title="最近报名记录" 
        extra={
          <Button type="link" onClick={() => navigate('/user/registrations')}>
            查看全部
          </Button>
        }
      >
        <List
          loading={loading}
          dataSource={registrations}
          renderItem={(item) => (
            <List.Item
              actions={[
                <Button 
                  type="link" 
                  onClick={() => navigate(`/user/registrations/${item.id}`)}
                >
                  查看详情
                </Button>
              ]}
            >
              <List.Item.Meta
                avatar={<Avatar icon={<BookOutlined />} />}
                title={item.exam?.examName}
                description={
                  <Space direction="vertical" size="small">
                    <div>
                      考试时间：{item.exam?.examDate ? formatDate(item.exam.examDate) : '未知'} {item.exam?.examTime}
                    </div>
                    <div>
                      报名时间：{item.createTime ? formatDate(item.createTime) : '未知'}
                    </div>
                    <div>
                      <Space>
                        <Tag color={getStatusColor(item.auditStatus)}>
                          {formatStatus(item.auditStatus, 'audit')}
                        </Tag>
                        <Tag color={getPaymentStatusColor(item.paymentStatus)}>
                          {formatStatus(item.paymentStatus, 'payment')}
                        </Tag>
                      </Space>
                    </div>
                  </Space>
                }
              />
            </List.Item>
          )}
        />
      </Card>
    </div>
  );
};

export default UserDashboard;
