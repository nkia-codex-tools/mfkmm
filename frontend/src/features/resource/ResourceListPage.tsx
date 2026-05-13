import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import client from '@/shared/api/client';
import { RESOURCES } from '@/shared/api/endpoints';
import Pagination from '@/shared/components/Pagination';
import { Resource, ResourceType, Page } from '@/types';

export default function ResourceListPage() {
  const [query, setQuery] = useState('');
  const [resourceType, setResourceType] = useState<ResourceType | ''>('');
  const [page, setPage] = useState(0);
  const [data, setData] = useState<Page<Resource> | null>(null);
  const [loading, setLoading] = useState(false);

  const search = async (pageNum = 0) => {
    setLoading(true);
    try {
      const params: Record<string, string> = { page: String(pageNum), size: '20' };
      if (query) params.q = query;
      if (resourceType) params.type = resourceType;
      const response = await client.get(RESOURCES.SEARCH, { params });
      setData(response.data);
      setPage(pageNum);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { search(); }, []);

  return (
    <div>
      <h1 className="text-2xl font-bold mb-6">리소스 검색</h1>
      <div className="flex gap-3 mb-4">
        <input
          type="text"
          placeholder="키워드 검색..."
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && search(0)}
          className="flex-1 px-3 py-2 border rounded-md"
        />
        <select
          value={resourceType}
          onChange={(e) => setResourceType(e.target.value as ResourceType | '')}
          className="px-3 py-2 border rounded-md"
        >
          <option value="">전체 유형</option>
          <option value="MESSAGE_KEY">메시지 키</option>
          <option value="FUNCTION_ID">기능 ID</option>
          <option value="MENU_ID">메뉴 ID</option>
        </select>
        <button onClick={() => search(0)} className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700">
          검색
        </button>
      </div>

      {loading ? (
        <p className="text-gray-500">로딩 중...</p>
      ) : data && data.content.length > 0 ? (
        <>
          <table className="w-full bg-white border rounded-lg overflow-hidden">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-4 py-3 text-left text-sm font-medium text-gray-500">유형</th>
                <th className="px-4 py-3 text-left text-sm font-medium text-gray-500">키</th>
                <th className="px-4 py-3 text-left text-sm font-medium text-gray-500">내용</th>
                <th className="px-4 py-3 text-left text-sm font-medium text-gray-500">등록자</th>
                <th className="px-4 py-3 text-left text-sm font-medium text-gray-500">등록일</th>
              </tr>
            </thead>
            <tbody className="divide-y">
              {data.content.map((r) => (
                <tr key={r.id} className="hover:bg-gray-50">
                  <td className="px-4 py-3 text-sm">{r.resourceType}</td>
                  <td className="px-4 py-3 text-sm">
                    <Link to={`/resources/${r.id}`} className="text-blue-600 hover:underline">{r.key}</Link>
                  </td>
                  <td className="px-4 py-3 text-sm truncate max-w-xs">{r.content}</td>
                  <td className="px-4 py-3 text-sm">{r.createdBy}</td>
                  <td className="px-4 py-3 text-sm">{new Date(r.createdAt).toLocaleDateString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
          <Pagination currentPage={page} totalPages={data.totalPages} onPageChange={search} />
        </>
      ) : (
        <p className="text-gray-500">검색 결과가 없습니다.</p>
      )}
    </div>
  );
}
