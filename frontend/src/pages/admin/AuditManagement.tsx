import React, { useState, useEffect } from 'react';
import {
  Table,
  Button,
  Tag,
  Modal,
  Form,
  Input,
  Select,
  message,
  Space,
  Card,
  Descriptions,
  Radio,
  Spin,
} from 'antd';
import { EyeOutlined, CheckOutlined, CloseOutlined, ReloadOutlined } from '@ant-design/icons';
import {
  getPendingRegistrations,
  auditRegistration,
  getRegistrationDetail,
  type Registration,
  type AuditRequest,
  type PageResult,
} from '../../services/registration';

const { TextArea } = Input;
const { Option } = Select;

/**
 * 报名审核管理页面（管理端）
 */
export default function AuditManagement() {
  const [loading, setLoading] = useState(false);
  const [dataSource, setDataSource] = useState<Registration[]>([]);
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0,
  });
  const [filters, setFilters] = useState<{ examId?: number }>({});

  // 审核弹窗
  const [auditModalVisible, setAuditModalVisible] = useState(false);
  const [currentRecord, setCurrentRecord] = useState<Registration | null>(null);
  const [auditForm] = Form.useForm();
  const [auditing, setAuditing] = useState(false);

  // 详情弹窗
  const [detailModalVisible, setDetailModalVisible] = useState(false);
  const [detailData, setDetailData] = useState<any>(null);
  const [detailLoading, setDetailLoading] = useState(false);

  // 加载数据
  const loadData = async (page = 1, pageSize = 10) => {
    setLoading(true);
    try {
      const response: any = await getPendingRegistrations(page, pageSize, filters.examId);

      if (response.code === 200 && response.data) {
        setDataSource(response.data.records || []);
        setPagination({
          current: response.data.current || page,
          pageSize: response.data.size || pageSize,
          total: response.data.total || 0,
        });
      } else {
        message.error(response.message || '加载数据失败');
      }
    } catch (error: any) {
      console.error('加载数据失败:', error);
      message.error(error.message || '加载数据失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [filters]);

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

  // 打开审核弹窗
  const handleAudit = (record: Registration) => {
    setCurrentRecord(record);
    setAuditModalVisible(true);
    auditForm.resetFields();
  };

  // 提交审核
  const handleSubmitAudit = async () => {
    if (!currentRecord) return;

    try {
      const values = await auditForm.validateFields();

      if (values.auditStatus === 3 && !values.auditRemark) {
        message.error('驳回时必须填写原因');
        return;
      }

      setAuditing(true);
      const auditData: AuditRequest = {
        auditStatus: values.auditStatus,
        auditRemark: values.auditRemark,
      };

      const response: any = await auditRegistration(currentRecord.id, auditData);

      if (response.code === 200) {
        message.success(values.auditStatus === 2 ? '审核通过' : '已驳回');
        setAuditModalVisible(false);
        loadData(pagination.current, pagination.pageSize);
      } else {
        message.error(response.message || '审核失败');
      }
    } catch (error: any) {
      if (error.errorFields) {
        // 表单验证错误
        return;
      }
      console.error('审核失败:', error);
      message.error(error.message || '审核失败');
    } finally {
      setAuditing(false);
    }
  };

  // 表格列定义
  const columns = [
    {
      title: 'ID',
      dataIndex: 'id',
      width: 80,
    },
    {
      title: '考生姓名',
      dataIndex: 'userRealName',
      width: 120,
    },
    {
      title: '手机号',
      dataIndex: 'phoneMasked',
      width: 130,
      render: (text: string, record: Registration) => {
        return record.phoneMasked || record.phone || '-';
      },
    },
    {
      title: '考试名称',
      dataIndex: 'examName',
      width: 200,
      ellipsis: true,
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
      title: '状态',
      dataIndex: 'auditStatus',
      width: 100,
      render: (status: number) => {
        const statusMap: Record<number, { text: string; color: string }> = {
          1: { text: '待审核', color: 'orange' },
          2: { text: '已通过', color: 'green' },
          3: { text: '已驳回', color: 'red' },
        };
        const config = statusMap[status] || { text: '未知', color: 'default' };
        return <Tag color={config.color}>{config.text}</Tag>;
      },
    },
    {
      title: '操作',
      width: 200,
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
          <Button
            type="primary"
            size="small"
            icon={<CheckOutlined />}
            onClick={() => handleAudit(record)}
          >
            审核
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <Card
        title="报名审核管理"
        extra={
          <Space>
            {/*
            <Select
              placeholder="选择考试"
              style={{ width: 200 }}
              allowClear
              onChange={(value) => setFilters({ examId: value })}
            >
              <Option value={1}>2025年成人高考</Option>
            </Select>
            */}
            <Button
              icon={<ReloadOutlined />}
              onClick={() => loadData(pagination.current, pagination.pageSize)}
            >
              刷新
            </Button>
          </Space>
        }
      >
        <Table
          columns={columns}
          dataSource={dataSource}
          loading={loading}
          rowKey="id"
          pagination={{
            ...pagination,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total) => `共 ${total} 条`,
          }}
          onChange={(page) => {
            loadData(page.current, page.pageSize);
          }}
          scroll={{ x: 1200 }}
        />
      </Card>

      {/* 审核弹窗 */}
      <Modal
        title="审核报名"
        open={auditModalVisible}
        onOk={handleSubmitAudit}
        onCancel={() => setAuditModalVisible(false)}
        width={500}
        confirmLoading={auditing}
        okText="提交"
        cancelText="取消"
      >
        <Form form={auditForm} layout="vertical">
          <Form.Item
            name="auditStatus"
            label="审核结果"
            rules={[{ required: true, message: '请选择审核结果' }]}
          >
            <Radio.Group>
              <Radio value={2}>通过</Radio>
              <Radio value={3}>驳回</Radio>
            </Radio.Group>
          </Form.Item>

          <Form.Item noStyle shouldUpdate={(prevValues, currentValues) => prevValues.auditStatus !== currentValues.auditStatus}>
            {({ getFieldValue }) =>
              getFieldValue('auditStatus') === 3 ? (
                <Form.Item
                  name="auditRemark"
                  label="驳回原因"
                  rules={[{ required: true, message: '请填写驳回原因' }]}
                >
                  <TextArea rows={4} placeholder="请详细说明驳回原因" />
                </Form.Item>
              ) : null
            }
          </Form.Item>

          {currentRecord && (
            <div style={{ marginTop: 16, padding: 12, background: '#f5f5f5', borderRadius: 4 }}>
              <div><strong>考生姓名：</strong>{currentRecord.userRealName}</div>
              <div><strong>考试名称：</strong>{currentRecord.examName}</div>
              <div><strong>考点：</strong>{currentRecord.siteName}</div>
            </div>
          )}
        </Form>
      </Modal>

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
            <Descriptions.Item label="考生姓名" span={1}>
              {detailData.user?.realName || '-'}
            </Descriptions.Item>
            <Descriptions.Item label="用户名" span={1}>
              {detailData.user?.username || '-'}
            </Descriptions.Item>
            <Descriptions.Item label="手机号" span={1}>
              {detailData.phone || '-'}
            </Descriptions.Item>
            <Descriptions.Item label="身份证号" span={1}>
              {detailData.idCard || '-'}
            </Descriptions.Item>
            <Descriptions.Item label="考试名称" span={2}>
              {detailData.exam?.examName || '-'}
            </Descriptions.Item>
            <Descriptions.Item label="考试时间" span={2}>
              {detailData.exam?.examDate ? `${detailData.exam.examDate} ${detailData.exam.examTime || ''}` : '-'}
            </Descriptions.Item>
            <Descriptions.Item label="考点名称" span={2}>
              {detailData.site?.siteName || '-'}
            </Descriptions.Item>
            <Descriptions.Item label="考点地址" span={2}>
              {detailData.site?.address || '-'}
            </Descriptions.Item>
            <Descriptions.Item label="报考科目" span={2}>
              {detailData.subject || '-'}
            </Descriptions.Item>
            <Descriptions.Item label="报名时间" span={2}>
              {detailData.createTime || '-'}
            </Descriptions.Item>
            <Descriptions.Item label="审核状态" span={1}>
              <Tag color={detailData.auditStatus === 1 ? 'orange' : detailData.auditStatus === 2 ? 'green' : 'red'}>
                {detailData.auditStatusText || '-'}
              </Tag>
            </Descriptions.Item>
            <Descriptions.Item label="审核时间" span={1}>
              {detailData.auditTime || '-'}
            </Descriptions.Item>
            {detailData.auditRemark && (
              <Descriptions.Item label="审核备注" span={2}>
                {detailData.auditRemark}
              </Descriptions.Item>
            )}
            {detailData.auditorName && (
              <Descriptions.Item label="审核人" span={2}>
                {detailData.auditorName}
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
