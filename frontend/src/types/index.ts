export type Role = 'READ' | 'WRITE' | 'ADMIN' | 'ROOT_ADMIN';

export interface UserInfo {
  id: string;
  userId: string;
  role: Role;
}

export interface AuthState {
  user: UserInfo | null;
  accessToken: string | null;
  refreshToken: string | null;
  isAuthenticated: boolean;
}

export type ResourceType = 'MESSAGE_KEY' | 'FUNCTION_ID' | 'MENU_ID';
export type FileFormat = 'EXCEL' | 'TSV' | 'JSON';
export type ConflictPolicy = 'SKIP' | 'OVERWRITE';

export interface Resource {
  id: string;
  resourceType: ResourceType;
  resourceKey: string;
  content: string;
  description: string;
  createdBy: string;
  createdAt: string;
  updatedBy: string;
  updatedAt: string;
}

export interface ImportResult {
  jobId: string;
  totalRows: number;
  successCount: number;
  failedCount: number;
  skippedCount: number;
  errors: ImportError[];
}

export interface ImportError {
  rowNumber: number;
  field: string;
  value: string;
  reason: string;
}

export interface Deployment {
  id: string;
  version: string;
  format: string;
  totalRecords: number;
  fileSize: number;
  userId: string;
  createdAt: string;
}

export interface SimilarityResult {
  resource: Resource;
  distance: number;
  matchType: string;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
