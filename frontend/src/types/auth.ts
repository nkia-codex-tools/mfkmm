export interface UserInfo {
  id: number;
  email: string;
  name: string;
  role: 'ADMIN' | 'WRITER' | 'READER' | 'PENDING';
  status: string;
  createdAt: string;
}

export interface TokenResponse {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  user: UserInfo;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
}

export interface RoleUpdateRequest {
  role: string;
}

export interface ErrorResponse {
  status: number;
  error: string;
  message: string;
  timestamp: string;
  requestId: string;
}
