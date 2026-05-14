import { useState, useRef } from 'react';
import { useResourceStore } from '../stores/useResourceStore';
import type { ResourceType } from '../types/resource';
import FunctionGrid from '../components/resource/FunctionGrid';
import MenuGrid from '../components/resource/MenuGrid';
import MessageResourceGrid from '../components/resource/MessageResourceGrid';
import GridToolbar from '../components/resource/GridToolbar';
import ImportDialog from '../components/resource/ImportDialog';
import ExportDialog from '../components/resource/ExportDialog';

const TABS: { key: ResourceType; label: string }[] = [
  { key: 'functions', label: '기능' },
  { key: 'menus', label: '메뉴' },
  { key: 'messageResources', label: '리소스 키' },
];

export default function ResourcePage() {
  const { activeTab, setActiveTab } = useResourceStore();
  const [importOpen, setImportOpen] = useState(false);
  const [exportOpen, setExportOpen] = useState(false);
  const gridRef = useRef<{ addRow: () => void; deleteSelected: () => void; refresh: () => void } | null>(null);

  return (
    <div data-testid="resource-page">
      <div style={{ display: 'flex', gap: 0, marginBottom: 16, borderBottom: '2px solid #1976d2' }}>
        {TABS.map((tab) => (
          <button
            key={tab.key}
            data-testid={`tab-${tab.key}`}
            onClick={() => setActiveTab(tab.key)}
            style={{
              padding: '10px 24px', cursor: 'pointer', border: 'none',
              background: activeTab === tab.key ? '#1976d2' : '#e3f2fd',
              color: activeTab === tab.key ? '#fff' : '#333',
              fontWeight: activeTab === tab.key ? 'bold' : 'normal',
            }}
          >
            {tab.label}
          </button>
        ))}
      </div>
      <GridToolbar
        onAddRow={() => gridRef.current?.addRow()}
        onDeleteSelected={() => gridRef.current?.deleteSelected()}
        onImport={() => setImportOpen(true)}
        onExport={() => setExportOpen(true)}
      />
      <div style={{ marginTop: 16 }}>
        {activeTab === 'functions' && <FunctionGrid ref={gridRef} />}
        {activeTab === 'menus' && <MenuGrid ref={gridRef} />}
        {activeTab === 'messageResources' && <MessageResourceGrid ref={gridRef} />}
      </div>
      <ImportDialog open={importOpen} onClose={() => setImportOpen(false)} onSuccess={() => { setImportOpen(false); gridRef.current?.refresh(); }} />
      <ExportDialog open={exportOpen} onClose={() => setExportOpen(false)} />
    </div>
  );
}
