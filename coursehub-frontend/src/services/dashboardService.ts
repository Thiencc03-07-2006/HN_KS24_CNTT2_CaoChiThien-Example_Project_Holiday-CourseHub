import axiosInstance from '../api/axiosInstance';
import type { ApiResponse } from '../types/auth.types';

export const dashboardService = {
  getAdminStatistics: async () => {
    const res = await axiosInstance.get<ApiResponse<any>>('/admin/statistics');
    return res.data;
  },

  getInstructorStatistics: async () => {
    const res = await axiosInstance.get<ApiResponse<any>>('/instructor/dashboard');
    return res.data;
  },

  getAdminDashboardOverview: async () => {
    const res = await axiosInstance.get<ApiResponse<any>>('/admin/dashboard/stats');
    return res.data;
  },
};
