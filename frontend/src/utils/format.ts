import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';

// 扩展dayjs插件
dayjs.extend(relativeTime);

// 日期格式化
export const formatDate = (date: string | Date, format = 'YYYY-MM-DD'): string => {
  if (!date) return '';
  return dayjs(date).format(format);
};

// 日期时间格式化
export const formatDateTime = (date: string | Date, format = 'YYYY-MM-DD HH:mm:ss'): string => {
  if (!date) return '';
  return dayjs(date).format(format);
};

// 相对时间格式化
export const formatRelativeTime = (date: string | Date): string => {
  if (!date) return '';
  return dayjs(date).fromNow();
};

// 金额格式化
export const formatMoney = (amount: number): string => {
  if (amount === null || amount === undefined) return '0.00';
  return `¥${amount.toFixed(2)}`;
};

// 手机号格式化（脱敏）
export const formatPhone = (phone: string): string => {
  if (!phone) return '';
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2');
};

// 身份证号格式化（脱敏）
export const formatIdCard = (idCard: string): string => {
  if (!idCard) return '';
  return idCard.replace(/(\d{6})\d{8}(\d{4})/, '$1********$2');
};

// 姓名格式化（脱敏）
export const formatName = (name: string): string => {
  if (!name) return '';
  if (name.length === 1) return name;
  if (name.length === 2) return name[0] + '*';
  return name[0] + '*'.repeat(name.length - 2) + name[name.length - 1];
};

// 文件大小格式化
export const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 B';
  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
};

// 状态文本格式化
export const formatStatus = (status: number, type: 'user' | 'exam' | 'audit' | 'payment' | 'order' | 'notice'): string => {
  const statusMap: Record<string, Record<number, string>> = {
    user: {
      1: '正常',
      2: '禁用',
    },
    exam: {
      1: '草稿',
      2: '已发布',
      3: '报名中',
      4: '报名结束',
      5: '已结束',
    },
    audit: {
      1: '待审核',
      2: '审核通过',
      3: '审核驳回',
    },
    payment: {
      1: '未缴费',
      2: '已缴费',
    },
    order: {
      1: '待支付',
      2: '已支付',
      3: '已关闭',
      4: '已退款',
    },
    notice: {
      1: '已发布',
      2: '已下架',
    },
  };
  
  return statusMap[type]?.[status] || '未知';
};

// 考试类型格式化
export const formatExamType = (type: string): string => {
  const typeMap: Record<string, string> = {
    vocational: '职业资格考试',
    academic: '学业水平考试',
    skill: '技能等级考试',
    professional: '职称考试',
    other: '其他',
  };
  return typeMap[type] || type;
};

// 支付方式格式化
export const formatPaymentMethod = (method: string): string => {
  const methodMap: Record<string, string> = {
    alipay: '支付宝',
    wechat: '微信支付',
    union: '银联支付',
  };
  return methodMap[method] || method;
};
