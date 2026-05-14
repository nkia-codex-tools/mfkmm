import apiClient from './apiClient';

export interface VersionTag {
  id: number;
  tagName: string;
  description: string;
  resourceType: string;
  createdBy: number;
  createdAt: string;
}

export interface VersionDiff {
  versionId: number;
  tagName: string;
  resourceType: string;
  snapshotRowCount: number;
  currentRowCount: number;
  snapshotPreview: string;
}

export const versionApi = {
  getVersions: (resourceType?: string) =>
    apiClient.get<VersionTag[]>('/versions', { params: resourceType ? { resourceType } : {} }),

  createTag: (data: { tagName: string; description?: string; resourceType: string }) =>
    apiClient.post<VersionTag>('/versions', data),

  getDiff: (id: number) =>
    apiClient.get<VersionDiff>(`/versions/${id}/diff`),

  rollback: (id: number) =>
    apiClient.post(`/versions/${id}/rollback`),
};
