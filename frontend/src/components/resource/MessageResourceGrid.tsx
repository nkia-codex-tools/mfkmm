import { useEffect, useState, useCallback, useRef, forwardRef, useImperativeHandle } from 'react';
import { AgGridReact } from 'ag-grid-react';
import type { ColDef, CellValueChangedEvent, RowDragEndEvent } from 'ag-grid-community';
import 'ag-grid-community/styles/ag-grid.css';
import 'ag-grid-community/styles/ag-theme-alpine.css';
import { messageResourceApi } from '../../api/resourceApi';
import { useResourceStore } from '../../stores/useResourceStore';
import { useAuthStore } from '../../stores/useAuthStore';
import type { MessageResourceItem } from '../../types/resource';

export interface GridHandle {
  addRow: () => void;
  deleteSelected: () => void;
  refresh: () => void;
}

const MessageResourceGrid = forwardRef<GridHandle>((_props, ref) => {
  const [rowData, setRowData] = useState<MessageResourceItem[]>([]);
  const gridRef = useRef<AgGridReact>(null);
  const { searchQuery, setSelectedRows, selectedRowIds } = useResourceStore();
  const { hasWritePermission } = useAuthStore();
  const editable = hasWritePermission();

  const readOnlyStyle = { backgroundColor: '#f5f5f5' };

  const columnDefs: ColDef<MessageResourceItem>[] = [
    { rowDrag: editable, width: 40, suppressMenu: true, lockPosition: true, checkboxSelection: true, headerCheckboxSelection: true },
    { field: 'rowNumber', headerName: '번호', editable: false, cellStyle: readOnlyStyle, width: 70 },
    { field: 'duplicateStatus', headerName: '중복/대문자', editable: false, cellStyle: readOnlyStyle, width: 100 },
    { field: 'module', headerName: '모듈', editable },
    { field: 'resourceKey', headerName: 'resource_key', editable },
    { field: 'fullResourceKey', headerName: 'full_resource_key', editable: false, cellStyle: readOnlyStyle },
    { field: 'korean', headerName: '국문', editable },
    { field: 'english', headerName: '영문', editable },
    { field: 'japanese', headerName: '일문', editable },
    { field: 'description', headerName: '설명/사용처', editable },
    { field: 'registeredDate', headerName: '등록/수정일자', editable },
    { field: 'registeredBy', headerName: '등록자', editable },
    { field: 'resourceKeyCount', headerName: '리소스키 개수', editable: false, cellStyle: readOnlyStyle, width: 100 },
    { field: 'koreanCount', headerName: '국문 개수', editable: false, cellStyle: readOnlyStyle, width: 90 },
    { field: 'englishCount', headerName: '영문 개수', editable: false, cellStyle: readOnlyStyle, width: 90 },
    { field: 'japaneseCount', headerName: '일문 개수', editable: false, cellStyle: readOnlyStyle, width: 90 },
  ];

  const fetchData = useCallback(async () => {
    const res = await messageResourceApi.getAll();
    setRowData(res.data);
  }, []);

  useEffect(() => { fetchData(); }, [fetchData]);

  useEffect(() => {
    gridRef.current?.api?.setGridOption('quickFilterText', searchQuery);
  }, [searchQuery]);

  useImperativeHandle(ref, () => ({
    addRow: async () => {
      try {
        await messageResourceApi.create({ module: '', resourceKey: '' });
        await fetchData();
        setTimeout(() => {
          gridRef.current?.api?.ensureIndexVisible(0);
          gridRef.current?.api?.startEditingCell({ rowIndex: 0, colKey: 'module' });
        }, 100);
      } catch (e) { console.error(e); }
    },
    deleteSelected: async () => {
      if (selectedRowIds.length === 0) return;
      if (!confirm(`${selectedRowIds.length}개의 행을 삭제하시겠습니까?`)) return;
      try {
        await messageResourceApi.deleteBatch(selectedRowIds);
        setSelectedRows([]);
        fetchData();
      } catch (e) { console.error(e); }
    },
    refresh: fetchData,
  }));

  const onCellValueChanged = async (event: CellValueChangedEvent<MessageResourceItem>) => {
    const { data } = event;
    if (!data) return;
    try {
      await messageResourceApi.update(data.id, data);
      await fetchData();
    } catch {
      fetchData();
    }
  };

  const onRowDragEnd = async (event: RowDragEndEvent) => {
    const orderedIds: number[] = [];
    event.api.forEachNodeAfterFilterAndSort((node) => {
      if (node.data) orderedIds.push(node.data.id);
    });
    await messageResourceApi.reorder(orderedIds);
  };

  const onSelectionChanged = () => {
    const selected = gridRef.current?.api?.getSelectedRows() || [];
    setSelectedRows(selected.map(r => r.id));
  };

  return (
    <div className="ag-theme-alpine" style={{ height: 600, width: '100%' }} data-testid="message-resource-grid">
      <AgGridReact<MessageResourceItem>
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

MessageResourceGrid.displayName = 'MessageResourceGrid';
export default MessageResourceGrid;
