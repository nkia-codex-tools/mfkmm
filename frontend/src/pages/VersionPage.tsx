import { useEffect, useState } from 'react';
import { versionApi, type VersionTag, type VersionDiff } from '../api/versionApi';

export default function VersionPage() {
  const [versions, setVersions] = useState<VersionTag[]>([]);
  const [loading, setLoading] = useState(true);
  const [resourceType, setResourceType] = useState('');
  const [showCreate, setShowCreate] = useState(false);
  const [newTag, setNewTag] = useState({ tagName: '', description: '', resourceType: 'functions' });
  const [diff, setDiff] = useState<VersionDiff | null>(null);
  const [rollbackTarget, setRollbackTarget] = useState<VersionTag | null>(null);

  const fetchVersions = async () => {
    setLoading(true);
    try {
      const res = await versionApi.getVersions(resourceType || undefined);
      setVersions(res.data);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchVersions(); }, [resourceType]);

  const handleCreate = async () => {
    await versionApi.createTag(newTag);
    setShowCreate(false);
    setNewTag({ tagName: '', description: '', resourceType: 'functions' });
    fetchVersions();
  };

  const handleViewDiff = async (id: number) => {
    const res = await versionApi.getDiff(id);
    setDiff(res.data);
  };

  const handleRollback = async () => {
    if (!rollbackTarget) return;
    await versionApi.rollback(rollbackTarget.id);
    setRollbackTarget(null);
    fetchVersions();
  };

  return (
    <div data-testid="version-page">
      <h1>버전 관리</h1>

      <div style={{ display: 'flex', gap: 16, marginBottom: 16 }}>
        <select value={resourceType} onChange={(e) => setResourceType(e.target.value)} data-testid="version-filter-type">
          <option value="">전체</option>
          <option value="functions">기능</option>
          <option value="menus">메뉴</option>
          <option value="message-resources">리소스 키</option>
        </select>
        <button onClick={() => setShowCreate(true)} data-testid="version-create-btn">새 버전 태그</button>
      </div>

      {showCreate && (
        <div style={{ border: '1px solid #ddd', padding: 16, marginBottom: 16, borderRadius: 4 }}>
          <h3>버전 태그 생성</h3>
          <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
            <input placeholder="태그 이름 (예: v1.0)" value={newTag.tagName}
              onChange={(e) => setNewTag({ ...newTag, tagName: e.target.value })} data-testid="version-tag-name" />
            <input placeholder="설명 (선택)" value={newTag.description}
              onChange={(e) => setNewTag({ ...newTag, description: e.target.value })} data-testid="version-tag-desc" />
            <select value={newTag.resourceType} onChange={(e) => setNewTag({ ...newTag, resourceType: e.target.value })} data-testid="version-tag-type">
              <option value="functions">기능</option>
              <option value="menus">메뉴</option>
              <option value="message-resources">리소스 키</option>
            </select>
            <button onClick={handleCreate} data-testid="version-tag-save">저장</button>
            <button onClick={() => setShowCreate(false)}>취소</button>
          </div>
        </div>
      )}

      {loading ? <p>로딩 중...</p> : (
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead>
            <tr>
              <th style={{ padding: 8, borderBottom: '1px solid #ddd', textAlign: 'left' }}>태그</th>
              <th style={{ padding: 8, borderBottom: '1px solid #ddd', textAlign: 'left' }}>설명</th>
              <th style={{ padding: 8, borderBottom: '1px solid #ddd', textAlign: 'left' }}>리소스</th>
              <th style={{ padding: 8, borderBottom: '1px solid #ddd', textAlign: 'left' }}>생성일시</th>
              <th style={{ padding: 8, borderBottom: '1px solid #ddd' }}>작업</th>
            </tr>
          </thead>
          <tbody>
            {versions.map((v) => (
              <tr key={v.id}>
                <td style={{ padding: 8 }}><strong>{v.tagName}</strong></td>
                <td style={{ padding: 8 }}>{v.description || '-'}</td>
                <td style={{ padding: 8 }}>{v.resourceType}</td>
                <td style={{ padding: 8 }}>{new Date(v.createdAt).toLocaleString('ko-KR')}</td>
                <td style={{ padding: 8, display: 'flex', gap: 4 }}>
                  <button onClick={() => handleViewDiff(v.id)} data-testid={`version-diff-${v.id}`}>비교</button>
                  <button onClick={() => setRollbackTarget(v)} data-testid={`version-rollback-${v.id}`}
                    style={{ color: 'red' }}>롤백</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      {diff && (
        <div data-testid="version-diff-view" style={{ marginTop: 16, border: '1px solid #ddd', padding: 16, borderRadius: 4 }}>
          <h3>버전 비교: {diff.tagName}</h3>
          <p>스냅샷 행 수: {diff.snapshotRowCount} | 현재 행 수: {diff.currentRowCount}</p>
          <pre style={{ background: '#f5f5f5', padding: 8, overflow: 'auto', maxHeight: 200, fontSize: 12 }}>
            {diff.snapshotPreview}
          </pre>
          <button onClick={() => setDiff(null)}>닫기</button>
        </div>
      )}

      {rollbackTarget && (
        <div data-testid="rollback-confirm" style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, background: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <div style={{ background: '#fff', padding: 24, borderRadius: 8 }}>
            <h3>롤백 확인</h3>
            <p>정말로 <strong>{rollbackTarget.tagName}</strong> 버전으로 롤백하시겠습니까?</p>
            <p style={{ color: 'red' }}>현재 {rollbackTarget.resourceType} 데이터가 스냅샷으로 대체됩니다.</p>
            <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
              <button onClick={() => setRollbackTarget(null)} data-testid="rollback-cancel">취소</button>
              <button onClick={handleRollback} style={{ background: 'red', color: '#fff', border: 'none', padding: '8px 16px', cursor: 'pointer' }} data-testid="rollback-confirm-btn">롤백 실행</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
