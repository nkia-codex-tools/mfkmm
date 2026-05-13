import { useState, useEffect } from 'react';
import client from '@/shared/api/client';
import { HISTORY } from '@/shared/api/endpoints';
import Pagination from '@/shared/components/Pagination';
import { Page } from '@/types';

interface WorkLog {
  id: string;
  workLogType: string;
  userId: string;
  resourceId: string | null;
  resourceKey: string | null;
  previousValue: string | null;
  newValue: string | null;
  details: string | null;
  performedAt: string;
}

export default function HistoryPage() {
  const [data, setData] = useState<Page<WorkLog> | null>(null);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(false);
  const [filters, setFilters] = useState({ workLogType: '', userId: '', startDate: '', endDate: '' });

  const fetchLogs = async (pageNum = 0) => {
    setLoading(true);
    try {
      const params: Record<string, string> = { page: String(pageNum), size: '20' };
      if (filters.workLogType) params.workLogType = filters.workLogType;
      if (filters.userId) params.userId = filters.userId;
      if (filters.startDate) params.startDate = filters.startDate;
      if (filters.endDate) params.endDate = filters.endDate;
      const res = await client.get(HISTORY.WORK_LOGS, { params });
      setData(res.data);
      setPage(pageNum);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchLogs(); }, []);

  return (
    <div>
      <h1 className="text-2xl font-bold mb-6">작업 이력</h1>

      <div className="flex gap-3 mb-4 flex-wrap">
        <select value={filters.workLogType} onChange={e => setFilters({...filters, workLogType: e.target.value})} className="px-3 py-2 border rounded-md" data-testid="history-type-filter">
          <option value="">전체 유형</option>
          <option value="RESOURCE_CREATED">리소스 등록</option>
          <option value="RESOURCE_UPDATED">리소스 수정</option>
          <option value="RESOURCE_DELETED">리소스 삭제</option>
          <option value="SEARCH">검색</option>
          <option value="IMPORT">IMPORT</option>
          <option value="EXPORT">EXPORT</option>
          <option value="DEPLOY">배포</option>
          <option value="USER_CREATED">사용자 생성</option>
          <option value="USER_DELETED">사용자 삭제</option>
          <option value="PERMISSION_CHANGED">권한 변경</option>
          <option value="LOGIN">로그인</option>
          <option value="LOGOUT">로그아웃</option>
        </select>
        <input type="text" placeholder="사용자 ID" value={filters.userId} onChange={e => setFilters({...filters, userId: e.target.value})} className="px-3 py-2 border rounded-md" />
        <input type="date" value={filters.startDate} onChange={e => setFilters({...filters, startDate: e.target.value})} className="px-3 py-2 border rounded-md" />
        <input type="date" value={filters.endDate} onChange={e => setFilters({...filters, endDate: e.target.value})} className="px-3 py-2 border rounded-md" />
        <button onClick={() => fetchLogs(0)} className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700">조회</button>
      </div>

      {loading ? (
        <p className="text-gray-500">로딩 중...</p>
      ) : data && data.content.length > 0 ? (
        <>
          <table className="w-full bg-white border rounded-lg overflow-hidden">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-4 py-3 text-left text-sm font-medium text-gray-500">유형</th>
                <th className="px-4 py-3 text-left text-sm font-medium text-gray-500">작업자</th>
                <th className="px-4 py-3 text-left text-sm font-medium text-gray-500">리소스 키</th>
                <th className="px-4 py-3 text-left text-sm font-medium text-gray-500">변경 전</th>
                <th className="px-4 py-3 text-left text-sm font-medium text-gray-500">변경 후</th>
                <th className="px-4 py-3 text-left text-sm font-medium text-gray-500">시각</th>
              </tr>
            </thead>
            <tbody className="divide-y">
              {data.content.map((log) => (
                <tr key={log.id} className="hover:bg-gray-50">
                  <td className="px-4 py-3 text-sm"><span className="px-2 py-1 bg-gray-100 rounded text-xs">{log.workLogType}</span></td>
                  <td className="px-4 py-3 text-sm">{log.userId}</td>
                  <td className="px-4 py-3 text-sm font-mono">{log.resourceKey || '-'}</td>
                  <td className="px-4 py-3 text-sm truncate max-w-[150px]">{log.previousValue || '-'}</td>
                  <td className="px-4 py-3 text-sm truncate max-w-[150px]">{log.newValue || '-'}</td>
                  <td className="px-4 py-3 text-sm">{new Date(log.performedAt).toLocaleString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
          <Pagination currentPage={page} totalPages={data.totalPages} onPageChange={fetchLogs} />
        </>
      ) : (
        <p className="text-gray-500">이력이 없습니다.</p>
      )}
    </div>
  );
}
