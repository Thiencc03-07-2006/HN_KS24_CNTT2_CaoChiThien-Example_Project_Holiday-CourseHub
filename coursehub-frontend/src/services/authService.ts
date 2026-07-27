import axiosInstance from '../api/axiosInstance';
import type { ApiResponse, AuthResponse, LoginRequest, RegisterRequest } from '../types/auth.types';

export const authService = {
  login: async (data: LoginRequest) => {
    const res = await axiosInstance.post<ApiResponse<AuthResponse>>('/auth/login', data);
    return res.data;
  },
  register: async (data: RegisterRequest) => {
    const res = await axiosInstance.post<ApiResponse<void>>('/auth/register', data);
    return res.data;
  },
  verifyOtp: async (email: string, otpCode: string) => {
    const res = await axiosInstance.post<ApiResponse<void>>('/auth/verify-otp', { email, otpCode });
    return res.data;
  },
  logout: async () => {
    const res = await axiosInstance.post<ApiResponse<void>>('/auth/logout');
    return res.data;
  },
  forgotPassword: async (email: string) => {
    const res = await axiosInstance.post<ApiResponse<void>>('/auth/forgot-password', { email });
    return res.data;
  },
  resetPassword: async (token: string, newPassword: string, confirmPassword: string) => {
    const res = await axiosInstance.post<ApiResponse<void>>('/auth/reset-password', {
      token,
      newPassword,
      confirmPassword,
    });
    return res.data;
  },
  oauth2MockLogin: async (email: string, name: string) => {
    const res = await axiosInstance.post<ApiResponse<AuthResponse>>('/auth/oauth2/mock', {
      provider: 'GOOGLE',
      email,
      name,
    });
    return res.data;
  },
};
