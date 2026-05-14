import { useState } from 'react';
import { transferApi } from '../../api/transferApi';
import { useResourceStore } from '../../stores/useResourceStore';

interface Props {
  open: boolean;
  onClose: () => void;
}

const RESOURCE_TYPE_MAP = { functions: 'functions', menus: 'menus', messageResources: 'message-resources' } as const;
const FILENAME_MAP = { functions: 'functions.tsv', menus: 'menus.tsv', messageResources: 'message-resource.tsv' } as const;

export default function ExportDialog({ open, onClose }: Props) {
  const { activeTab, selectedRowIds } = useResourceStore();
  const [exportType, setExportType] = useState<'all' | 'selected'>('all');
  const [loading, setLoading] = useState(false);

  if (!open) return null;

  const handleExport = async () => {
    setLoading(true);
    try {
      const ids = exportType === 'selected' ? selectedRowIds : undefined;
      const res = await transferApi.export(RESOURCE_TYPE_MAP[activeTab], ids);

      const blob = new Blob([res.data], { type: 'text/tab-separated-values' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = FILENAME_MAP[activeTab];
      a.click();
      URL.revokeObjectURL(url);
      onClose();
    } finally {
      setLoading(false);
    }
  };

  return (
    <div data-testid="export-dialog" style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, background: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
      <div style={{ background: '#fff', padding: 24, borderRadius: 8, minWidth: 320 }}>
        <h3>TSV Export</h3>
        <div style={{ marginBottom: 16 }}>
          <label>
            <input type="radio" name="exportType" value="all" checked={exportType === 'all'} onChange={() => setExportType('all')} />
            전체 내보내기
          </label>
          <br />
          <label>
            <input type="radio" name="exportType" value="selected" checked={exportType === 'selected'} onChange={() => setExportType('selected')} disabled={selectedRowIds.length === 0} />
            선택된 행만 ({selectedRowIds.length}개)
          </label>
        </div>
        <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
          <button onClick={onClose} data-testid="export-cancel-btn">취소</button>
          <button onClick={handleExport} disabled={loading} data-testid="export-download-btn">
            {loading ? '다운로드 중...' : '다운로드'}
          </button>
        </div>
      </div>
    </div>
  );
}
