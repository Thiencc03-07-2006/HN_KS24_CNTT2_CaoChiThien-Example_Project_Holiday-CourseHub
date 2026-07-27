import axiosInstance from '../api/axiosInstance';
import type { ApiResponse } from '../types/auth.types';

export interface ReportResponse {
  id: string;
  reporterId: string;
  reporterName: string;
  reportableType: string;
  reportableId: string;
  reason: string;
  description: string;
  status: string;
  createdAt: string;
  updatedAt: string;
  adminNote?: string;
  targetTitle?: string;
}

export const reportService = {
  // Course Report
  reportCourse: async (courseId: string, reason: string, description: string) => {
    const res = await axiosInstance.post<ApiResponse<ReportResponse>>(`/courses/${courseId}/report`, {
      reason,
      description,
    });
    return res.data;
  },

  getMyCourseReports: async () => {
    const res = await axiosInstance.get<ApiResponse<ReportResponse[]>>('/reports/courses/my');
    return res.data;
  },

  getAdminCourseReports: async () => {
    const res = await axiosInstance.get<ApiResponse<ReportResponse[]>>('/admin/reports/courses');
    return res.data;
  },

  getAdminCourseReport: async (id: string) => {
    const res = await axiosInstance.get<ApiResponse<ReportResponse>>(`/admin/reports/courses/${id}`);
    return res.data;
  },

  updateCourseReportStatus: async (id: string, status: string, adminNote: string) => {
    const res = await axiosInstance.put<ApiResponse<ReportResponse>>(`/admin/reports/courses/${id}/status`, {
      status,
      adminNote,
    });
    return res.data;
  },

  deleteCourseReport: async (id: string) => {
    const res = await axiosInstance.delete<ApiResponse<void>>(`/admin/reports/courses/${id}`);
    return res.data;
  },

  // Comment & Review Report
  reportReview: async (reviewId: string, reason: string, description: string) => {
    const res = await axiosInstance.post<ApiResponse<ReportResponse>>(`/reviews/${reviewId}/report`, {
      reason,
      description,
    });
    return res.data;
  },

  reportComment: async (commentId: string, reason: string, description: string) => {
    const res = await axiosInstance.post<ApiResponse<ReportResponse>>(`/comments/${commentId}/report`, {
      reason,
      description,
    });
    return res.data;
  },

  getAdminCommentAndReviewReports: async () => {
    const res = await axiosInstance.get<ApiResponse<ReportResponse[]>>('/admin/reports/comments');
    return res.data;
  },

  getAdminCommentOrReviewReport: async (id: string) => {
    const res = await axiosInstance.get<ApiResponse<ReportResponse>>(`/admin/reports/comments/${id}`);
    return res.data;
  },

  updateCommentOrReviewReportStatus: async (id: string, status: string, adminNote: string) => {
    const res = await axiosInstance.put<ApiResponse<ReportResponse>>(`/admin/reports/comments/${id}/status`, {
      status,
      adminNote,
    });
    return res.data;
  },

  deleteCommentOrReviewReport: async (id: string) => {
    const res = await axiosInstance.delete<ApiResponse<void>>(`/admin/reports/comments/${id}`);
    return res.data;
  },
};
