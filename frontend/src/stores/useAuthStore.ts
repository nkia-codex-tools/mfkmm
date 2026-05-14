import { create } from 'zustand';
import { authApi } from '../api/authApi';
import type { UserInfo } from '../types/auth';

interface AuthState {
  accessToken: string | null;
  refreshToken: string | null;
  user: UserInfo | null;
  isAuthenticated: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (name: string, email: string, password: string) => Promise<void>;
  logout: () => void;
  isAdmin: () => boolean;
  hasWritePermission: () => boolean;
  hasReadPermission: () => boolean;
  loadFromStorage: () => void;
}

export const useAuthStore = create<AuthState>((set, get) => ({
  accessToken: null,
  refreshToken: null,
  user: null,
  isAuthenticated: false,

  login: async (email: string, password: string) => {
    const response = await authApi.login({ email, password });
    const { accessToken, refreshToken, user } = response.data;

    localStorage.setItem('accessToken', accessToken);
    localStorage.setItem('refreshToken', refreshToken);
    localStorage.setItem('user', JSON.stringify(user));

    set({ accessToken, refreshToken, user, isAuthenticated: true });
  },

  register: async (name: string, email: string, password: string) => {
    await authApi.register({ name, email, password });
  },

  logout: () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
    set({ accessToken: null, refreshToken: null, user: null, isAuthenticated: false });
  },

  isAdmin: () => get().user?.role === 'ADMIN',

  hasWritePermission: () => {
    const role = get().user?.role;
    return role === 'ADMIN' || role === 'WRITER';
  },

  hasReadPermission: () => {
    const role = get().user?.role;
    return role === 'ADMIN' || role === 'WRITER' || role === 'READER';
  },

  loadFromStorage: () => {
    const accessToken = localStorage.getItem('accessToken');
    const refreshToken = localStorage.getItem('refreshToken');
    const userStr = localStorage.getItem('user');

    if (accessToken && userStr) {
      const user = JSON.parse(userStr) as UserInfo;
      set({ accessToken, refreshToken, user, isAuthenticated: true });
    }
  },
}));
