import { useState } from 'react';
import { adminApi } from '../../api/authApi';
import type { UserInfo } from '../../types/auth';

interface Props {
  user: UserInfo;
  open: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

export default function PermissionDialog({ user, open, onClose, onSuccess }: Props) {
  const [selectedRole, setSelectedRole] = useState(user.role);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  if (!open) return null;

  const handleSave = async () => {
    setLoading(true);
    setError(null);
    try {
      await adminApi.updateUserRole(user.id, { role: selectedRole });
      onSuccess();
    } catch (err: unknown) {
      const axiosErr = err as { response?: { data?: { message?: string } } };
      setError(axiosErr.response?.data?.message || '권한 변경에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div data-testid="permission-dialog" style={{
      position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
      background: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center'
    }}>
      <div style={{ background: '#fff', padding: 24, borderRadius: 8, minWidth: 320 }}>
        <h3>권한 변경</h3>
        <p><strong>{user.name}</strong> ({user.email})</p>
        <div style={{ marginBottom: 16 }}>
          <label htmlFor="role-select">역할 선택:</label>
          <select
            id="role-select"
            data-testid="role-select"
            value={selectedRole}
            onChange={(e) => setSelectedRole(e.target.value as UserInfo['role'])}
            style={{ display: 'block', width: '100%', padding: 8, marginTop: 4 }}
          >
            <option value="READER">READER (읽기 전용)</option>
            <option value="WRITER">WRITER (읽기/쓰기)</option>
            <option value="ADMIN">ADMIN (관리자)</option>
          </select>
        </div>
        {error && <div style={{ color: 'red', marginBottom: 16 }}>{error}</div>}
        <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
          <button onClick={onClose} data-testid="dialog-cancel">취소</button>
          <button onClick={handleSave} disabled={loading} data-testid="dialog-save">
            {loading ? '저장 중...' : '저장'}
          </button>
        </div>
      </div>
    </div>
  );
}
