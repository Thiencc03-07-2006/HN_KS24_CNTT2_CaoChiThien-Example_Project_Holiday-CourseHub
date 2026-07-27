import axiosInstance from '../api/axiosInstance';
import type { ApiResponse } from '../types/auth.types';

export interface CommentResponse {
  id: string;
  userId: string;
  userName: string;
  userAvatar?: string;
  content: string;
  createdAt: string;
  replies?: CommentResponse[];
}

export const commentService = {
  addComment: async (lessonId: string, content: string, parentId?: string) => {
    const res = await axiosInstance.post<ApiResponse<CommentResponse>>(`/lessons/${lessonId}/comments`, {
      content,
      parentId,
    });
    return res.data;
  },

  getComments: async (lessonId: string) => {
    const res = await axiosInstance.get<ApiResponse<CommentResponse[]>>(`/lessons/${lessonId}/comments`);
    return res.data;
  },

  updateComment: async (commentId: string, content: string) => {
    const res = await axiosInstance.put<ApiResponse<CommentResponse>>(`/comments/${commentId}`, { content });
    return res.data;
  },

  deleteComment: async (commentId: string) => {
    const res = await axiosInstance.delete<ApiResponse<void>>(`/comments/${commentId}`);
    return res.data;
  },
};
