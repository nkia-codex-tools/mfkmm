import apiClient from './apiClient';
import type { FunctionResource, MenuResource, MessageResourceItem } from '../types/resource';

export const functionApi = {
  getAll: () => apiClient.get<FunctionResource[]>('/resources/functions'),
  create: (data: Partial<FunctionResource>) => apiClient.post<FunctionResource>('/resources/functions', data),
  update: (id: number, data: Partial<FunctionResource>) => apiClient.put<FunctionResource>(`/resources/functions/${id}`, data),
  delete: (id: number) => apiClient.delete(`/resources/functions/${id}`),
  deleteBatch: (ids: number[]) => apiClient.delete('/resources/functions/batch', { data: ids }),
  reorder: (orderedIds: number[]) => apiClient.patch('/resources/functions/reorder', { orderedIds }),
};

export const menuApi = {
  getAll: () => apiClient.get<MenuResource[]>('/resources/menus'),
  create: (data: Partial<MenuResource>) => apiClient.post<MenuResource>('/resources/menus', data),
  update: (id: number, data: Partial<MenuResource>) => apiClient.put<MenuResource>(`/resources/menus/${id}`, data),
  delete: (id: number) => apiClient.delete(`/resources/menus/${id}`),
  deleteBatch: (ids: number[]) => apiClient.delete('/resources/menus/batch', { data: ids }),
  reorder: (orderedIds: number[]) => apiClient.patch('/resources/menus/reorder', { orderedIds }),
  searchFunctionIds: (query: string) => apiClient.get<string[]>('/resources/menus/function-ids', { params: { query } }),
};

export const messageResourceApi = {
  getAll: () => apiClient.get<MessageResourceItem[]>('/resources/message-resources'),
  create: (data: Partial<MessageResourceItem>) => apiClient.post<MessageResourceItem>('/resources/message-resources', data),
  update: (id: number, data: Partial<MessageResourceItem>) => apiClient.put<MessageResourceItem>(`/resources/message-resources/${id}`, data),
  delete: (id: number) => apiClient.delete(`/resources/message-resources/${id}`),
  deleteBatch: (ids: number[]) => apiClient.delete('/resources/message-resources/batch', { data: ids }),
  reorder: (orderedIds: number[]) => apiClient.patch('/resources/message-resources/reorder', { orderedIds }),
};
