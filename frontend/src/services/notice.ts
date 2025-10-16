import api from './api';
import type { Notice, ApiResponse, PageResponse } from '../types';

// 获取公告列表
export const getNoticeList = (params?: {
  pageNum?: number;
  pageSize?: number;
  type?: string;
  status?: number;
}): Promise<ApiResponse<PageResponse<Notice>>> => {
  return api.get('/notices', { params });
};

// 获取公告详情
export const getNoticeDetail = (id: number): Promise<ApiResponse<Notice>> => {
  return api.get(`/notices/${id}`);
};

// 创建公告（管理员）
export const createNotice = (data: Partial<Notice>): Promise<ApiResponse<Notice>> => {
  return api.post('/notices', data);
};

// 更新公告（管理员）
export const updateNotice = (id: number, data: Partial<Notice>): Promise<ApiResponse<Notice>> => {
  return api.put(`/notices/${id}`, data);
};

// 删除公告（管理员）
export const deleteNotice = (id: number): Promise<ApiResponse> => {
  return api.delete(`/notices/${id}`);
};

// 发布/下架公告（管理员）
export const updateNoticeStatus = (id: number, status: number): Promise<ApiResponse> => {
  return api.put(`/notices/${id}/status`, { status });
};

// 置顶/取消置顶公告（管理员）
export const updateNoticeTop = (id: number, isTop: number): Promise<ApiResponse> => {
  return api.put(`/notices/${id}/top`, { isTop });
};
