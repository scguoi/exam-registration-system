import api from './api';
import type { Exam, ExamSite, ApiResponse, PageResponse } from '../types';

// 获取考试列表
export const getExamList = (params?: {
  pageNum?: number;
  pageSize?: number;
  examType?: string;
  status?: number;
  keyword?: string;
}): Promise<ApiResponse<PageResponse<Exam>>> => {
  return api.get('/exams', { params });
};

// 获取考试详情
export const getExamDetail = (id: number): Promise<ApiResponse<Exam>> => {
  return api.get(`/exams/${id}`);
};

// 创建考试（管理员）
export const createExam = (data: Partial<Exam>): Promise<ApiResponse<Exam>> => {
  return api.post('/exams', data);
};

// 更新考试（管理员）
export const updateExam = (id: number, data: Partial<Exam>): Promise<ApiResponse<Exam>> => {
  return api.put(`/exams/${id}`, data);
};

// 删除考试（管理员）
export const deleteExam = (id: number): Promise<ApiResponse> => {
  return api.delete(`/exams/${id}`);
};

// 发布/下架考试（管理员）
export const updateExamStatus = (id: number, status: number): Promise<ApiResponse> => {
  return api.put(`/exams/${id}/status`, { status });
};

// 获取考点列表
export const getExamSiteList = (examId: number): Promise<ApiResponse<ExamSite[]>> => {
  return api.get(`/exams/${examId}/sites`);
};

// 创建考点（管理员）
export const createExamSite = (data: Partial<ExamSite>): Promise<ApiResponse<ExamSite>> => {
  return api.post('/exam-sites', data);
};

// 更新考点（管理员）
export const updateExamSite = (id: number, data: Partial<ExamSite>): Promise<ApiResponse<ExamSite>> => {
  return api.put(`/exam-sites/${id}`, data);
};

// 删除考点（管理员）
export const deleteExamSite = (id: number): Promise<ApiResponse> => {
  return api.delete(`/exam-sites/${id}`);
};
