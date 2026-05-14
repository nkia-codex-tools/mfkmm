import { useEffect, useState, useCallback, useRef, forwardRef, useImperativeHandle } from 'react';
import { AgGridReact } from 'ag-grid-react';
import type { ColDef, CellValueChangedEvent, RowDragEndEvent } from 'ag-grid-community';
import 'ag-grid-community/styles/ag-grid.css';
import 'ag-grid-community/styles/ag-theme-alpine.css';
import { functionApi } from '../../api/resourceApi';
import { useResourceStore } from '../../stores/useResourceStore';
import { useAuthStore } from '../../stores/useAuthStore';
import type { FunctionResource } from '../../types/resource';
import { ACTION_OPTIONS, TYPE_OPTIONS, BOOLEAN_OPTIONS } from '../../types/resource';

export interface GridHandle {
  addRow: () => void;
  deleteSelected: () => void;
  refresh: () => void;
}

const FunctionGrid = forwardRef<GridHandle>((_props, ref) => {
  const [rowData, setRowData] = useState<FunctionResource[]>([]);
  const gridRef = useRef<AgGridReact>(null);
  const { searchQuery, setSelectedRows, selectedRowIds } = useResourceStore();
  const { hasWritePermission } = useAuthStore();
  const editable = hasWritePermission();

  const columnDefs: ColDef<FunctionResource>[] = [
    { rowDrag: editable, width: 40, suppressMenu: true, lockPosition: true, checkboxSelection: true, headerCheckboxSelection: true },
    { field: 'aClass', headerName: 'A Class', editable },
    { field: 'bClass', headerName: 'B Class', editable },
    { field: 'cClass', headerName: 'C Class', editable },
    { field: 'action', headerName: '기능(ACTION)', editable, cellEditor: 'agSelectCellEditor', cellEditorParams: { values: ACTION_OPTIONS } },
    { field: 'functionName', headerName: '기능명', editable },
    { field: 'functionId', headerName: '기능(FUNCTION) ID', editable },
    { field: 'type', headerName: '유형', editable, cellEditor: 'agSelectCellEditor', cellEditorParams: { values: TYPE_OPTIONS } },
    { field: 'light', headerName: 'Light', editable, cellEditor: 'agSelectCellEditor', cellEditorParams: { values: BOOLEAN_OPTIONS }, valueFormatter: p => p.value ? 'TRUE' : 'FALSE' },
    { field: 'standard', headerName: 'Standard', editable, cellEditor: 'agSelectCellEditor', cellEditorParams: { values: BOOLEAN_OPTIONS }, valueFormatter: p => p.value ? 'TRUE' : 'FALSE' },
    { field: 'enterprise', headerName: 'Enterprise', editable, cellEditor: 'agSelectCellEditor', cellEditorParams: { values: BOOLEAN_OPTIONS }, valueFormatter: p => p.value ? 'TRUE' : 'FALSE' },
    { field: 'systemMenu', headerName: 'System Menu', editable, cellEditor: 'agSelectCellEditor', cellEditorParams: { values: BOOLEAN_OPTIONS }, valueFormatter: p => p.value ? 'TRUE' : 'FALSE' },
    { field: 'productDomain', headerName: 'Product Domain', editable },
    { field: 'domainLicenseResourceType', headerName: '도메인 라이선스 리소스 타입', editable },
    { field: 'relatedServices', headerName: '관련 컨테이너 서비스명', editable },
    { field: 'resourceKey', headerName: '기능 ID 리소스키(자동생성)', editable: false, cellStyle: { backgroundColor: '#f5f5f5' } },
  ];

  const fetchData = useCallback(async () => {
    const res = await functionApi.getAll();
    setRowData(res.data);
  }, []);

  useEffect(() => { fetchData(); }, [fetchData]);

  useEffect(() => {
    gridRef.current?.api?.setGridOption('quickFilterText', searchQuery);
  }, [searchQuery]);

  useImperativeHandle(ref, () => ({
    addRow: async () => {
      try {
        const res = await functionApi.create({ functionId: '', functionName: '', action: '', type: '' });
        setRowData(prev => [res.data, ...prev]);
        setTimeout(() => {
          gridRef.current?.api?.ensureIndexVisible(0);
          gridRef.current?.api?.startEditingCell({ rowIndex: 0, colKey: 'aClass' });
        }, 100);
      } catch (e) { console.error(e); }
    },
    deleteSelected: async () => {
      if (selectedRowIds.length === 0) return;
      if (!confirm(`${selectedRowIds.length}개의 행을 삭제하시겠습니까?`)) return;
      try {
        await functionApi.deleteBatch(selectedRowIds);
        setSelectedRows([]);
        fetchData();
      } catch (e) { console.error(e); }
    },
    refresh: fetchData,
  }));

  const onCellValueChanged = async (event: CellValueChangedEvent<FunctionResource>) => {
    const { data } = event;
    if (!data) return;
    try {
      const res = await functionApi.update(data.id, data);
      event.api.applyTransaction({ update: [res.data] });
    } catch {
      event.api.applyTransaction({ update: [{ ...data, [event.colDef.field!]: event.oldValue }] });
    }
  };

  const onRowDragEnd = async (event: RowDragEndEvent) => {
    const orderedIds: number[] = [];
    event.api.forEachNodeAfterFilterAndSort((node) => {
      if (node.data) orderedIds.push(node.data.id);
    });
    await functionApi.reorder(orderedIds);
  };

  const onSelectionChanged = () => {
    const selected = gridRef.current?.api?.getSelectedRows() || [];
    setSelectedRows(selected.map(r => r.id));
  };

  return (
    <div className="ag-theme-alpine" style={{ height: 600, width: '100%' }} data-testid="function-grid">
      <AgGridReact<FunctionResource>
        ref={gridRef}
        rowData={rowData}
        columnDefs={columnDefs}
        getRowId={(params) => String(params.data.id)}
        rowSelection="multiple"
        rowDragManaged={editable}
        rowDragMultiRow={true}
        animateRows={true}
        rowMultiSelectWithClick={true}
        onCellValueChanged={onCellValueChanged}
        onRowDragEnd={onRowDragEnd}
        onSelectionChanged={onSelectionChanged}
      />
    </div>
  );
});

FunctionGrid.displayName = 'FunctionGrid';
export default FunctionGrid;
