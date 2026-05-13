import { createContext, useContext, useReducer, useCallback, useEffect, ReactNode } from 'react';
import { useNavigate } from 'react-router-dom';
import client, { setTokens, setOnAuthFailure } from '@/shared/api/client';
import { AUTH } from '@/shared/api/endpoints';
import { AuthState, UserInfo } from '@/types';

type AuthAction =
  | { type: 'LOGIN_SUCCESS'; payload: { user: UserInfo; accessToken: string; refreshToken: string } }
  | { type: 'LOGOUT' }
  | { type: 'TOKEN_REFRESHED'; payload: { accessToken: string } };

const initialState: AuthState = {
  user: null,
  accessToken: null,
  refreshToken: null,
  isAuthenticated: false,
};

function authReducer(state: AuthState, action: AuthAction): AuthState {
  switch (action.type) {
    case 'LOGIN_SUCCESS':
      return {
        user: action.payload.user,
        accessToken: action.payload.accessToken,
        refreshToken: action.payload.refreshToken,
        isAuthenticated: true,
      };
    case 'LOGOUT':
      return initialState;
    case 'TOKEN_REFRESHED':
      return { ...state, accessToken: action.payload.accessToken };
    default:
      return state;
  }
}

interface AuthContextValue {
  state: AuthState;
  login: (userId: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [state, dispatch] = useReducer(authReducer, initialState);
  const navigate = useNavigate();

  useEffect(() => {
    setOnAuthFailure(() => {
      dispatch({ type: 'LOGOUT' });
      setTokens(null, null);
      navigate('/login');
    });
  }, [navigate]);

  useEffect(() => {
    setTokens(state.accessToken, state.refreshToken);
  }, [state.accessToken, state.refreshToken]);

  const login = useCallback(async (userId: string, password: string) => {
    const response = await client.post(AUTH.LOGIN, { userId, password });
    const { accessToken, refreshToken, user } = response.data;
    dispatch({ type: 'LOGIN_SUCCESS', payload: { user, accessToken, refreshToken } });
  }, []);

  const logout = useCallback(async () => {
    try {
      if (state.refreshToken) {
        await client.post(AUTH.LOGOUT, { refreshToken: state.refreshToken });
      }
    } finally {
      dispatch({ type: 'LOGOUT' });
      setTokens(null, null);
      navigate('/login');
    }
  }, [state.refreshToken, navigate]);

  return (
    <AuthContext.Provider value={{ state, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
}
