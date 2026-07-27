import axiosInstance from '../api/axiosInstance';
import type { ApiResponse } from '../types/auth.types';

export const courseService = {
  searchCourses: async (params: {
    keyword?: string;
    categoryId?: number | string;
    level?: string;
    minPrice?: number | string;
    maxPrice?: number | string;
    rating?: number | string;
    language?: string;
    sortBy?: string;
    page?: number;
    size?: number;
  }) => {
    const res = await axiosInstance.get<ApiResponse<any>>('/courses/search', { params });
    return res.data;
  },

  getCourseBySlug: async (slug: string) => {
    const res = await axiosInstance.get<ApiResponse<any>>(`/courses/public/${slug}`);
    return res.data;
  },

  createCourse: async (data: any) => {
    const res = await axiosInstance.post<ApiResponse<any>>('/instructor/courses', data);
    return res.data;
  },

  getInstructorCourses: async (page = 0, size = 12) => {
    const res = await axiosInstance.get<ApiResponse<any>>('/instructor/courses', {
      params: { page, size },
    });
    return res.data;
  },

  getCourseById: async (courseId: string) => {
    const res = await axiosInstance.get<ApiResponse<any>>(`/instructor/courses/${courseId}`);
    return res.data;
  },

  updateCourse: async (courseId: string, data: any) => {
    const res = await axiosInstance.put<ApiResponse<any>>(`/instructor/courses/${courseId}`, data);
    return res.data;
  },

  uploadThumbnail: async (courseId: string, file: File) => {
    const formData = new FormData();
    formData.append('file', file);
    const res = await axiosInstance.post<ApiResponse<string>>(
      `/instructor/courses/${courseId}/thumbnail`,
      formData,
      { headers: { 'Content-Type': 'multipart-form-data' } }
    );
    return res.data;
  },

  submitForReview: async (courseId: string) => {
    const res = await axiosInstance.post<ApiResponse<void>>(`/instructor/courses/${courseId}/submit`);
    return res.data;
  },

  resubmitForReview: async (courseId: string) => {
    const res = await axiosInstance.put<ApiResponse<void>>(`/instructor/courses/${courseId}/resubmit`);
    return res.data;
  },

  deleteCourse: async (courseId: string) => {
    const res = await axiosInstance.delete<ApiResponse<void>>(`/instructor/courses/${courseId}`);
    return res.data;
  },

  // Admin moderation endpoints
  approveCourse: async (courseId: string, note?: string) => {
    const res = await axiosInstance.put<ApiResponse<void>>(`/admin/courses/${courseId}/approve`, null, {
      params: { note },
    });
    return res.data;
  },

  rejectCourse: async (courseId: string, note?: string) => {
    const res = await axiosInstance.put<ApiResponse<void>>(`/admin/courses/${courseId}/reject`, null, {
      params: { note },
    });
    return res.data;
  },
};
