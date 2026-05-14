import apiClient from './apiClient';

export interface HistoryItem {
  id: number;
  resourceType: string;
  resourceId: number;
  changeType: string;
  fieldName: string | null;
  oldValue: string | null;
  newValue: string | null;
  changedBy: number;
  changedAt: string;
}

export interface HistoryPage {
  content: HistoryItem[];
  totalPages: number;
  totalElements: number;
  number: number;
}

export const historyApi = {
  getHistory: (params: { resourceType?: string; changedBy?: number; from?: string; to?: string; page?: number; size?: number }) =>
    apiClient.get<HistoryPage>('/history', { params }),
};
