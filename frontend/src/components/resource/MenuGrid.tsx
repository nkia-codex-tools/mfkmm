import { useEffect, useState, useCallback, useRef, forwardRef, useImperativeHandle } from 'react';
import { AgGridReact } from 'ag-grid-react';
import type { ColDef, CellValueChangedEvent, RowDragEndEvent } from 'ag-grid-community';
import 'ag-grid-community/styles/ag-grid.css';
import 'ag-grid-community/styles/ag-theme-alpine.css';
import { menuApi } from '../../api/resourceApi';
import { useResourceStore } from '../../stores/useResourceStore';
import { useAuthStore } from '../../stores/useAuthStore';
import type { MenuResource } from '../../types/resource';
import { BOOLEAN_OPTIONS } from '../../types/resource';

export interface GridHandle {
  addRow: () => void;
  deleteSelected: () => void;
  refresh: () => void;
}

const MenuGrid = forwardRef<GridHandle>((_props, ref) => {
  const [rowData, setRowData] = useState<MenuResource[]>([]);
  const gridRef = useRef<AgGridReact>(null);
  const { searchQuery, setSelectedRows, selectedRowIds } = useResourceStore();
  const { hasWritePermission } = useAuthStore();
  const editable = hasWritePermission();

  const columnDefs: ColDef<MenuResource>[] = [
    { rowDrag: editable, width: 40, suppressMenu: true, lockPosition: true, checkboxSelection: true, headerCheckboxSelection: true },
    { field: 'mainMenu', headerName: '메인 메뉴', editable },
    { field: 'subMenuGroup', headerName: '하위 메뉴 그룹', editable },
    { field: 'subMenu', headerName: '하위 메뉴', editable },
    { field: 'menuLevel1', headerName: '메뉴 1레벨', editable },
    { field: 'menuLevel2', headerName: '메뉴 2레벨', editable },
    { field: 'menuLevel3', headerName: '메뉴 3레벨', editable },
    { field: 'menuId', headerName: '메뉴 ID', editable },
    { field: 'isMenu', headerName: '메뉴여부', editable, cellEditor: 'agSelectCellEditor', cellEditorParams: { values: BOOLEAN_OPTIONS }, valueFormatter: p => p.value ? 'TRUE' : 'FALSE' },
    { field: 'isSystemMenu', headerName: 'System 메뉴', editable, cellEditor: 'agSelectCellEditor', cellEditorParams: { values: BOOLEAN_OPTIONS }, valueFormatter: p => p.value ? 'TRUE' : 'FALSE' },
    { field: 'functionId', headerName: '기능(FUNCTION) ID', editable },
    { field: 'functionDescription', headerName: '기능 설명', editable: false, cellStyle: { backgroundColor: '#f5f5f5' } },
    { field: 'menuIcon', headerName: '대메뉴 Icon', editable },
  ];

  const fetchData = useCallback(async () => {
    const res = await menuApi.getAll();
    setRowData(res.data);
  }, []);

  useEffect(() => { fetchData(); }, [fetchData]);

  useEffect(() => {
    gridRef.current?.api?.setGridOption('quickFilterText', searchQuery);
  }, [searchQuery]);

  useImperativeHandle(ref, () => ({
    addRow: async () => {
      try {
        const res = await menuApi.create({ isMenu: true, isSystemMenu: false });
        setRowData(prev => [res.data, ...prev]);
        setTimeout(() => {
          gridRef.current?.api?.ensureIndexVisible(0);
          gridRef.current?.api?.startEditingCell({ rowIndex: 0, colKey: 'mainMenu' });
        }, 100);
      } catch (e) { console.error(e); }
    },
    deleteSelected: async () => {
      if (selectedRowIds.length === 0) return;
      if (!confirm(`${selectedRowIds.length}개의 행을 삭제하시겠습니까?`)) return;
      try {
        await menuApi.deleteBatch(selectedRowIds);
        setSelectedRows([]);
        fetchData();
      } catch (e) { console.error(e); }
    },
    refresh: fetchData,
  }));

  const onCellValueChanged = async (event: CellValueChangedEvent<MenuResource>) => {
    const { data } = event;
    if (!data) return;
    try {
      const res = await menuApi.update(data.id, data);
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
    await menuApi.reorder(orderedIds);
  };

  const onSelectionChanged = () => {
    const selected = gridRef.current?.api?.getSelectedRows() || [];
    setSelectedRows(selected.map(r => r.id));
  };

  return (
    <div className="ag-theme-alpine" style={{ height: 600, width: '100%' }} data-testid="menu-grid">
      <AgGridReact<MenuResource>
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

MenuGrid.displayName = 'MenuGrid';
export default MenuGrid;
