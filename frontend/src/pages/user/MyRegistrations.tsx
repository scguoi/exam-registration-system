import React, { useState, useEffect } from 'react';
import {
  Card,
  Table,
  Tag,
  Button,
  Space,
  Modal,
  Descriptions,
  message,
  Spin,
  Empty,
  Popconfirm,
} from 'antd';
import {
  EyeOutlined,
  CloseCircleOutlined,
  DollarOutlined,
  ReloadOutlined,
  FileTextOutlined,
} from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import {
  getMyRegistrations,
  getRegistrationDetail,
  cancelRegistration,
  type Registration,
} from '../../services/registration';

/**
 * 考生"我的报名"页面
 */
export default function MyRegistrations() {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [dataSource, setDataSource] = useState<Registration[]>([]);

  // 详情弹窗
  const [detailModalVisible, setDetailModalVisible] = useState(false);
  const [detailData, setDetailData] = useState<any>(null);
  const [detailLoading, setDetailLoading] = useState(false);

  // 加载我的报名记录
  const loadData = async () => {
    setLoading(true);
    try {
      const response: any = await getMyRegistrations();

      if (response.code === 200) {
        setDataSource(response.data || []);
      } else {
        message.error(response.message || '加载报名记录失败');
      }
    } catch (error: any) {
      console.error('加载报名记录失败:', error);
      message.error(error.message || '加载报名记录失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  // 查看详情
  const handleViewDetail = async (record: Registration) => {
    setDetailModalVisible(true);
    setDetailLoading(true);
    try {
      const response: any = await getRegistrationDetail(record.id);
      if (response.code === 200) {
        setDetailData(response.data);
      } else {
        message.error(response.message || '获取详情失败');
      }
    } catch (error: any) {
      console.error('获取详情失败:', error);
      message.error(error.message || '获取详情失败');
    } finally {
      setDetailLoading(false);
    }
  };

  // 取消报名
  const handleCancel = async (id: number) => {
    try {
      const response: any = await cancelRegistration(id);
      if (response.code === 200) {
        message.success('取消报名成功');
        loadData();
      } else {
        message.error(response.message || '取消报名失败');
      }
    } catch (error: any) {
      console.error('取消报名失败:', error);
      message.error(error.message || '取消报名失败');
    }
  };

  // 去支付
  const handlePay = (record: Registration) => {
    // 跳转到支付页面，传递报名ID
    navigate(`/user/payment/${record.id}`);
  };

  // 审核状态标签
  const getAuditStatusTag = (status: number) => {
    const statusMap: Record<number, { text: string; color: string }> = {
      1: { text: '待审核', color: 'orange' },
      2: { text: '已通过', color: 'green' },
      3: { text: '已驳回', color: 'red' },
    };
    const config = statusMap[status] || { text: '未知', color: 'default' };
    return <Tag color={config.color}>{config.text}</Tag>;
  };

  // 缴费状态标签
  const getPaymentStatusTag = (status: number) => {
    const statusMap: Record<number, { text: string; color: string }> = {
      1: { text: '待缴费', color: 'orange' },
      2: { text: '已缴费', color: 'green' },
      3: { text: '已退费', color: 'blue' },
    };
    const config = statusMap[status] || { text: '未知', color: 'default' };
    return <Tag color={config.color}>{config.text}</Tag>;
  };

  // 表格列定义
  const columns = [
    {
      title: '报名编号',
      dataIndex: 'id',
      width: 100,
    },
    {
      title: '考试名称',
      dataIndex: 'examName',
      width: 200,
      ellipsis: true,
    },
    {
      title: '考试时间',
      dataIndex: 'examDate',
      width: 180,
      render: (text: string, record: Registration) => {
        return `${record.examDate || ''} ${record.examTime || ''}`;
      },
    },
    {
      title: '考点',
      dataIndex: 'siteName',
      width: 150,
      ellipsis: true,
    },
    {
      title: '报名时间',
      dataIndex: 'createTime',
      width: 180,
    },
    {
      title: '审核状态',
      dataIndex: 'auditStatus',
      width: 100,
      render: (status: number) => getAuditStatusTag(status),
    },
    {
      title: '缴费状态',
      dataIndex: 'paymentStatus',
      width: 100,
      render: (status: number) => getPaymentStatusTag(status),
    },
    {
      title: '报名费',
      dataIndex: 'fee',
      width: 100,
      render: (fee: number) => (fee ? `¥${fee}` : '-'),
    },
    {
      title: '操作',
      width: 250,
      fixed: 'right' as const,
      render: (_: any, record: Registration) => (
        <Space>
          <Button
            type="link"
            size="small"
            icon={<EyeOutlined />}
            onClick={() => handleViewDetail(record)}
          >
            查看
          </Button>

          {/* 审核通过且待缴费，显示去支付按钮 */}
          {record.auditStatus === 2 && record.paymentStatus === 1 && (
            <Button
              type="primary"
              size="small"
              icon={<DollarOutlined />}
              onClick={() => handlePay(record)}
            >
              去支付
            </Button>
          )}

          {/* 待审核状态可以取消 */}
          {record.auditStatus === 1 && (
            <Popconfirm
              title="确定要取消这条报名吗？"
              onConfirm={() => handleCancel(record.id)}
              okText="确定"
              cancelText="取消"
            >
              <Button
                type="link"
                size="small"
                danger
                icon={<CloseCircleOutlined />}
              >
                取消报名
              </Button>
            </Popconfirm>
          )}

          {/* 已缴费显示准考证号 */}
          {record.paymentStatus === 2 && record.admissionTicketNo && (
            <Button
              type="link"
              size="small"
              icon={<FileTextOutlined />}
              onClick={() =>
                message.info(`准考证号：${record.admissionTicketNo}`)
              }
            >
              准考证
            </Button>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div>
      <Card
        title="我的报名记录"
        extra={
          <Space>
            <Button
              type="primary"
              onClick={() => navigate('/user/apply')}
            >
              去报名
            </Button>
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
            description="暂无报名记录"
            image={Empty.PRESENTED_IMAGE_SIMPLE}
          >
            <Button type="primary" onClick={() => navigate('/user/apply')}>
              立即报名
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
            scroll={{ x: 1400 }}
          />
        )}
      </Card>

      {/* 详情弹窗 */}
      <Modal
        title="报名详情"
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
            <Descriptions.Item label="报名编号" span={1}>
              {detailData.id}
            </Descriptions.Item>
            <Descriptions.Item label="报名时间" span={1}>
              {detailData.createTime || '-'}
            </Descriptions.Item>

            <Descriptions.Item label="考试名称" span={2}>
              {detailData.exam?.examName || '-'}
            </Descriptions.Item>
            <Descriptions.Item label="考试时间" span={2}>
              {detailData.exam?.examDate
                ? `${detailData.exam.examDate} ${detailData.exam.examTime || ''}`
                : '-'}
            </Descriptions.Item>
            <Descriptions.Item label="考试类型" span={1}>
              {detailData.exam?.examType || '-'}
            </Descriptions.Item>
            <Descriptions.Item label="报名费" span={1}>
              <span style={{ fontSize: 18, color: '#ff4d4f', fontWeight: 'bold' }}>
                ¥{detailData.fee || 0}
              </span>
            </Descriptions.Item>

            <Descriptions.Item label="考点名称" span={2}>
              {detailData.site?.siteName || '-'}
            </Descriptions.Item>
            <Descriptions.Item label="考点地址" span={2}>
              {detailData.site
                ? `${detailData.site.province} ${detailData.site.city} ${detailData.site.district} ${detailData.site.address}`
                : '-'}
            </Descriptions.Item>

            <Descriptions.Item label="报考科目" span={2}>
              {detailData.subject || '-'}
            </Descriptions.Item>

            <Descriptions.Item label="手机号" span={1}>
              {detailData.phoneMasked || detailData.phone || '-'}
            </Descriptions.Item>
            <Descriptions.Item label="身份证号" span={1}>
              {detailData.idCardMasked || detailData.idCard || '-'}
            </Descriptions.Item>

            <Descriptions.Item label="审核状态" span={1}>
              {getAuditStatusTag(detailData.auditStatus)}
            </Descriptions.Item>
            <Descriptions.Item label="审核时间" span={1}>
              {detailData.auditTime || '-'}
            </Descriptions.Item>

            {detailData.auditRemark && (
              <Descriptions.Item label="审核备注" span={2}>
                <span style={{ color: detailData.auditStatus === 3 ? '#ff4d4f' : 'inherit' }}>
                  {detailData.auditRemark}
                </span>
              </Descriptions.Item>
            )}

            <Descriptions.Item label="缴费状态" span={1}>
              {getPaymentStatusTag(detailData.paymentStatus)}
            </Descriptions.Item>
            <Descriptions.Item label="缴费时间" span={1}>
              {detailData.paymentTime || '-'}
            </Descriptions.Item>

            {detailData.admissionTicketNo && (
              <Descriptions.Item label="准考证号" span={2}>
                <span style={{ fontSize: 16, fontWeight: 'bold', color: '#1890ff' }}>
                  {detailData.admissionTicketNo}
                </span>
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
