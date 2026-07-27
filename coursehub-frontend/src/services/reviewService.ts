import axiosInstance from '../api/axiosInstance';
import type { ApiResponse } from '../types/auth.types';

export interface Review {
  id: string;
  studentId: string;
  studentName: string;
  studentAvatar?: string;
  rating: number;
  comment: string;
  isEdited: boolean;
  courseId: string;
  courseTitle: string;
  createdAt: string;
  updatedAt: string;
}

export interface RatingSummary {
  averageRating: number;
  totalReviews: number;
  distribution: Record<string, number>;
}

export interface InstructorReviewStats {
  averageRating: number;
  totalReviews: number;
  distribution: Record<string, number>;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
  first: boolean;
}

export const reviewService = {
  createOrUpdateReview: async (courseId: string, rating: number, comment: string) => {
    const res = await axiosInstance.post<ApiResponse<Review>>(`/courses/${courseId}/reviews`, { rating, comment });
    return res.data;
  },

  updateReview: async (reviewId: string, rating: number, comment: string) => {
    const res = await axiosInstance.put<ApiResponse<Review>>(`/reviews/${reviewId}`, { rating, comment });
    return res.data;
  },

  deleteReview: async (reviewId: string) => {
    const res = await axiosInstance.delete<ApiResponse<void>>(`/reviews/${reviewId}`);
    return res.data;
  },

  getCourseReviews: async (courseId: string, page = 0, size = 10, sort = 'newest') => {
    const res = await axiosInstance.get<ApiResponse<PageResponse<Review>>>(
      `/courses/${courseId}/reviews?page=${page}&size=${size}&sort=${sort}`
    );
    return res.data;
  },

  getRatingSummary: async (courseId: string) => {
    const res = await axiosInstance.get<ApiResponse<RatingSummary>>(`/courses/${courseId}/rating-summary`);
    return res.data;
  },

  getReviewsForAdmin: async (params: { keyword?: string; courseId?: string; userId?: string; rating?: number; page?: number; size?: number }) => {
    const query = new URLSearchParams();
    if (params.keyword) query.append('keyword', params.keyword);
    if (params.courseId) query.append('courseId', params.courseId);
    if (params.userId) query.append('userId', params.userId);
    if (params.rating) query.append('rating', String(params.rating));
    if (params.page !== undefined) query.append('page', String(params.page));
    if (params.size !== undefined) query.append('size', String(params.size));
    
    const res = await axiosInstance.get<ApiResponse<PageResponse<Review>>>(`/admin/reviews?${query.toString()}`);
    return res.data;
  },

  getInstructorReviews: async (page = 0, size = 10) => {
    const res = await axiosInstance.get<ApiResponse<PageResponse<Review>>>(`/instructor/reviews?page=${page}&size=${size}`);
    return res.data;
  },

  getInstructorStats: async () => {
    const res = await axiosInstance.get<ApiResponse<InstructorReviewStats>>('/instructor/reviews/stats');
    return res.data;
  },
};
