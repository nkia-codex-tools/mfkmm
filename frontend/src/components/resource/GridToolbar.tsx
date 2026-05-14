import { useResourceStore } from '../../stores/useResourceStore';
import { useAuthStore } from '../../stores/useAuthStore';

interface Props {
  onAddRow?: () => void;
  onDeleteSelected?: () => void;
  onImport?: () => void;
  onExport?: () => void;
}

export default function GridToolbar({ onAddRow, onDeleteSelected, onImport, onExport }: Props) {
  const { selectedRowIds, searchQuery, setSearchQuery } = useResourceStore();
  const { hasWritePermission } = useAuthStore();

  return (
    <div data-testid="grid-toolbar" style={{ display: 'flex', gap: 8, alignItems: 'center', flexWrap: 'wrap' }}>
      {hasWritePermission() && (
        <>
          <button onClick={onAddRow} data-testid="toolbar-add-row">행 추가</button>
          <button
            onClick={onDeleteSelected}
            disabled={selectedRowIds.length === 0}
            data-testid="toolbar-delete-selected"
          >
            선택 삭제 ({selectedRowIds.length})
          </button>
          <button onClick={onImport} data-testid="toolbar-import">Import</button>
        </>
      )}
      <button onClick={onExport} data-testid="toolbar-export">Export</button>
      <div style={{ marginLeft: 'auto' }}>
        <input
          type="text"
          placeholder="검색..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          data-testid="toolbar-search"
          style={{ padding: 8, width: 200 }}
        />
      </div>
    </div>
  );
}
