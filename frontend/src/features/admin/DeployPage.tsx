import { useState, useEffect } from 'react';
import client from '@/shared/api/client';
import { DEPLOY } from '@/shared/api/endpoints';
import { useToast } from '@/shared/components/Toast';
import Pagination from '@/shared/components/Pagination';
import { Deployment, FileFormat, Page } from '@/types';

export default function DeployPage() {
  const [format, setFormat] = useState<FileFormat>('JSON');
  const [loading, setLoading] = useState(false);
  const [history, setHistory] = useState<Page<Deployment> | null>(null);
  const [page, setPage] = useState(0);
  const { showToast } = useToast();

  const fetchHistory = async (pageNum = 0) => {
    try {
      const response = await client.get(DEPLOY.HISTORY, { params: { page: pageNum, size: 10 } });
      setHistory(response.data);
      setPage(pageNum);
    } catch {
      /* ignore */
    }
  };

  useEffect(() => { fetchHistory(); }, []);

  const handleDeploy = async () => {
    if (!confirm('전체 데이터를 배포하시겠습니까?')) return;
    setLoading(true);
    try {
      await client.post(DEPLOY.BASE, { format });
      showToast('success', '배포 완료');
      fetchHistory(0);
    } catch {
      showToast('error', '배포 실패');
    } finally {
      setLoading(false);
    }
  };

  const handleDownload = async (id: string) => {
    try {
      const response = await client.get(`${DEPLOY.BASE}/${id}/download`, { responseType: 'blob' });
      const blob = new Blob([response.data]);
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `deployment-${id}`;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(url);
    } catch {
      showToast('error', '다운로드 실패');
    }
  };

  return (
    <div>
      <h1 className="text-2xl font-bold mb-6">배포 관리</h1>

      <div className="bg-white border rounded-lg p-4 mb-6">
        <h2 className="text-lg font-semibold mb-3">새 배포</h2>
        <div className="flex gap-3 items-end">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">형식</label>
            <select
              value={format}
              onChange={(e) => setFormat(e.target.value as FileFormat)}
              className="px-3 py-2 border rounded-md"
            >
              <option value="EXCEL">Excel</option>
              <option value="TSV">TSV</option>
              <option value="JSON">JSON</option>
            </select>
          </div>
          <button
            onClick={handleDeploy}
            disabled={loading}
            className="px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 disabled:opacity-50"
          >
            {loading ? '배포 중...' : '배포 실행'}
          </button>
        </div>
      </div>

      <div className="bg-white border rounded-lg p-4">
        <h2 className="text-lg font-semibold mb-3">배포 이력</h2>
        {history && history.content.length > 0 ? (
          <>
            <table className="w-full text-sm">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-4 py-2 text-left">버전</th>
                  <th className="px-4 py-2 text-left">형식</th>
                  <th className="px-4 py-2 text-left">건수</th>
                  <th className="px-4 py-2 text-left">배포자</th>
                  <th className="px-4 py-2 text-left">시각</th>
                  <th className="px-4 py-2 text-left">다운로드</th>
                </tr>
              </thead>
              <tbody className="divide-y">
                {history.content.map((d) => (
                  <tr key={d.id}>
                    <td className="px-4 py-2">{d.version}</td>
                    <td className="px-4 py-2">{d.format}</td>
                    <td className="px-4 py-2">{d.totalRecords}</td>
                    <td className="px-4 py-2">{d.userId}</td>
                    <td className="px-4 py-2">{new Date(d.createdAt).toLocaleString()}</td>
                    <td className="px-4 py-2">
                      <button
                        onClick={() => handleDownload(d.id)}
                        className="text-blue-600 hover:underline"
                      >
                        재다운로드
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            <Pagination currentPage={page} totalPages={history.totalPages} onPageChange={fetchHistory} />
          </>
        ) : (
          <p className="text-gray-500">배포 이력이 없습니다.</p>
        )}
      </div>
    </div>
  );
}
