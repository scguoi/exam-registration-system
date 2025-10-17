import React, { useState, useEffect } from 'react';
import {
  Steps,
  Card,
  Form,
  Input,
  Select,
  Button,
  message,
  Descriptions,
  Space,
  Radio,
  Alert,
  Spin,
  Empty,
} from 'antd';
import { useNavigate } from 'react-router-dom';
import { getExamList, getExamSiteList } from '../../services/exam';
import { submitRegistration, type RegistrationRequest } from '../../services/registration';

const { Step } = Steps;
const { Option } = Select;
const { TextArea } = Input;

/**
 * 考生报名流程页面
 */
export default function ApplyExam() {
  const navigate = useNavigate();
  const [form] = Form.useForm();

  const [current, setCurrent] = useState(0);
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  // 数据状态
  const [exams, setExams] = useState<any[]>([]);
  const [sites, setSites] = useState<any[]>([]);
  const [selectedExam, setSelectedExam] = useState<any>(null);
  const [selectedSite, setSelectedSite] = useState<any>(null);
  const [formData, setFormData] = useState<Partial<RegistrationRequest>>({});

  // 加载可报名的考试列表
  useEffect(() => {
    loadExams();
  }, []);

  const loadExams = async () => {
    setLoading(true);
    try {
      const response: any = await getExamList({
        status: 3, // 报名中的考试
        pageNum: 1,
        pageSize: 100,
      });

      if (response.code === 200 && response.data) {
        setExams(response.data.records || response.data || []);
      } else {
        message.error(response.message || '加载考试列表失败');
      }
    } catch (error: any) {
      console.error('加载考试失败:', error);
      message.error(error.message || '加载考试列表失败');
    } finally {
      setLoading(false);
    }
  };

  // 加载考点列表
  const loadSites = async (examId: number) => {
    setLoading(true);
    try {
      const response: any = await getExamSiteList(examId);

      if (response.code === 200) {
        setSites(response.data || []);
      } else {
        message.error(response.message || '加载考点列表失败');
      }
    } catch (error: any) {
      console.error('加载考点失败:', error);
      message.error(error.message || '加载考点列表失败');
    } finally {
      setLoading(false);
    }
  };

  // 选择考试
  const handleSelectExam = (examId: number) => {
    const exam = exams.find((e) => e.id === examId);
    if (exam) {
      setSelectedExam(exam);
      setFormData({ ...formData, examId });
      loadSites(examId);
    }
  };

  // 选择考点
  const handleSelectSite = (siteId: number) => {
    const site = sites.find((s) => s.id === siteId);
    if (site) {
      setSelectedSite(site);
      setFormData({ ...formData, examSiteId: siteId });
    }
  };

  // 下一步
  const handleNext = async () => {
    if (current === 0) {
      // 验证是否选择了考试
      if (!formData.examId) {
        message.error('请选择考试');
        return;
      }
      setCurrent(current + 1);
    } else if (current === 1) {
      // 验证是否选择了考点
      if (!formData.examSiteId) {
        message.error('请选择考点');
        return;
      }
      setCurrent(current + 1);
    } else if (current === 2) {
      // 验证表单
      try {
        const values = await form.validateFields();
        setFormData({ ...formData, ...values });
        setCurrent(current + 1);
      } catch (error) {
        message.error('请正确填写报名信息');
      }
    }
  };

  // 上一步
  const handlePrev = () => {
    setCurrent(current - 1);
  };

  // 提交报名
  const handleSubmit = async () => {
    setSubmitting(true);
    try {
      const requestData: RegistrationRequest = {
        examId: formData.examId!,
        examSiteId: formData.examSiteId!,
        idCard: formData.idCard!,
        phone: formData.phone!,
        subject: formData.subject,
        materials: formData.materials,
      };

      const response: any = await submitRegistration(requestData);

      if (response.code === 200) {
        message.success('报名成功！请等待审核');
        // 跳转到我的报名页面
        setTimeout(() => {
          navigate('/user/registrations');
        }, 1500);
      } else {
        message.error(response.message || '报名失败');
      }
    } catch (error: any) {
      console.error('提交报名失败:', error);
      message.error(error.message || '提交报名失败');
    } finally {
      setSubmitting(false);
    }
  };

  // 步骤内容
  const steps = [
    {
      title: '选择考试',
      content: (
        <Card title="请选择要报名的考试" bordered={false}>
          <Spin spinning={loading}>
            {exams.length === 0 ? (
              <Empty description="暂无可报名的考试" />
            ) : (
              <Radio.Group
                style={{ width: '100%' }}
                value={formData.examId}
                onChange={(e) => handleSelectExam(e.target.value)}
              >
                <Space direction="vertical" style={{ width: '100%' }} size="middle">
                  {exams.map((exam) => (
                    <Card
                      key={exam.id}
                      hoverable
                      style={{
                        border: formData.examId === exam.id ? '2px solid #1890ff' : '1px solid #d9d9d9',
                      }}
                    >
                      <Radio value={exam.id}>
                        <div style={{ marginLeft: 8 }}>
                          <h3 style={{ margin: 0 }}>{exam.examName}</h3>
                          <p style={{ margin: '8px 0 0 0', color: '#666' }}>
                            考试时间：{exam.examDate} {exam.examTime || ''} | 报名费：¥{exam.fee} | 类型：
                            {exam.examType}
                          </p>
                        </div>
                      </Radio>
                    </Card>
                  ))}
                </Space>
              </Radio.Group>
            )}
          </Spin>
        </Card>
      ),
    },
    {
      title: '选择考点',
      content: (
        <Card title="请选择考试地点" bordered={false}>
          <Spin spinning={loading}>
            {!selectedExam ? (
              <Alert message="请先选择考试" type="warning" />
            ) : sites.length === 0 ? (
              <Empty description="该考试暂无考点" />
            ) : (
              <Radio.Group
                style={{ width: '100%' }}
                value={formData.examSiteId}
                onChange={(e) => handleSelectSite(e.target.value)}
              >
                <Space direction="vertical" style={{ width: '100%' }} size="middle">
                  {sites.map((site) => (
                    <Card
                      key={site.id}
                      hoverable
                      style={{
                        border: formData.examSiteId === site.id ? '2px solid #1890ff' : '1px solid #d9d9d9',
                      }}
                    >
                      <Radio value={site.id}>
                        <div style={{ marginLeft: 8 }}>
                          <h3 style={{ margin: 0 }}>{site.siteName}</h3>
                          <p style={{ margin: '8px 0 0 0', color: '#666' }}>
                            地址：{site.province} {site.city} {site.district} {site.address}
                          </p>
                          <p style={{ margin: '4px 0 0 0', color: '#666' }}>
                            容量：{site.currentCount || 0} / {site.capacity} (剩余名额：
                            {site.capacity - (site.currentCount || 0)})
                          </p>
                        </div>
                      </Radio>
                    </Card>
                  ))}
                </Space>
              </Radio.Group>
            )}
          </Spin>
        </Card>
      ),
    },
    {
      title: '填写信息',
      content: (
        <Card title="请填写报名信息" bordered={false}>
          <Form form={form} layout="vertical" initialValues={formData}>
            <Form.Item
              name="idCard"
              label="身份证号"
              rules={[
                { required: true, message: '请输入身份证号' },
                {
                  pattern: /^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[0-9Xx]$/,
                  message: '身份证号格式不正确',
                },
              ]}
            >
              <Input placeholder="请输入18位身份证号" maxLength={18} />
            </Form.Item>

            <Form.Item
              name="phone"
              label="手机号"
              rules={[
                { required: true, message: '请输入手机号' },
                { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确' },
              ]}
            >
              <Input placeholder="请输入11位手机号" maxLength={11} />
            </Form.Item>

            <Form.Item name="subject" label="报考科目">
              <Input placeholder="请输入报考科目（选填）" />
            </Form.Item>

            <Form.Item name="materials" label="证明材料">
              <TextArea
                rows={4}
                placeholder="请输入证明材料URL（选填，多个URL用逗号分隔）"
              />
            </Form.Item>

            <Alert
              message="温馨提示"
              description="请确保填写的身份证号和手机号真实有效，审核时将进行核验。"
              type="info"
              showIcon
            />
          </Form>
        </Card>
      ),
    },
    {
      title: '确认提交',
      content: (
        <Card title="确认报名信息" bordered={false}>
          <Descriptions bordered column={1} size="middle">
            <Descriptions.Item label="考试名称">{selectedExam?.examName}</Descriptions.Item>
            <Descriptions.Item label="考试时间">
              {selectedExam?.examDate} {selectedExam?.examTime || ''}
            </Descriptions.Item>
            <Descriptions.Item label="考试类型">{selectedExam?.examType}</Descriptions.Item>
            <Descriptions.Item label="报名费用">
              <span style={{ fontSize: 18, color: '#ff4d4f', fontWeight: 'bold' }}>
                ¥{selectedExam?.fee}
              </span>
            </Descriptions.Item>
            <Descriptions.Item label="考点名称">{selectedSite?.siteName}</Descriptions.Item>
            <Descriptions.Item label="考点地址">
              {selectedSite?.province} {selectedSite?.city} {selectedSite?.district}{' '}
              {selectedSite?.address}
            </Descriptions.Item>
            <Descriptions.Item label="身份证号">{formData.idCard}</Descriptions.Item>
            <Descriptions.Item label="手机号">{formData.phone}</Descriptions.Item>
            {formData.subject && (
              <Descriptions.Item label="报考科目">{formData.subject}</Descriptions.Item>
            )}
          </Descriptions>

          <Alert
            message="重要提示"
            description="提交后将进入审核流程，审核通过后方可缴费。请确保信息准确无误！"
            type="warning"
            showIcon
            style={{ marginTop: 16 }}
          />
        </Card>
      ),
    },
  ];

  return (
    <div>
      <Card>
        <Steps current={current} style={{ marginBottom: 24 }}>
          {steps.map((item) => (
            <Step key={item.title} title={item.title} />
          ))}
        </Steps>

        <div style={{ minHeight: 400 }}>{steps[current].content}</div>

        <div style={{ marginTop: 24, textAlign: 'right' }}>
          <Space>
            {current > 0 && (
              <Button onClick={handlePrev} disabled={submitting}>
                上一步
              </Button>
            )}
            {current < steps.length - 1 && (
              <Button type="primary" onClick={handleNext}>
                下一步
              </Button>
            )}
            {current === steps.length - 1 && (
              <Button type="primary" onClick={handleSubmit} loading={submitting}>
                提交报名
              </Button>
            )}
            <Button onClick={() => navigate('/user/dashboard')}>取消</Button>
          </Space>
        </div>
      </Card>
    </div>
  );
}
