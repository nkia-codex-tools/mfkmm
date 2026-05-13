import { Navigate } from 'react-router-dom';
import { useAuth } from './AuthContext';
import { Role } from '@/types';

const ROLE_LEVEL: Record<Role, number> = {
  READ: 1,
  WRITE: 2,
  ADMIN: 3,
  ROOT_ADMIN: 4,
};

interface Props {
  children: React.ReactNode;
  requiredRole?: Role;
}

export default function ProtectedRoute({ children, requiredRole = 'READ' }: Props) {
  const { state } = useAuth();

  if (!state.isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (state.user && ROLE_LEVEL[state.user.role] < ROLE_LEVEL[requiredRole]) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <h1 className="text-2xl font-bold text-red-600">접근 권한 없음</h1>
          <p className="mt-2 text-gray-600">이 페이지에 접근할 권한이 없습니다.</p>
        </div>
      </div>
    );
  }

  return <>{children}</>;
}
