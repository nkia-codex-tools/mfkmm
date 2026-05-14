import { useEffect, useState } from 'react';
import { adminApi } from '../../api/authApi';
import type { UserInfo } from '../../types/auth';
import PermissionDialog from './PermissionDialog';

export default function UserList() {
  const [users, setUsers] = useState<UserInfo[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [selectedUser, setSelectedUser] = useState<UserInfo | null>(null);

  const fetchUsers = async (p: number) => {
    setLoading(true);
    try {
      const res = await adminApi.getUsers(p);
      setUsers(res.data.content);
      setTotalPages(res.data.totalPages);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchUsers(page); }, [page]);

  if (loading) return <p>로딩 중...</p>;

  return (
    <div data-testid="user-list">
      <table style={{ width: '100%', borderCollapse: 'collapse' }}>
        <thead>
          <tr>
            <th style={{ textAlign: 'left', padding: 8, borderBottom: '1px solid #ddd' }}>이름</th>
            <th style={{ textAlign: 'left', padding: 8, borderBottom: '1px solid #ddd' }}>이메일</th>
            <th style={{ textAlign: 'left', padding: 8, borderBottom: '1px solid #ddd' }}>역할</th>
            <th style={{ textAlign: 'left', padding: 8, borderBottom: '1px solid #ddd' }}>상태</th>
            <th style={{ padding: 8, borderBottom: '1px solid #ddd' }}>작업</th>
          </tr>
        </thead>
        <tbody>
          {users.map((user) => (
            <tr key={user.id}>
              <td style={{ padding: 8 }}>{user.name}</td>
              <td style={{ padding: 8 }}>{user.email}</td>
              <td style={{ padding: 8 }}>{user.role}</td>
              <td style={{ padding: 8 }}>{user.status}</td>
              <td style={{ padding: 8, textAlign: 'center' }}>
                <button onClick={() => setSelectedUser(user)} data-testid={`edit-role-${user.id}`}>
                  역할 변경
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      <div style={{ marginTop: 16, display: 'flex', gap: 8 }}>
        <button disabled={page === 0} onClick={() => setPage(p => p - 1)}>이전</button>
        <span>{page + 1} / {totalPages || 1}</span>
        <button disabled={page >= totalPages - 1} onClick={() => setPage(p => p + 1)}>다음</button>
      </div>
      {selectedUser && (
        <PermissionDialog
          user={selectedUser}
          open={true}
          onClose={() => setSelectedUser(null)}
          onSuccess={() => { setSelectedUser(null); fetchUsers(page); }}
        />
      )}
    </div>
  );
}
