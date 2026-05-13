import { useState } from 'react';
import client from '@/shared/api/client';
import { DATAIO } from '@/shared/api/endpoints';
import { useToast } from '@/shared/components/Toast';
import { FileFormat } from '@/types';

export default function ExportPage() {
  const [format, setFormat] = useState<FileFormat>('EXCEL');
  const [exportType, setExportType] = useState<'PARTIAL' | 'ALL'>('ALL');
  const [loading, setLoading] = useState(false);
  const { showToast } = useToast();

  const handleExport = async () => {
    setLoading(true);
    try {
      const url = exportType === 'ALL' ? DATAIO.EXPORT_ALL : DATAIO.EXPORT_PARTIAL;
      const params = exportType === 'ALL' ? { format } : { format };
      const response = await client.post(url, exportType === 'PARTIAL' ? {} : undefined, {
        params,
        responseType: 'blob',
      });

      const contentDisposition = response.headers['content-disposition'];
      const ext = format === 'EXCEL' ? 'xlsx' : format.toLowerCase();
      const fileName = contentDisposition
        ? contentDisposition.split('filename="')[1]?.replace('"', '')
        : `export.${ext}`;

      const blob = new Blob([response.data]);
      const downloadUrl = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = downloadUrl;
      a.download = fileName;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(downloadUrl);

      showToast('success', 'EXPORT 완료');
    } catch {
      showToast('error', 'EXPORT 실패');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <h1 className="text-2xl font-bold mb-6">데이터 EXPORT</h1>
      <div className="max-w-lg space-y-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">내보내기 범위</label>
          <select
            value={exportType}
            onChange={(e) => setExportType(e.target.value as 'PARTIAL' | 'ALL')}
            className="w-full px-3 py-2 border rounded-md"
          >
            <option value="ALL">전체 데이터</option>
            <option value="PARTIAL">검색 결과</option>
          </select>
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">파일 형식</label>
          <select
            value={format}
            onChange={(e) => setFormat(e.target.value as FileFormat)}
            className="w-full px-3 py-2 border rounded-md"
          >
            <option value="EXCEL">Excel (.xlsx)</option>
            <option value="TSV">TSV (.tsv)</option>
            <option value="JSON">JSON (.json)</option>
          </select>
        </div>
        <button
          onClick={handleExport}
          disabled={loading}
          className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50"
        >
          {loading ? '생성 중...' : 'EXPORT 다운로드'}
        </button>
      </div>
    </div>
  );
}
