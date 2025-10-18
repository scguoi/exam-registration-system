/**
 * 数据统计页面（管理端）
 *
 * 依赖安装：
 * npm install echarts echarts-for-react
 * 或
 * yarn add echarts echarts-for-react
 */

import React, { useState, useEffect } from 'react';
import {
  Card,
  Row,
  Col,
  Statistic,
  Spin,
  message,
  Select,
  Space,
  Tabs,
} from 'antd';
import {
  UserOutlined,
  FileTextOutlined,
  DollarOutlined,
  BookOutlined,
  ReloadOutlined,
} from '@ant-design/icons';
// import ReactECharts from 'echarts-for-react'; // 需要安装 echarts-for-react
import {
  getDashboardStats,
  getExamDetailStats,
} from '../../services/statistics';

const { Option } = Select;

/**
 * 数据统计页面
 *
 * 注意：此页面使用 ECharts 进行数据可视化
 * 请先安装依赖：npm install echarts echarts-for-react
 */
export default function Statistics() {
  const [loading, setLoading] = useState(false);
  const [dashboardData, setDashboardData] = useState<any>(null);
  const [examDetailData, setExamDetailData] = useState<any[]>([]);

  // 加载仪表盘数据
  const loadDashboardData = async () => {
    setLoading(true);
    try {
      const response: any = await getDashboardStats();
      if (response.code === 200) {
        setDashboardData(response.data);
      } else {
        message.error(response.message || '加载统计数据失败');
      }
    } catch (error: any) {
      console.error('加载统计数据失败:', error);
      message.error(error.message || '加载统计数据失败');
    } finally {
      setLoading(false);
    }
  };

  // 加载按考试统计数据
  const loadExamDetailData = async () => {
    try {
      const response: any = await getExamDetailStats();
      if (response.code === 200) {
        setExamDetailData(response.data || []);
      } else {
        message.error(response.message || '加载考试统计失败');
      }
    } catch (error: any) {
      console.error('加载考试统计失败:', error);
      message.error(error.message || '加载考试统计失败');
    }
  };

  useEffect(() => {
    loadDashboardData();
    loadExamDetailData();
  }, []);

  if (loading || !dashboardData) {
    return (
      <div style={{ textAlign: 'center', padding: '100px 0' }}>
        <Spin size="large" tip="加载统计数据中..." />
      </div>
    );
  }

  const { examStats, registrationStats, paymentStats, userStats, registrationTrend, paymentTrend } = dashboardData;

  // ==================== ECharts 配置 ====================
  // 注意：以下代码需要安装 echarts-for-react 才能正常使用

  // 报名趋势图配置
  const registrationTrendOption = {
    title: {
      text: '报名趋势（最近30天）',
    },
    tooltip: {
      trigger: 'axis',
    },
    xAxis: {
      type: 'category',
      data: registrationTrend?.map((item: any) => item.date) || [],
    },
    yAxis: {
      type: 'value',
    },
    series: [
      {
        name: '报名人数',
        type: 'line',
        smooth: true,
        data: registrationTrend?.map((item: any) => item.count) || [],
        itemStyle: { color: '#1890ff' },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(24, 144, 255, 0.3)' },
              { offset: 1, color: 'rgba(24, 144, 255, 0)' },
            ],
          },
        },
      },
    ],
  };

  // 缴费趋势图配置
  const paymentTrendOption = {
    title: {
      text: '缴费趋势（最近30天）',
    },
    tooltip: {
      trigger: 'axis',
    },
    xAxis: {
      type: 'category',
      data: paymentTrend?.map((item: any) => item.date) || [],
    },
    yAxis: [
      {
        type: 'value',
        name: '金额（元）',
        position: 'left',
      },
      {
        type: 'value',
        name: '订单数',
        position: 'right',
      },
    ],
    series: [
      {
        name: '缴费金额',
        type: 'bar',
        yAxisIndex: 0,
        data: paymentTrend?.map((item: any) => item.amount) || [],
        itemStyle: { color: '#52c41a' },
      },
      {
        name: '订单数',
        type: 'line',
        yAxisIndex: 1,
        data: paymentTrend?.map((item: any) => item.count) || [],
        itemStyle: { color: '#faad14' },
      },
    ],
  };

  // 报名状态饼图配置
  const registrationStatusOption = {
    title: {
      text: '报名审核状态分布',
    },
    tooltip: {
      trigger: 'item',
    },
    legend: {
      orient: 'vertical',
      left: 'left',
    },
    series: [
      {
        name: '报名状态',
        type: 'pie',
        radius: '50%',
        data: [
          { value: registrationStats?.pendingCount || 0, name: '待审核' },
          { value: registrationStats?.approvedCount || 0, name: '已通过' },
          { value: registrationStats?.rejectedCount || 0, name: '已驳回' },
        ],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)',
          },
        },
      },
    ],
  };

  // 缴费状态饼图配置
  const paymentStatusOption = {
    title: {
      text: '缴费状态分布',
    },
    tooltip: {
      trigger: 'item',
    },
    legend: {
      orient: 'vertical',
      left: 'left',
    },
    series: [
      {
        name: '缴费状态',
        type: 'pie',
        radius: '50%',
        data: [
          { value: paymentStats?.paidCount || 0, name: '已缴费' },
          { value: paymentStats?.unpaidCount || 0, name: '待缴费' },
          { value: paymentStats?.refundedCount || 0, name: '已退款' },
        ],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)',
          },
        },
      },
    ],
  };

  return (
    <div>
      {/* 关键指标卡片 */}
      <Row gutter={16} style={{ marginBottom: 16 }}>
        <Col span={6}>
          <Card>
            <Statistic
              title="考试总数"
              value={examStats?.totalCount || 0}
              prefix={<BookOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
            <div style={{ marginTop: 8, fontSize: 12, color: '#666' }}>
              报名中：{examStats?.registrationOpenCount || 0}
            </div>
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="报名总数"
              value={registrationStats?.totalCount || 0}
              prefix={<FileTextOutlined />}
              valueStyle={{ color: '#52c41a' }}
            />
            <div style={{ marginTop: 8, fontSize: 12, color: '#666' }}>
              待审核：{registrationStats?.pendingCount || 0}
            </div>
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="缴费总额"
              value={paymentStats?.paidAmount || 0}
              prefix={<DollarOutlined />}
              precision={2}
              valueStyle={{ color: '#faad14' }}
              suffix="元"
            />
            <div style={{ marginTop: 8, fontSize: 12, color: '#666' }}>
              订单数：{paymentStats?.paidCount || 0}
            </div>
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="用户总数"
              value={userStats?.totalCount || 0}
              prefix={<UserOutlined />}
              valueStyle={{ color: '#eb2f96' }}
            />
            <div style={{ marginTop: 8, fontSize: 12, color: '#666' }}>
              考生：{userStats?.userCount || 0} | 管理员：{userStats?.adminCount || 0}
            </div>
          </Card>
        </Col>
      </Row>

      {/* 趋势图表 */}
      <Row gutter={16} style={{ marginBottom: 16 }}>
        <Col span={12}>
          <Card title="报名趋势">
            {/*
              使用 ECharts 需要先安装依赖：
              npm install echarts echarts-for-react

              然后取消注释下面的代码：
              <ReactECharts option={registrationTrendOption} style={{ height: 300 }} />
            */}
            <div style={{ height: 300, display: 'flex', alignItems: 'center', justifyContent: 'center', background: '#f5f5f5' }}>
              <div style={{ textAlign: 'center' }}>
                <p>📊 报名趋势图</p>
                <p style={{ fontSize: 12, color: '#666' }}>
                  请安装 ECharts：<br />
                  <code style={{ background: '#fff', padding: '2px 6px', borderRadius: 4 }}>
                    npm install echarts echarts-for-react
                  </code>
                </p>
              </div>
            </div>
          </Card>
        </Col>
        <Col span={12}>
          <Card title="缴费趋势">
            <div style={{ height: 300, display: 'flex', alignItems: 'center', justifyContent: 'center', background: '#f5f5f5' }}>
              <div style={{ textAlign: 'center' }}>
                <p>📊 缴费趋势图</p>
                <p style={{ fontSize: 12, color: '#666' }}>
                  请安装 ECharts：<br />
                  <code style={{ background: '#fff', padding: '2px 6px', borderRadius: 4 }}>
                    npm install echarts echarts-for-react
                  </code>
                </p>
              </div>
            </div>
          </Card>
        </Col>
      </Row>

      {/* 状态分布图表 */}
      <Row gutter={16} style={{ marginBottom: 16 }}>
        <Col span={12}>
          <Card title="报名审核状态分布">
            <div style={{ height: 300, display: 'flex', alignItems: 'center', justifyContent: 'center', background: '#f5f5f5' }}>
              <div style={{ textAlign: 'center' }}>
                <p>🥧 报名状态饼图</p>
                <p style={{ fontSize: 12, color: '#666' }}>
                  待审核：{registrationStats?.pendingCount || 0} |
                  已通过：{registrationStats?.approvedCount || 0} |
                  已驳回：{registrationStats?.rejectedCount || 0}
                </p>
              </div>
            </div>
          </Card>
        </Col>
        <Col span={12}>
          <Card title="缴费状态分布">
            <div style={{ height: 300, display: 'flex', alignItems: 'center', justifyContent: 'center', background: '#f5f5f5' }}>
              <div style={{ textAlign: 'center' }}>
                <p>🥧 缴费状态饼图</p>
                <p style={{ fontSize: 12, color: '#666' }}>
                  已缴费：{paymentStats?.paidCount || 0} |
                  待缴费：{paymentStats?.unpaidCount || 0} |
                  已退款：{paymentStats?.refundedCount || 0}
                </p>
              </div>
            </div>
          </Card>
        </Col>
      </Row>

      {/* 按考试统计详情 */}
      <Card title="按考试统计详情">
        {examDetailData.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '50px 0', color: '#999' }}>
            暂无考试数据
          </div>
        ) : (
          <div>
            {examDetailData.map((exam: any) => (
              <Card
                key={exam.examId}
                type="inner"
                title={exam.examName}
                extra={`考试日期：${exam.examDate || '-'}`}
                style={{ marginBottom: 16 }}
              >
                <Row gutter={16}>
                  <Col span={8}>
                    <Card>
                      <Statistic
                        title="报名总数"
                        value={exam.registrationStats?.totalCount || 0}
                        valueStyle={{ color: '#1890ff' }}
                      />
                      <div style={{ marginTop: 8, fontSize: 12 }}>
                        待审核：{exam.registrationStats?.pendingCount || 0} |
                        已通过：{exam.registrationStats?.approvedCount || 0}
                      </div>
                    </Card>
                  </Col>
                  <Col span={8}>
                    <Card>
                      <Statistic
                        title="已缴费人数"
                        value={exam.registrationStats?.paidCount || 0}
                        valueStyle={{ color: '#52c41a' }}
                      />
                      <div style={{ marginTop: 8, fontSize: 12 }}>
                        未缴费：{exam.registrationStats?.unpaidCount || 0}
                      </div>
                    </Card>
                  </Col>
                  <Col span={8}>
                    <Card>
                      <Statistic
                        title="缴费总额"
                        value={exam.paymentStats?.paidAmount || 0}
                        precision={2}
                        suffix="元"
                        valueStyle={{ color: '#faad14' }}
                      />
                      <div style={{ marginTop: 8, fontSize: 12 }}>
                        报名费：¥{exam.fee || 0}
                      </div>
                    </Card>
                  </Col>
                </Row>
              </Card>
            ))}
          </div>
        )}
      </Card>
    </div>
  );
}
