import { useState } from 'react';
import client from '@/shared/api/client';
import { DATAIO } from '@/shared/api/endpoints';
import { useToast } from '@/shared/components/Toast';
import { ConflictPolicy, ImportResult } from '@/types';

const MAX_FILE_SIZE = 5 * 1024 * 1024;
const ALLOWED_EXTENSIONS = ['.xlsx', '.tsv', '.json'];

export default function ImportPage() {
  const [file, setFile] = useState<File | null>(null);
  const [conflictPolicy, setConflictPolicy] = useState<ConflictPolicy>('SKIP');
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState<ImportResult | null>(null);
  const [fileError, setFileError] = useState('');
  const { showToast } = useToast();

  const validateFile = (f: File): string | null => {
    const ext = '.' + f.name.split('.').pop()?.toLowerCase();
    if (!ALLOWED_EXTENSIONS.includes(ext)) return '지원하지 않는 파일 형식입니다 (.xlsx, .tsv, .json만 가능)';
    if (f.size > MAX_FILE_SIZE) return '파일 크기가 5MB를 초과합니다';
    if (f.size === 0) return '빈 파일입니다';
    return null;
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const selected = e.target.files?.[0] || null;
    setFile(selected);
    setFileError('');
    setResult(null);
    if (selected) {
      const error = validateFile(selected);
      if (error) setFileError(error);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!file || fileError) return;

    setLoading(true);
    setResult(null);
    try {
      const formData = new FormData();
      formData.append('file', file);
      formData.append('conflictPolicy', conflictPolicy);
      const response = await client.post(DATAIO.IMPORT, formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });
      setResult(response.data);
      showToast('success', 'IMPORT 완료');
    } catch {
      showToast('error', 'IMPORT 실패');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <h1 className="text-2xl font-bold mb-6">파일 IMPORT</h1>
      <form onSubmit={handleSubmit} className="max-w-lg space-y-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">파일 선택</label>
          <input
            type="file"
            accept=".xlsx,.tsv,.json"
            onChange={handleFileChange}
            className="block w-full text-sm border rounded-md p-2"
          />
          {fileError && <p className="text-red-600 text-sm mt-1">{fileError}</p>}
          {file && !fileError && (
            <p className="text-gray-500 text-sm mt-1">{file.name} ({(file.size / 1024).toFixed(1)} KB)</p>
          )}
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">충돌 정책</label>
          <select
            value={conflictPolicy}
            onChange={(e) => setConflictPolicy(e.target.value as ConflictPolicy)}
            className="w-full px-3 py-2 border rounded-md"
          >
            <option value="SKIP">건너뛰기 (기존 유지)</option>
            <option value="OVERWRITE">덮어쓰기 (최신값 반영)</option>
          </select>
        </div>
        <button
          type="submit"
          disabled={!file || !!fileError || loading}
          className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50"
        >
          {loading ? '처리 중...' : 'IMPORT 실행'}
        </button>
      </form>

      {result && (
        <div className="mt-6 bg-white border rounded-lg p-4">
          <h2 className="text-lg font-semibold mb-3">IMPORT 결과</h2>
          <div className="grid grid-cols-4 gap-4 mb-4">
            <div className="text-center p-3 bg-gray-50 rounded">
              <p className="text-2xl font-bold">{result.totalRows}</p>
              <p className="text-sm text-gray-500">전체</p>
            </div>
            <div className="text-center p-3 bg-green-50 rounded">
              <p className="text-2xl font-bold text-green-600">{result.successCount}</p>
              <p className="text-sm text-gray-500">성공</p>
            </div>
            <div className="text-center p-3 bg-red-50 rounded">
              <p className="text-2xl font-bold text-red-600">{result.failedCount}</p>
              <p className="text-sm text-gray-500">실패</p>
            </div>
            <div className="text-center p-3 bg-yellow-50 rounded">
              <p className="text-2xl font-bold text-yellow-600">{result.skippedCount}</p>
              <p className="text-sm text-gray-500">건너뜀</p>
            </div>
          </div>
          {result.errors.length > 0 && (
            <div>
              <h3 className="font-medium mb-2">오류 상세</h3>
              <table className="w-full text-sm border">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-3 py-2 text-left">행</th>
                    <th className="px-3 py-2 text-left">필드</th>
                    <th className="px-3 py-2 text-left">사유</th>
                  </tr>
                </thead>
                <tbody className="divide-y">
                  {result.errors.slice(0, 20).map((err, i) => (
                    <tr key={i}>
                      <td className="px-3 py-2">{err.rowNumber}</td>
                      <td className="px-3 py-2">{err.field}</td>
                      <td className="px-3 py-2">{err.reason}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
              {result.errors.length > 20 && (
                <p className="text-sm text-gray-500 mt-2">외 {result.errors.length - 20}건...</p>
              )}
            </div>
          )}
        </div>
      )}
    </div>
  );
}
