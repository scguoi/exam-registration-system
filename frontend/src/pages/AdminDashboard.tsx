import React, { useState, useEffect } from 'react';
import { Card, Row, Col, Statistic, List, Avatar, Button, Space, Progress } from 'antd';
import { 
  UserOutlined, 
  BookOutlined, 
  FileTextOutlined, 
  DollarOutlined,
  ClockCircleOutlined,
  CheckCircleOutlined
} from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { useAuthContext } from '../contexts/AuthContext';
import { formatDate } from '../utils/format';
import type { StatisticsData } from '../types';

const AdminDashboard: React.FC = () => {
  const { user } = useAuthContext();
  const navigate = useNavigate();
  const [stats, setStats] = useState<StatisticsData>({
    registrationCount: 0,
    paymentCount: 0,
    auditCount: 0,
    examCount: 0,
  });

  useEffect(() => {
    loadStatistics();
  }, []);

  const loadStatistics = async () => {
    try {
      // 这里应该调用统计接口获取数据
      // const response = await getRegistrationStatistics();
      // setStats(response.data);
      
      // 模拟数据
      setStats({
        registrationCount: 156,
        paymentCount: 89,
        auditCount: 23,
        examCount: 8,
      });
    } catch (error) {
      console.error('Load statistics error:', error);
    }
  };

  const recentActivities = [
    {
      id: 1,
      type: 'registration',
      title: '新报名申请',
      description: '张三 报名了 计算机二级考试',
      time: '2024-01-15 14:30',
      status: 'pending'
    },
    {
      id: 2,
      type: 'payment',
      title: '缴费完成',
      description: '李四 完成了 英语四级考试 缴费',
      time: '2024-01-15 13:45',
      status: 'success'
    },
    {
      id: 3,
      type: 'audit',
      title: '审核通过',
      description: '王五 的 教师资格证考试 报名已通过',
      time: '2024-01-15 12:20',
      status: 'success'
    },
  ];

  return (
    <div>
      <h2>数据概览</h2>
      
      {/* 管理员信息 */}
      <Card style={{ marginBottom: '24px' }}>
        <Row gutter={16} align="middle">
          <Col>
            <Avatar size={64} src={user?.avatar} icon={<UserOutlined />} />
          </Col>
          <Col flex={1}>
            <h3 style={{ margin: 0 }}>欢迎回来，{user?.realName || '管理员'}</h3>
            <p style={{ margin: '8px 0 0 0', color: '#666' }}>
              今天是 {formatDate(new Date(), 'YYYY年MM月DD日 dddd')}
            </p>
          </Col>
        </Row>
      </Card>

      {/* 统计卡片 */}
      <Row gutter={16} style={{ marginBottom: '24px' }}>
        <Col span={6}>
          <Card>
            <Statistic
              title="总报名数"
              value={stats.registrationCount}
              prefix={<FileTextOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="待审核"
              value={stats.auditCount}
              prefix={<ClockCircleOutlined />}
              valueStyle={{ color: '#faad14' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="已缴费"
              value={stats.paymentCount}
              prefix={<DollarOutlined />}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="进行中考试"
              value={stats.examCount}
              prefix={<BookOutlined />}
              valueStyle={{ color: '#722ed1' }}
            />
          </Card>
        </Col>
      </Row>

      <Row gutter={16}>
        {/* 审核进度 */}
        <Col span={12}>
          <Card title="审核进度" extra={<Button type="link" onClick={() => navigate('/admin/registrations')}>查看全部</Button>}>
            <div style={{ marginBottom: '16px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px' }}>
                <span>待审核</span>
                <span>{stats.auditCount} / {stats.registrationCount}</span>
              </div>
              <Progress 
                percent={Math.round((stats.auditCount / stats.registrationCount) * 100)} 
                status="active"
              />
            </div>
            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px' }}>
                <span>已审核</span>
                <span>{stats.registrationCount - stats.auditCount} / {stats.registrationCount}</span>
              </div>
              <Progress 
                percent={Math.round(((stats.registrationCount - stats.auditCount) / stats.registrationCount) * 100)} 
                status="success"
              />
            </div>
          </Card>
        </Col>

        {/* 最近活动 */}
        <Col span={12}>
          <Card title="最近活动" extra={<Button type="link" onClick={() => navigate('/admin/statistics')}>查看统计</Button>}>
            <List
              dataSource={recentActivities}
              renderItem={(item) => (
                <List.Item>
                  <List.Item.Meta
                    avatar={
                      <Avatar 
                        icon={
                          item.type === 'registration' ? <FileTextOutlined /> :
                          item.type === 'payment' ? <DollarOutlined /> :
                          <CheckCircleOutlined />
                        }
                        style={{
                          backgroundColor: item.status === 'success' ? '#52c41a' : 
                                          item.status === 'pending' ? '#faad14' : '#f5222d'
                        }}
                      />
                    }
                    title={item.title}
                    description={
                      <Space direction="vertical" size="small">
                        <div>{item.description}</div>
                        <div style={{ fontSize: '12px', color: '#999' }}>
                          {item.time}
                        </div>
                      </Space>
                    }
                  />
                </List.Item>
              )}
            />
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default AdminDashboard;
