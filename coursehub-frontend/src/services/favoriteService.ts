import axiosInstance from '../api/axiosInstance';
import type { ApiResponse } from '../types/auth.types';

export const favoriteService = {
  addFavorite: async (courseId: string) => {
    const res = await axiosInstance.post<ApiResponse<void>>(`/favorites/${courseId}`);
    return res.data;
  },

  removeFavorite: async (courseId: string) => {
    const res = await axiosInstance.delete<ApiResponse<void>>(`/favorites/${courseId}`);
    return res.data;
  },

  checkFavorite: async (courseId: string) => {
    const res = await axiosInstance.get<ApiResponse<boolean>>(`/favorites/${courseId}/check`);
    return res.data;
  },

  getMyFavorites: async () => {
    const res = await axiosInstance.get<ApiResponse<any[]>>('/favorites');
    return res.data;
  },
};
