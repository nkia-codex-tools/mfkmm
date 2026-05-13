import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '@/features/auth/AuthContext';
import { Role } from '@/types';

interface MenuItem {
  label: string;
  path: string;
  minRole: Role;
}

const MENU_ITEMS: MenuItem[] = [
  { label: '리소스 검색', path: '/resources', minRole: 'READ' },
  { label: '리소스 등록', path: '/resources/new', minRole: 'WRITE' },
  { label: 'IMPORT', path: '/import', minRole: 'WRITE' },
  { label: 'EXPORT', path: '/export', minRole: 'READ' },
  { label: '사용자 관리', path: '/admin/users', minRole: 'ADMIN' },
  { label: '배포', path: '/admin/deploy', minRole: 'ADMIN' },
  { label: '작업 이력', path: '/admin/history', minRole: 'ADMIN' },
];

const ROLE_LEVEL: Record<Role, number> = {
  READ: 1, WRITE: 2, ADMIN: 3, ROOT_ADMIN: 4,
};

export default function Layout({ children }: { children: React.ReactNode }) {
  const { state, logout } = useAuth();
  const location = useLocation();
  const userRole = state.user?.role || 'READ';

  const visibleItems = MENU_ITEMS.filter(
    (item) => ROLE_LEVEL[userRole] >= ROLE_LEVEL[item.minRole]
  );

  return (
    <div className="min-h-screen flex">
      <aside className="w-64 bg-gray-800 text-white flex flex-col">
        <div className="p-4 border-b border-gray-700">
          <h1 className="text-lg font-bold">MKFMM</h1>
          <p className="text-sm text-gray-400">{state.user?.userId} ({userRole})</p>
        </div>
        <nav className="flex-1 p-4 space-y-1">
          {visibleItems.map((item) => (
            <Link
              key={item.path}
              to={item.path}
              className={`block px-3 py-2 rounded text-sm ${
                location.pathname === item.path
                  ? 'bg-gray-700 text-white'
                  : 'text-gray-300 hover:bg-gray-700 hover:text-white'
              }`}
            >
              {item.label}
            </Link>
          ))}
        </nav>
        <div className="p-4 border-t border-gray-700">
          <button
            onClick={logout}
            className="w-full text-left px-3 py-2 text-sm text-gray-300 hover:text-white"
          >
            로그아웃
          </button>
        </div>
      </aside>
      <main className="flex-1 bg-gray-50 p-6 overflow-auto">
        {children}
      </main>
    </div>
  );
}
