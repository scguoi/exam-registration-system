import React, { useState, useEffect } from 'react';
import {
  Card,
  Descriptions,
  Radio,
  Button,
  Result,
  Spin,
  message,
  Alert,
  Space,
  Statistic,
} from 'antd';
import {
  AlipayCircleOutlined,
  WechatOutlined,
  DollarOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
} from '@ant-design/icons';
import { useParams, useNavigate } from 'react-router-dom';
import {
  getOrderByRegistrationId,
  executePay,
  type PayRequest,
} from '../../services/payment';
import { getRegistrationDetail } from '../../services/registration';

const { Countdown } = Statistic;

/**
 * 支付页面
 */
export default function Payment() {
  const { registrationId } = useParams<{ registrationId: string }>();
  const navigate = useNavigate();

  const [loading, setLoading] = useState(true);
  const [paying, setPaying] = useState(false);
  const [paymentMethod, setPaymentMethod] = useState<string>('mock');

  const [orderData, setOrderData] = useState<any>(null);
  const [registrationData, setRegistrationData] = useState<any>(null);
  const [paySuccess, setPaySuccess] = useState(false);

  // 加载订单和报名信息
  useEffect(() => {
    if (!registrationId) {
      message.error('报名ID不能为空');
      navigate('/user/registrations');
      return;
    }

    loadData();
  }, [registrationId]);

  const loadData = async () => {
    setLoading(true);
    try {
      // 1. 查询报名详情
      const regResponse: any = await getRegistrationDetail(Number(registrationId));
      if (regResponse.code !== 200) {
        message.error(regResponse.message || '加载报名信息失败');
        return;
      }
      setRegistrationData(regResponse.data);

      // 2. 查询支付订单
      const orderResponse: any = await getOrderByRegistrationId(Number(registrationId));
      if (orderResponse.code !== 200) {
        message.error(orderResponse.message || '加载订单信息失败');
        return;
      }

      const order = orderResponse.data;
      setOrderData(order);

      // 3. 如果订单已支付，显示成功状态
      if (order.status === 2) {
        setPaySuccess(true);
      }
    } catch (error: any) {
      console.error('加载数据失败:', error);
      message.error(error.message || '加载数据失败');
    } finally {
      setLoading(false);
    }
  };

  // 执行支付
  const handlePay = async () => {
    if (!orderData) {
      message.error('订单信息不存在');
      return;
    }

    // 检查订单状态
    if (orderData.status !== 1) {
      message.warning('订单状态不正确，无法支付');
      return;
    }

    setPaying(true);
    try {
      const payRequest: PayRequest = {
        orderNo: orderData.orderNo,
        paymentMethod: paymentMethod,
      };

      const response: any = await executePay(payRequest);

      if (response.code === 200) {
        message.success('支付成功！');
        setPaySuccess(true);
        // 2秒后刷新页面数据
        setTimeout(() => {
          loadData();
        }, 2000);
      } else {
        message.error(response.message || '支付失败');
      }
    } catch (error: any) {
      console.error('支付失败:', error);
      message.error(error.message || '支付失败');
    } finally {
      setPaying(false);
    }
  };

  // 订单状态标签
  const getStatusTag = (status: number) => {
    const statusMap: Record<number, { text: string; color: string }> = {
      1: { text: '待支付', color: 'orange' },
      2: { text: '已支付', color: 'green' },
      3: { text: '已关闭', color: 'gray' },
      4: { text: '已退款', color: 'blue' },
    };
    return statusMap[status] || { text: '未知', color: 'default' };
  };

  // 加载中
  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: '100px 0' }}>
        <Spin size="large" tip="加载订单信息中..." />
      </div>
    );
  }

  // 支付成功页面
  if (paySuccess && orderData?.status === 2) {
    return (
      <Card>
        <Result
          status="success"
          title="支付成功！"
          subTitle={`订单号：${orderData.orderNo}`}
          extra={[
            <Button type="primary" key="registrations" onClick={() => navigate('/user/registrations')}>
              查看我的报名
            </Button>,
            <Button key="dashboard" onClick={() => navigate('/user/dashboard')}>
              返回首页
            </Button>,
          ]}
        >
          <div style={{ marginTop: 24 }}>
            <Descriptions bordered column={2}>
              <Descriptions.Item label="考试名称" span={2}>
                {registrationData?.exam?.examName}
              </Descriptions.Item>
              <Descriptions.Item label="考试时间" span={2}>
                {registrationData?.exam?.examDate} {registrationData?.exam?.examTime || ''}
              </Descriptions.Item>
              <Descriptions.Item label="支付金额" span={1}>
                <span style={{ fontSize: 18, color: '#52c41a', fontWeight: 'bold' }}>
                  ¥{orderData.amount}
                </span>
              </Descriptions.Item>
              <Descriptions.Item label="支付时间" span={1}>
                {orderData.payTime}
              </Descriptions.Item>
              <Descriptions.Item label="准考证号" span={2}>
                <span style={{ fontSize: 16, fontWeight: 'bold', color: '#1890ff' }}>
                  {registrationData?.admissionTicketNo || '生成中...'}
                </span>
              </Descriptions.Item>
              <Descriptions.Item label="交易流水号" span={2}>
                {orderData.transactionId}
              </Descriptions.Item>
            </Descriptions>

            <Alert
              message="温馨提示"
              description="请妥善保管您的准考证号，考试当天凭准考证号和身份证入场。"
              type="info"
              showIcon
              style={{ marginTop: 16 }}
            />
          </div>
        </Result>
      </Card>
    );
  }

  // 订单已关闭或已退款
  if (orderData && (orderData.status === 3 || orderData.status === 4)) {
    const statusConfig = getStatusTag(orderData.status);
    return (
      <Card>
        <Result
          status="warning"
          title={`订单${statusConfig.text}`}
          subTitle={`订单号：${orderData.orderNo}`}
          extra={[
            <Button type="primary" key="registrations" onClick={() => navigate('/user/registrations')}>
              返回我的报名
            </Button>,
          ]}
        />
      </Card>
    );
  }

  // 待支付页面
  return (
    <div>
      <Card title="确认支付信息" style={{ marginBottom: 16 }}>
        <Descriptions bordered column={2}>
          <Descriptions.Item label="订单号" span={2}>
            {orderData?.orderNo}
          </Descriptions.Item>
          <Descriptions.Item label="考试名称" span={2}>
            {registrationData?.exam?.examName}
          </Descriptions.Item>
          <Descriptions.Item label="考试时间" span={2}>
            {registrationData?.exam?.examDate} {registrationData?.exam?.examTime || ''}
          </Descriptions.Item>
          <Descriptions.Item label="考点" span={2}>
            {registrationData?.site?.siteName}
          </Descriptions.Item>
          <Descriptions.Item label="考点地址" span={2}>
            {registrationData?.site
              ? `${registrationData.site.province} ${registrationData.site.city} ${registrationData.site.district} ${registrationData.site.address}`
              : '-'}
          </Descriptions.Item>
          <Descriptions.Item label="应付金额" span={1}>
            <span style={{ fontSize: 24, color: '#ff4d4f', fontWeight: 'bold' }}>
              ¥{orderData?.amount}
            </span>
          </Descriptions.Item>
          <Descriptions.Item label="订单状态" span={1}>
            <span style={{ color: getStatusTag(orderData?.status || 1).color }}>
              {getStatusTag(orderData?.status || 1).text}
            </span>
          </Descriptions.Item>
        </Descriptions>

        {orderData?.expireTime && (
          <Alert
            message={
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                <span>
                  <ClockCircleOutlined style={{ marginRight: 8 }} />
                  订单将在 30 分钟后过期，请尽快完成支付
                </span>
                <Countdown
                  value={new Date(orderData.expireTime).getTime()}
                  format="mm:ss"
                  valueStyle={{ fontSize: 16, color: '#ff4d4f' }}
                />
              </div>
            }
            type="warning"
            style={{ marginTop: 16 }}
          />
        )}
      </Card>

      <Card title="选择支付方式">
        <Radio.Group
          value={paymentMethod}
          onChange={(e) => setPaymentMethod(e.target.value)}
          style={{ width: '100%' }}
        >
          <Space direction="vertical" style={{ width: '100%' }} size="middle">
            <Card
              hoverable
              style={{
                border: paymentMethod === 'mock' ? '2px solid #1890ff' : '1px solid #d9d9d9',
              }}
            >
              <Radio value="mock">
                <div style={{ display: 'flex', alignItems: 'center', marginLeft: 8 }}>
                  <DollarOutlined style={{ fontSize: 24, color: '#1890ff', marginRight: 12 }} />
                  <div>
                    <div style={{ fontSize: 16, fontWeight: 'bold' }}>Mock支付（测试）</div>
                    <div style={{ fontSize: 12, color: '#666' }}>点击即支付成功，用于测试演示</div>
                  </div>
                </div>
              </Radio>
            </Card>

            <Card
              hoverable
              style={{
                border: paymentMethod === 'alipay' ? '2px solid #1890ff' : '1px solid #d9d9d9',
              }}
            >
              <Radio value="alipay" disabled>
                <div style={{ display: 'flex', alignItems: 'center', marginLeft: 8 }}>
                  <AlipayCircleOutlined style={{ fontSize: 24, color: '#1890ff', marginRight: 12 }} />
                  <div>
                    <div style={{ fontSize: 16, fontWeight: 'bold' }}>支付宝支付（暂未开通）</div>
                    <div style={{ fontSize: 12, color: '#666' }}>安全便捷，支持花呗分期</div>
                  </div>
                </div>
              </Radio>
            </Card>

            <Card
              hoverable
              style={{
                border: paymentMethod === 'wechat' ? '2px solid #1890ff' : '1px solid #d9d9d9',
              }}
            >
              <Radio value="wechat" disabled>
                <div style={{ display: 'flex', alignItems: 'center', marginLeft: 8 }}>
                  <WechatOutlined style={{ fontSize: 24, color: '#09bb07', marginRight: 12 }} />
                  <div>
                    <div style={{ fontSize: 16, fontWeight: 'bold' }}>微信支付（暂未开通）</div>
                    <div style={{ fontSize: 12, color: '#666' }}>微信扫码支付</div>
                  </div>
                </div>
              </Radio>
            </Card>
          </Space>
        </Radio.Group>

        <div style={{ marginTop: 24, textAlign: 'center' }}>
          <Space size="large">
            <Button size="large" onClick={() => navigate('/user/registrations')}>
              取消
            </Button>
            <Button
              type="primary"
              size="large"
              icon={<CheckCircleOutlined />}
              loading={paying}
              onClick={handlePay}
              style={{ minWidth: 160 }}
            >
              确认支付 ¥{orderData?.amount}
            </Button>
          </Space>
        </div>

        <Alert
          message="温馨提示"
          description="这是一个演示系统，使用Mock支付方式，点击确认支付即可完成支付流程。"
          type="info"
          showIcon
          style={{ marginTop: 16 }}
        />
      </Card>
    </div>
  );
}
