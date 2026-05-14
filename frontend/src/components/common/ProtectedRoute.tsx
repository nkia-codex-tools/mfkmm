import { Navigate } from 'react-router-dom';
import { useAuthStore } from '../../stores/useAuthStore';

interface Props {
  children: React.ReactNode;
  requiredRole?: 'ADMIN' | 'WRITER' | 'READER';
}

const ROLE_HIERARCHY = { ADMIN: 3, WRITER: 2, READER: 1, PENDING: 0 };

export default function ProtectedRoute({ children, requiredRole }: Props) {
  const { isAuthenticated, user } = useAuthStore();

  if (!isAuthenticated || !user) {
    return <Navigate to="/login" replace />;
  }

  if (user.role === 'PENDING') {
    return (
      <div data-testid="pending-message" style={{ padding: 48, textAlign: 'center' }}>
        <h2>승인 대기 중</h2>
        <p>관리자가 권한을 부여하면 서비스를 이용할 수 있습니다.</p>
      </div>
    );
  }

  if (requiredRole && ROLE_HIERARCHY[user.role] < ROLE_HIERARCHY[requiredRole]) {
    return (
      <div data-testid="access-denied" style={{ padding: 48, textAlign: 'center' }}>
        <h2>접근 권한 없음</h2>
        <p>이 페이지에 접근할 권한이 없습니다.</p>
      </div>
    );
  }

  return <>{children}</>;
}
