import apiClient from './apiClient';

export interface ImportPreviewResponse {
  totalRows: number;
  validRows: number;
  invalidRows: number;
  errors: string[];
  previewData: Record<string, string>[];
}

export interface ImportResultResponse {
  created: number;
  updated: number;
  skipped: number;
  failed: number;
}

export const transferApi = {
  export: (resourceType: string, selectedIds?: number[]) =>
    apiClient.post(`/transfer/${resourceType}/export`, { selectedIds }, { responseType: 'blob' }),

  importPreview: (resourceType: string, file: File) => {
    const formData = new FormData();
    formData.append('file', file);
    return apiClient.post<ImportPreviewResponse>(`/transfer/${resourceType}/import/preview`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },

  importApply: (resourceType: string, file: File, conflictStrategy: string) => {
    const formData = new FormData();
    formData.append('file', file);
    return apiClient.post<ImportResultResponse>(
      `/transfer/${resourceType}/import/apply?conflictStrategy=${conflictStrategy}`,
      formData,
      { headers: { 'Content-Type': 'multipart/form-data' } }
    );
  },
};
