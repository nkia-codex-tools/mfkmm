import { useState, useEffect } from 'react';
import client from '@/shared/api/client';
import { USERS } from '@/shared/api/endpoints';
import Pagination from '@/shared/components/Pagination';
import { Page, Role } from '@/types';

interface UserItem {
  id: string;
  userId: string;
  name: string;
  email: string;
  department: string;
  role: Role;
  isLocked: boolean;
  createdAt: string;
}

export default function UserManagementPage() {
  const [users, setUsers] = useState<Page<UserItem> | null>(null);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(false);
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [form, setForm] = useState({
    userId: '', name: '', email: '', department: '', role: 'WRITE' as Role, password: '', memo: ''
  });

  const fetchUsers = async (pageNum = 0) => {
    setLoading(true);
    try {
      const res = await client.get(USERS.BASE, { params: { page: pageNum, size: 20 } });
      setUsers(res.data);
      setPage(pageNum);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchUsers(); }, []);

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await client.post(USERS.BASE, form);
      setShowCreateForm(false);
      setForm({ userId: '', name: '', email: '', department: '', role: 'WRITE', password: '', memo: '' });
      fetchUsers(page);
    } catch (err: any) {
      alert(err.response?.data?.message || '사용자 등록 실패');
    }
  };

  const handleDelete = async (id: string, userId: string) => {
    if (!confirm(`${userId} 사용자를 삭제하시겠습니까?`)) return;
    try {
      await client.delete(`${USERS.BASE}/${id}`);
      fetchUsers(page);
    } catch (err: any) {
      alert(err.response?.data?.message || '삭제 실패');
    }
  };

  const handleUnlock = async (userId: string) => {
    try {
      await client.post(`${USERS.BASE}/${userId}/unlock`);
      fetchUsers(page);
    } catch (err: any) {
      alert(err.response?.data?.message || '잠금 해제 실패');
    }
  };

  const handleRoleChange = async (userId: string, newRole: Role) => {
    try {
      await client.put(`${USERS.BASE}/${userId}/permission`, { role: newRole });
      fetchUsers(page);
    } catch (err: any) {
      alert(err.response?.data?.message || '권한 변경 실패');
    }
  };

  return (
    <div>
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold">사용자 관리</h1>
        <button
          onClick={() => setShowCreateForm(!showCreateForm)}
          className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
          data-testid="create-user-button"
        >
          {showCreateForm ? '취소' : '사용자 등록'}
        </button>
      </div>

      {showCreateForm && (
        <form onSubmit={handleCreate} className="mb-6 p-4 bg-gray-50 rounded-lg space-y-3">
          <div className="grid grid-cols-2 gap-3">
            <input placeholder="사용자 ID" value={form.userId} onChange={e => setForm({...form, userId: e.target.value})} required className="px-3 py-2 border rounded-md" data-testid="new-user-id" />
            <input placeholder="이름" value={form.name} onChange={e => setForm({...form, name: e.target.value})} required className="px-3 py-2 border rounded-md" data-testid="new-user-name" />
            <input placeholder="이메일" value={form.email} onChange={e => setForm({...form, email: e.target.value})} className="px-3 py-2 border rounded-md" />
            <input placeholder="부서" value={form.department} onChange={e => setForm({...form, department: e.target.value})} className="px-3 py-2 border rounded-md" />
            <input type="password" placeholder="초기 비밀번호" value={form.password} onChange={e => setForm({...form, password: e.target.value})} required className="px-3 py-2 border rounded-md" data-testid="new-user-password" />
            <select value={form.role} onChange={e => setForm({...form, role: e.target.value as Role})} className="px-3 py-2 border rounded-md">
              <option value="READ">Read</option>
              <option value="WRITE">Write</option>
              <option value="ADMIN">Admin</option>
            </select>
          </div>
          <button type="submit" className="px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700" data-testid="submit-create-user">등록</button>
        </form>
      )}

      {loading ? (
        <p className="text-gray-500">로딩 중...</p>
      ) : users && users.content.length > 0 ? (
        <>
          <table className="w-full bg-white border rounded-lg overflow-hidden">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-4 py-3 text-left text-sm font-medium text-gray-500">ID</th>
                <th className="px-4 py-3 text-left text-sm font-medium text-gray-500">이름</th>
                <th className="px-4 py-3 text-left text-sm font-medium text-gray-500">이메일</th>
                <th className="px-4 py-3 text-left text-sm font-medium text-gray-500">부서</th>
                <th className="px-4 py-3 text-left text-sm font-medium text-gray-500">권한</th>
                <th className="px-4 py-3 text-left text-sm font-medium text-gray-500">상태</th>
                <th className="px-4 py-3 text-left text-sm font-medium text-gray-500">액션</th>
              </tr>
            </thead>
            <tbody className="divide-y">
              {users.content.map((u) => (
                <tr key={u.id} className="hover:bg-gray-50">
                  <td className="px-4 py-3 text-sm">{u.userId}</td>
                  <td className="px-4 py-3 text-sm">{u.name}</td>
                  <td className="px-4 py-3 text-sm">{u.email || '-'}</td>
                  <td className="px-4 py-3 text-sm">{u.department || '-'}</td>
                  <td className="px-4 py-3 text-sm">
                    <select
                      value={u.role}
                      onChange={e => handleRoleChange(u.userId, e.target.value as Role)}
                      disabled={u.role === 'ROOT_ADMIN'}
                      className="px-2 py-1 border rounded text-sm"
                    >
                      <option value="READ">Read</option>
                      <option value="WRITE">Write</option>
                      <option value="ADMIN">Admin</option>
                      {u.role === 'ROOT_ADMIN' && <option value="ROOT_ADMIN">Root Admin</option>}
                    </select>
                  </td>
                  <td className="px-4 py-3 text-sm">
                    {u.isLocked ? (
                      <span className="px-2 py-1 bg-red-100 text-red-700 rounded text-xs">잠김</span>
                    ) : (
                      <span className="px-2 py-1 bg-green-100 text-green-700 rounded text-xs">정상</span>
                    )}
                  </td>
                  <td className="px-4 py-3 text-sm space-x-2">
                    {u.isLocked && (
                      <button onClick={() => handleUnlock(u.userId)} className="px-2 py-1 bg-yellow-500 text-white rounded text-xs hover:bg-yellow-600">잠금해제</button>
                    )}
                    {u.role !== 'ROOT_ADMIN' && (
                      <button onClick={() => handleDelete(u.id, u.userId)} className="px-2 py-1 bg-red-500 text-white rounded text-xs hover:bg-red-600">삭제</button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          <Pagination currentPage={page} totalPages={users.totalPages} onPageChange={fetchUsers} />
        </>
      ) : (
        <p className="text-gray-500">등록된 사용자가 없습니다.</p>
      )}
    </div>
  );
}
