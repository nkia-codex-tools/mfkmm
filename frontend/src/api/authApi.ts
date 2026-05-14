import apiClient from './apiClient';
import type { LoginRequest, RegisterRequest, TokenResponse, UserInfo, RoleUpdateRequest } from '../types/auth';

export const authApi = {
  login: (data: LoginRequest) =>
    apiClient.post<TokenResponse>('/auth/login', data),

  register: (data: RegisterRequest) =>
    apiClient.post<UserInfo>('/auth/register', data),

  refresh: (refreshToken: string) =>
    apiClient.post<TokenResponse>('/auth/refresh', { refreshToken }),
};

export const adminApi = {
  getUsers: (page = 0, size = 20) =>
    apiClient.get<{ content: UserInfo[]; totalPages: number }>('/admin/users', { params: { page, size } }),

  getPendingUsers: () =>
    apiClient.get<UserInfo[]>('/admin/users/pending'),

  updateUserRole: (userId: number, data: RoleUpdateRequest) =>
    apiClient.patch<UserInfo>(`/admin/users/${userId}/role`, data),
};
