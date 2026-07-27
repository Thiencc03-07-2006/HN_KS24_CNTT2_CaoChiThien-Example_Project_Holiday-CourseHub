import axiosInstance from '../api/axiosInstance';
import type { ApiResponse } from '../types/auth.types';

export const wishlistService = {
  addToWishlist: async (courseId: string) => {
    const res = await axiosInstance.post<ApiResponse<void>>(`/wishlist/${courseId}`);
    return res.data;
  },

  removeFromWishlist: async (courseId: string) => {
    const res = await axiosInstance.delete<ApiResponse<void>>(`/wishlist/${courseId}`);
    return res.data;
  },

  checkWishlist: async (courseId: string) => {
    const res = await axiosInstance.get<ApiResponse<boolean>>(`/wishlist/${courseId}/check`);
    return res.data;
  },

  getMyWishlist: async () => {
    const res = await axiosInstance.get<ApiResponse<any[]>>('/wishlist');
    return res.data;
  },
};
