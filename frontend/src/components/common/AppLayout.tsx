import { Link, Outlet, useNavigate } from 'react-router-dom';
import { useAuthStore } from '../../stores/useAuthStore';

export default function AppLayout() {
  const { user, logout, isAdmin } = useAuthStore();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div data-testid="app-layout" style={{ display: 'flex', minHeight: '100vh' }}>
      <aside style={{ width: 220, background: '#f5f5f5', padding: 16 }}>
        <h3 style={{ marginBottom: 24 }}>리소스 관리</h3>
        <nav>
          <ul style={{ listStyle: 'none', padding: 0 }}>
            <li style={{ marginBottom: 8 }}>
              <Link to="/resources">리소스 관리</Link>
            </li>
            <li style={{ marginBottom: 8 }}>
              <Link to="/history">변경 이력</Link>
            </li>
            {isAdmin() && (
              <>
                <li style={{ marginBottom: 8 }}>
                  <Link to="/versions">버전 관리</Link>
                </li>
                <li style={{ marginBottom: 8 }}>
                  <Link to="/admin">사용자 관리</Link>
                </li>
              </>
            )}
          </ul>
        </nav>
      </aside>
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
        <header style={{ display: 'flex', justifyContent: 'flex-end', alignItems: 'center', padding: '12px 24px', borderBottom: '1px solid #e0e0e0' }}>
          <span style={{ marginRight: 16 }}>
            {user?.name} <span style={{ fontSize: 12, color: '#666' }}>({user?.role})</span>
          </span>
          <button onClick={handleLogout} data-testid="logout-button" style={{ cursor: 'pointer' }}>
            로그아웃
          </button>
        </header>
        <main style={{ flex: 1, padding: 24 }}>
          <Outlet />
        </main>
      </div>
    </div>
  );
}
