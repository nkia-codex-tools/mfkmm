import { useEffect, useState } from 'react';
import { historyApi, type HistoryItem } from '../api/historyApi';

export default function HistoryPage() {
  const [items, setItems] = useState<HistoryItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [resourceType, setResourceType] = useState<string>('');
  const [dateFrom, setDateFrom] = useState('');
  const [dateTo, setDateTo] = useState('');

  const fetchHistory = async () => {
    setLoading(true);
    try {
      const params: Record<string, unknown> = { page, size: 50 };
      if (resourceType) params.resourceType = resourceType;
      if (dateFrom) params.from = new Date(dateFrom).toISOString();
      if (dateTo) params.to = new Date(dateTo).toISOString();

      const res = await historyApi.getHistory(params as Parameters<typeof historyApi.getHistory>[0]);
      setItems(res.data.content);
      setTotalPages(res.data.totalPages);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchHistory(); }, [page, resourceType, dateFrom, dateTo]);

  const changeTypeLabel = (type: string) => {
    switch (type) {
      case 'CREATE': return '추가';
      case 'UPDATE': return '수정';
      case 'DELETE': return '삭제';
      case 'ROLLBACK': return '롤백';
      default: return type;
    }
  };

  return (
    <div data-testid="history-page">
      <h1>변경 이력</h1>
      <div style={{ display: 'flex', gap: 16, marginBottom: 16, flexWrap: 'wrap' }}>
        <select value={resourceType} onChange={(e) => { setResourceType(e.target.value); setPage(0); }} data-testid="history-filter-type">
          <option value="">전체 리소스</option>
          <option value="functions">기능</option>
          <option value="menus">메뉴</option>
          <option value="message_resources">리소스 키</option>
        </select>
        <input type="date" value={dateFrom} onChange={(e) => { setDateFrom(e.target.value); setPage(0); }} data-testid="history-filter-from" />
        <input type="date" value={dateTo} onChange={(e) => { setDateTo(e.target.value); setPage(0); }} data-testid="history-filter-to" />
      </div>

      {loading ? <p>로딩 중...</p> : (
        <>
          <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 14 }}>
            <thead>
              <tr>
                <th style={{ padding: 8, borderBottom: '1px solid #ddd', textAlign: 'left' }}>일시</th>
                <th style={{ padding: 8, borderBottom: '1px solid #ddd', textAlign: 'left' }}>리소스</th>
                <th style={{ padding: 8, borderBottom: '1px solid #ddd', textAlign: 'left' }}>유형</th>
                <th style={{ padding: 8, borderBottom: '1px solid #ddd', textAlign: 'left' }}>필드</th>
                <th style={{ padding: 8, borderBottom: '1px solid #ddd', textAlign: 'left' }}>이전 값</th>
                <th style={{ padding: 8, borderBottom: '1px solid #ddd', textAlign: 'left' }}>새 값</th>
              </tr>
            </thead>
            <tbody>
              {items.map((item) => (
                <tr key={item.id}>
                  <td style={{ padding: 8 }}>{new Date(item.changedAt).toLocaleString('ko-KR')}</td>
                  <td style={{ padding: 8 }}>{item.resourceType}</td>
                  <td style={{ padding: 8 }}>{changeTypeLabel(item.changeType)}</td>
                  <td style={{ padding: 8 }}>{item.fieldName || '-'}</td>
                  <td style={{ padding: 8, maxWidth: 200, overflow: 'hidden', textOverflow: 'ellipsis' }}>{item.oldValue || '-'}</td>
                  <td style={{ padding: 8, maxWidth: 200, overflow: 'hidden', textOverflow: 'ellipsis' }}>{item.newValue || '-'}</td>
                </tr>
              ))}
            </tbody>
          </table>
          <div style={{ marginTop: 16, display: 'flex', gap: 8 }}>
            <button disabled={page === 0} onClick={() => setPage(p => p - 1)}>이전</button>
            <span>{page + 1} / {totalPages || 1}</span>
            <button disabled={page >= totalPages - 1} onClick={() => setPage(p => p + 1)}>다음</button>
          </div>
        </>
      )}
    </div>
  );
}
