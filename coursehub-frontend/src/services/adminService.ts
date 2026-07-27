import axiosInstance from '../api/axiosInstance';
import type { ApiResponse } from '../types/auth.types';

export const adminService = {
  // Report Management
  getReports: async () => {
    const res = await axiosInstance.get<ApiResponse<any[]>>('/admin/reports');
    return res.data;
  },

  updateReportStatus: async (reportId: string, status: string) => {
    const res = await axiosInstance.patch<ApiResponse<void>>(`/admin/reports/${reportId}/status`, null, {
      params: { status },
    });
    return res.data;
  },

  // User moderation
  lockUser: async (userId: string) => {
    const res = await axiosInstance.put<ApiResponse<any>>(`/admin/users/${userId}/lock`);
    return res.data;
  },

  unlockUser: async (userId: string) => {
    const res = await axiosInstance.put<ApiResponse<any>>(`/admin/users/${userId}/unlock`);
    return res.data;
  },

  disableUser: async (userId: string) => {
    const res = await axiosInstance.put<ApiResponse<any>>(`/admin/users/${userId}/disable`);
    return res.data;
  },

  searchUsers: async (params: {
    keyword?: string;
    status?: string;
    role?: string;
    page?: number;
    size?: number;
  }) => {
    const res = await axiosInstance.get<ApiResponse<any>>('/admin/users', { params });
    return res.data;
  },

  getUserDetail: async (userId: string) => {
    const res = await axiosInstance.get<ApiResponse<any>>(`/admin/users/${userId}`);
    return res.data;
  },

  getCoursesForReview: async (params?: { keyword?: string; status?: string; page?: number; size?: number }) => {
    const res = await axiosInstance.get<ApiResponse<any>>('/admin/courses', { params });
    return res.data;
  },
};
