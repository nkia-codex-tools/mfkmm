import { useEffect, useState } from 'react';
import { adminApi } from '../../api/authApi';
import type { UserInfo } from '../../types/auth';
import PermissionDialog from './PermissionDialog';

export default function PendingUserList() {
  const [users, setUsers] = useState<UserInfo[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedUser, setSelectedUser] = useState<UserInfo | null>(null);

  const fetchPending = async () => {
    setLoading(true);
    try {
      const res = await adminApi.getPendingUsers();
      setUsers(res.data);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchPending(); }, []);

  if (loading) return <p>로딩 중...</p>;
  if (users.length === 0) return <p>승인 대기 중인 사용자가 없습니다.</p>;

  return (
    <div data-testid="pending-user-list">
      <table style={{ width: '100%', borderCollapse: 'collapse' }}>
        <thead>
          <tr>
            <th style={{ textAlign: 'left', padding: 8, borderBottom: '1px solid #ddd' }}>이름</th>
            <th style={{ textAlign: 'left', padding: 8, borderBottom: '1px solid #ddd' }}>이메일</th>
            <th style={{ textAlign: 'left', padding: 8, borderBottom: '1px solid #ddd' }}>가입일시</th>
            <th style={{ padding: 8, borderBottom: '1px solid #ddd' }}>작업</th>
          </tr>
        </thead>
        <tbody>
          {users.map((user) => (
            <tr key={user.id}>
              <td style={{ padding: 8 }}>{user.name}</td>
              <td style={{ padding: 8 }}>{user.email}</td>
              <td style={{ padding: 8 }}>{new Date(user.createdAt).toLocaleString('ko-KR')}</td>
              <td style={{ padding: 8, textAlign: 'center' }}>
                <button onClick={() => setSelectedUser(user)} data-testid={`grant-permission-${user.id}`}>
                  권한 부여
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      {selectedUser && (
        <PermissionDialog
          user={selectedUser}
          open={true}
          onClose={() => setSelectedUser(null)}
          onSuccess={() => { setSelectedUser(null); fetchPending(); }}
        />
      )}
    </div>
  );
}
