// 用户相关类型定义
export interface User {
  id: number;
  username: string;
  realName: string;
  idCard: string;
  phone: string;
  email?: string;
  gender?: number;
  birthday?: string;
  avatar?: string;
  education?: string;
  workUnit?: string;
  address?: string;
  role: 'user' | 'admin';
  status: number;
  createTime: string;
  updateTime: string;
}

// 考试相关类型定义
export interface Exam {
  id: number;
  examName: string;
  examType: string;
  examDate: string;
  examTime: string;
  registrationStart: string;
  registrationEnd: string;
  fee: number;
  description?: string;
  notice?: string;
  fileUrl?: string;
  status: number;
  totalQuota?: number;
  currentCount: number;
  createBy: number;
  createTime: string;
  updateTime: string;
}

// 考点相关类型定义
export interface ExamSite {
  id: number;
  examId: number;
  siteName: string;
  province: string;
  city: string;
  district?: string;
  address: string;
  contactPerson?: string;
  contactPhone?: string;
  capacity: number;
  currentCount: number;
  longitude?: number;
  latitude?: number;
  status: number;
  remark?: string;
  createTime: string;
}

// 报名相关类型定义
export interface Registration {
  id: number;
  examId: number;
  userId: number;
  examSiteId?: number;
  admissionTicketNo?: string;
  examRoom?: string;
  seatNo?: string;
  subject?: string;
  materials?: string;
  auditStatus: number;
  auditRemark?: string;
  auditBy?: number;
  auditTime?: string;
  paymentStatus: number;
  paymentTime?: string;
  ticketDownloadCount: number;
  ticketDownloadTime?: string;
  createTime: string;
  updateTime: string;
  // 关联数据
  exam?: Exam;
  user?: User;
  examSite?: ExamSite;
}

// 缴费订单相关类型定义
export interface PaymentOrder {
  id: number;
  orderNo: string;
  registrationId: number;
  userId: number;
  examId: number;
  amount: number;
  paymentMethod?: string;
  transactionId?: string;
  status: number;
  payTime?: string;
  expireTime: string;
  callbackData?: string;
  refundReason?: string;
  refundTime?: string;
  createTime: string;
  updateTime: string;
  // 关联数据
  registration?: Registration;
  exam?: Exam;
}

// 公告相关类型定义
export interface Notice {
  id: number;
  title: string;
  content: string;
  type: string;
  isTop: number;
  status: number;
  viewCount: number;
  publishTime?: string;
  createBy: number;
  createTime: string;
  updateTime: string;
}

// API响应类型定义
export interface ApiResponse<T = any> {
  code: number;
  message: string;
  data: T;
  timestamp: number;
}

// 分页响应类型定义
export interface PageResponse<T = any> {
  list: T[];
  total: number;
  pageNum: number;
  pageSize: number;
  pages: number;
}

// 登录请求类型定义
export interface LoginRequest {
  username: string;
  password: string;
}

// 注册请求类型定义
export interface RegisterRequest {
  username: string;
  password: string;
  smsCode: string;
}

// 报名请求类型定义
export interface RegistrationRequest {
  examId: number;
  examSiteId: number;
  subject?: string;
  materials?: string[];
}

// 审核请求类型定义
export interface AuditRequest {
  registrationId: number;
  auditStatus: number;
  auditRemark?: string;
}

// 统计数据类型定义
export interface StatisticsData {
  registrationCount: number;
  paymentCount: number;
  auditCount: number;
  examCount: number;
}

// 状态枚举
export const StatusEnum = {
  USER: {
    NORMAL: 1,
    DISABLED: 2,
  },
  EXAM: {
    DRAFT: 1,
    PUBLISHED: 2,
    REGISTERING: 3,
    REGISTRATION_END: 4,
    ENDED: 5,
  },
  AUDIT: {
    PENDING: 1,
    APPROVED: 2,
    REJECTED: 3,
  },
  PAYMENT: {
    UNPAID: 1,
    PAID: 2,
  },
  ORDER: {
    PENDING: 1,
    PAID: 2,
    CLOSED: 3,
    REFUNDED: 4,
  },
  NOTICE: {
    PUBLISHED: 1,
    UNPUBLISHED: 2,
  },
} as const;
