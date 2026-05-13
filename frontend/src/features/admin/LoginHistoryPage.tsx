import { useState, useEffect } from 'react';
import client from '@/shared/api/client';

import Pagination from '@/shared/components/Pagination';
import { Page } from '@/types';

interface LoginHistory {
  id: string;
  userId: string;
  loginAt: string;
  ipAddress: string;
  success: boolean;
  failureReason: string | null;
}

export default function LoginHistoryPage() {
  const [data, setData] = useState<Page<LoginHistory> | null>(null);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(false);
  const [filters, setFilters] = useState({ userId: '', success: '' });

  const fetchHistory = async (pageNum = 0) => {
    setLoading(true);
    try {
      const params: Record<string, string> = { page: String(pageNum), size: '20' };
      if (filters.userId) params.userId = filters.userId;
      if (filters.success) params.success = filters.success;
      const res = await client.get('/auth/login-history', { params });
      setData(res.data);
      setPage(pageNum);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchHistory(); }, []);

  return (
    <div>
      <h1 className="text-2xl font-bold mb-6">로그인 이력</h1>

      <div className="flex gap-3 mb-4">
        <input type="text" placeholder="사용자 ID" value={filters.userId} onChange={e => setFilters({...filters, userId: e.target.value})} className="px-3 py-2 border rounded-md" />
        <select value={filters.success} onChange={e => setFilters({...filters, success: e.target.value})} className="px-3 py-2 border rounded-md">
          <option value="">전체</option>
          <option value="true">성공</option>
          <option value="false">실패</option>
        </select>
        <button onClick={() => fetchHistory(0)} className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700">조회</button>
      </div>

      {loading ? (
        <p className="text-gray-500">로딩 중...</p>
      ) : data && data.content.length > 0 ? (
        <>
          <table className="w-full bg-white border rounded-lg overflow-hidden">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-4 py-3 text-left text-sm font-medium text-gray-500">사용자</th>
                <th className="px-4 py-3 text-left text-sm font-medium text-gray-500">시각</th>
                <th className="px-4 py-3 text-left text-sm font-medium text-gray-500">IP</th>
                <th className="px-4 py-3 text-left text-sm font-medium text-gray-500">결과</th>
                <th className="px-4 py-3 text-left text-sm font-medium text-gray-500">사유</th>
              </tr>
            </thead>
            <tbody className="divide-y">
              {data.content.map((h) => (
                <tr key={h.id} className="hover:bg-gray-50">
                  <td className="px-4 py-3 text-sm">{h.userId}</td>
                  <td className="px-4 py-3 text-sm">{new Date(h.loginAt).toLocaleString()}</td>
                  <td className="px-4 py-3 text-sm font-mono">{h.ipAddress || '-'}</td>
                  <td className="px-4 py-3 text-sm">
                    {h.success ? (
                      <span className="px-2 py-1 bg-green-100 text-green-700 rounded text-xs">성공</span>
                    ) : (
                      <span className="px-2 py-1 bg-red-100 text-red-700 rounded text-xs">실패</span>
                    )}
                  </td>
                  <td className="px-4 py-3 text-sm text-gray-500">{h.failureReason || '-'}</td>
                </tr>
              ))}
            </tbody>
          </table>
          <Pagination currentPage={page} totalPages={data.totalPages} onPageChange={fetchHistory} />
        </>
      ) : (
        <p className="text-gray-500">로그인 이력이 없습니다.</p>
      )}
    </div>
  );
}
