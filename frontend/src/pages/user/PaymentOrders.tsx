import React, { useState, useEffect } from 'react';
import {
  Card,
  Table,
  Tag,
  Button,
  Space,
  Empty,
  Spin,
  message,
  Modal,
  Descriptions,
} from 'antd';
import {
  EyeOutlined,
  ReloadOutlined,
  DollarOutlined,
} from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import {
  getMyOrders,
  getOrderDetail,
  type PaymentOrder,
} from '../../services/payment';

/**
 * 缴费记录页面
 */
export default function PaymentOrders() {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [dataSource, setDataSource] = useState<PaymentOrder[]>([]);

  // 详情弹窗
  const [detailModalVisible, setDetailModalVisible] = useState(false);
  const [detailData, setDetailData] = useState<any>(null);
  const [detailLoading, setDetailLoading] = useState(false);

  // 加载我的订单列表
  const loadData = async () => {
    setLoading(true);
    try {
      const response: any = await getMyOrders();

      if (response.code === 200) {
        setDataSource(response.data || []);
      } else {
        message.error(response.message || '加载订单列表失败');
      }
    } catch (error: any) {
      console.error('加载订单列表失败:', error);
      message.error(error.message || '加载订单列表失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  // 查看详情
  const handleViewDetail = async (record: PaymentOrder) => {
    setDetailModalVisible(true);
    setDetailLoading(true);
    try {
      const response: any = await getOrderDetail(record.orderNo);
      if (response.code === 200) {
        setDetailData(response.data);
      } else {
        message.error(response.message || '获取订单详情失败');
      }
    } catch (error: any) {
      console.error('获取订单详情失败:', error);
      message.error(error.message || '获取订单详情失败');
    } finally {
      setDetailLoading(false);
    }
  };

  // 去支付
  const handlePay = (record: PaymentOrder) => {
    navigate(`/user/payment/${record.registrationId}`);
  };

  // 订单状态标签
  const getStatusTag = (status: number) => {
    const statusMap: Record<number, { text: string; color: string }> = {
      1: { text: '待支付', color: 'orange' },
      2: { text: '已支付', color: 'green' },
      3: { text: '已关闭', color: 'default' },
      4: { text: '已退款', color: 'blue' },
    };
    const config = statusMap[status] || { text: '未知', color: 'default' };
    return <Tag color={config.color}>{config.text}</Tag>;
  };

  // 表格列定义
  const columns = [
    {
      title: '订单号',
      dataIndex: 'orderNo',
      width: 200,
    },
    {
      title: '报名ID',
      dataIndex: 'registrationId',
      width: 100,
    },
    {
      title: '金额（元）',
      dataIndex: 'amount',
      width: 120,
      render: (amount: number) => (
        <span style={{ fontSize: 16, color: '#ff4d4f', fontWeight: 'bold' }}>
          ¥{amount}
        </span>
      ),
    },
    {
      title: '支付方式',
      dataIndex: 'paymentMethod',
      width: 120,
      render: (method: string) => {
        const methodMap: Record<string, string> = {
          mock: 'Mock支付',
          alipay: '支付宝',
          wechat: '微信支付',
        };
        return methodMap[method] || method || '-';
      },
    },
    {
      title: '订单状态',
      dataIndex: 'status',
      width: 100,
      render: (status: number) => getStatusTag(status),
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      width: 180,
    },
    {
      title: '支付时间',
      dataIndex: 'payTime',
      width: 180,
      render: (time: string) => time || '-',
    },
    {
      title: '操作',
      width: 180,
      fixed: 'right' as const,
      render: (_: any, record: PaymentOrder) => (
        <Space>
          <Button
            type="link"
            size="small"
            icon={<EyeOutlined />}
            onClick={() => handleViewDetail(record)}
          >
            查看
          </Button>

          {/* 待支付订单显示去支付按钮 */}
          {record.status === 1 && (
            <Button
              type="primary"
              size="small"
              icon={<DollarOutlined />}
              onClick={() => handlePay(record)}
            >
              去支付
            </Button>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div>
      <Card
        title="缴费记录"
        extra={
          <Space>
            <Button
              icon={<ReloadOutlined />}
              onClick={loadData}
            >
              刷新
            </Button>
          </Space>
        }
      >
        {loading ? (
          <div style={{ textAlign: 'center', padding: '50px 0' }}>
            <Spin size="large" />
          </div>
        ) : dataSource.length === 0 ? (
          <Empty
            description="暂无缴费记录"
            image={Empty.PRESENTED_IMAGE_SIMPLE}
          >
            <Button type="primary" onClick={() => navigate('/user/registrations')}>
              查看我的报名
            </Button>
          </Empty>
        ) : (
          <Table
            columns={columns}
            dataSource={dataSource}
            rowKey="id"
            pagination={{
              pageSize: 10,
              showSizeChanger: true,
              showQuickJumper: true,
              showTotal: (total) => `共 ${total} 条`,
            }}
            scroll={{ x: 1200 }}
          />
        )}
      </Card>

      {/* 详情弹窗 */}
      <Modal
        title="订单详情"
        open={detailModalVisible}
        onCancel={() => setDetailModalVisible(false)}
        footer={null}
        width={800}
      >
        {detailLoading ? (
          <div style={{ textAlign: 'center', padding: 50 }}>
            <Spin size="large" />
          </div>
        ) : detailData ? (
          <Descriptions bordered column={2}>
            <Descriptions.Item label="订单号" span={2}>
              {detailData.orderNo}
            </Descriptions.Item>
            <Descriptions.Item label="报名ID" span={1}>
              {detailData.registrationId}
            </Descriptions.Item>
            <Descriptions.Item label="考试ID" span={1}>
              {detailData.examId}
            </Descriptions.Item>
            <Descriptions.Item label="订单金额" span={2}>
              <span style={{ fontSize: 18, color: '#ff4d4f', fontWeight: 'bold' }}>
                ¥{detailData.amount}
              </span>
            </Descriptions.Item>
            <Descriptions.Item label="支付方式" span={1}>
              {detailData.paymentMethod || '-'}
            </Descriptions.Item>
            <Descriptions.Item label="订单状态" span={1}>
              {getStatusTag(detailData.status)}
            </Descriptions.Item>
            <Descriptions.Item label="创建时间" span={2}>
              {detailData.createTime}
            </Descriptions.Item>
            {detailData.payTime && (
              <Descriptions.Item label="支付时间" span={2}>
                {detailData.payTime}
              </Descriptions.Item>
            )}
            {detailData.transactionId && (
              <Descriptions.Item label="交易流水号" span={2}>
                {detailData.transactionId}
              </Descriptions.Item>
            )}
            {detailData.expireTime && detailData.status === 1 && (
              <Descriptions.Item label="过期时间" span={2}>
                <span style={{ color: '#ff4d4f' }}>{detailData.expireTime}</span>
              </Descriptions.Item>
            )}
            {detailData.refundTime && (
              <Descriptions.Item label="退款时间" span={2}>
                {detailData.refundTime}
              </Descriptions.Item>
            )}
            {detailData.refundReason && (
              <Descriptions.Item label="退款原因" span={2}>
                {detailData.refundReason}
              </Descriptions.Item>
            )}
          </Descriptions>
        ) : (
          <div>暂无数据</div>
        )}
      </Modal>
    </div>
  );
}
