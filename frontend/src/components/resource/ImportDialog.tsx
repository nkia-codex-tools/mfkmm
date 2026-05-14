import { useState } from 'react';
import { transferApi } from '../../api/transferApi';
import { useResourceStore } from '../../stores/useResourceStore';

interface Props {
  open: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

const RESOURCE_TYPE_MAP = { functions: 'functions', menus: 'menus', messageResources: 'message-resources' } as const;

export default function ImportDialog({ open, onClose, onSuccess }: Props) {
  const { activeTab } = useResourceStore();
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState<{ created: number; failed: number } | null>(null);
  const [error, setError] = useState<string | null>(null);

  if (!open) return null;

  const resourceType = RESOURCE_TYPE_MAP[activeTab];

  const handleFileSelect = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    setLoading(true);
    setError(null);
    setResult(null);

    try {
      const res = await transferApi.importApply(resourceType, file, 'overwrite');
      setResult(res.data);
      onSuccess();
    } catch (err: unknown) {
      const axiosErr = err as { response?: { data?: { message?: string } } };
      setError(axiosErr.response?.data?.message || 'Import에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    setResult(null);
    setError(null);
    onClose();
  };

  return (
    <div data-testid="import-dialog" style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, background: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 }}>
      <div style={{ background: '#fff', padding: 24, borderRadius: 8, minWidth: 400 }}>
        <h3>TSV Import</h3>

        {!result && !loading && (
          <div style={{ marginBottom: 16 }}>
            <p style={{ marginBottom: 12, color: '#666' }}>TSV 파일을 선택하면 바로 등록됩니다.</p>
            <input type="file" accept=".tsv,.txt" onChange={handleFileSelect} data-testid="import-file-input" />
          </div>
        )}

        {loading && <p>업로드 중...</p>}

        {error && <div style={{ color: 'red', marginBottom: 16 }}>{error}</div>}

        {result && (
          <div data-testid="import-result" style={{ marginBottom: 16 }}>
            <p style={{ color: '#2e7d32', fontWeight: 'bold' }}>Import 완료</p>
            <p>생성: {result.created}건{result.failed > 0 && `, 실패: ${result.failed}건`}</p>
          </div>
        )}

        <div style={{ textAlign: 'right' }}>
          <button onClick={handleClose} data-testid="import-close-btn">닫기</button>
        </div>
      </div>
    </div>
  );
}
