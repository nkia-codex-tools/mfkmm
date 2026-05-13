import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import client from '@/shared/api/client';
import { RESOURCES } from '@/shared/api/endpoints';
import { ResourceType, SimilarityResult } from '@/types';

export default function ResourceCreatePage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    resourceKey: '',
    resourceType: 'MESSAGE_KEY' as ResourceType,
    content: '',
    description: '',
  });
  const [similarResults, setSimilarResults] = useState<SimilarityResult[] | null>(null);
  const [duplicate, setDuplicate] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [reason, setReason] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setDuplicate(null);
    setSimilarResults(null);

    try {
      const response = await client.post(RESOURCES.CREATE, form);
      const data = response.data;

      if (data.isDuplicate) {
        setDuplicate(`중복된 리소스가 존재합니다: ${data.existingResource?.resourceKey}`);
      } else if (Array.isArray(data) && data.length > 0) {
        setSimilarResults(data);
      } else {
        navigate('/resources');
      }
    } catch (err: any) {
      if (err.response?.status === 409) {
        setDuplicate(err.response.data.message || '중복된 리소스입니다.');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleConfirmChoice = async (action: 'USE_EXISTING' | 'CREATE_NEW', chosenResourceId?: string) => {
    setLoading(true);
    try {
      await client.post(RESOURCES.CONFIRM_CHOICE, {
        ...form,
        chosenAction: action,
        chosenResourceId: chosenResourceId || '',
        reason,
      });
      navigate('/resources');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-2xl">
      <h1 className="text-2xl font-bold mb-6">리소스 등록</h1>

      {duplicate && (
        <div className="mb-4 p-4 bg-red-50 border border-red-200 rounded-md text-red-700">
          {duplicate}
        </div>
      )}

      {similarResults ? (
        <div className="space-y-4">
          <div className="p-4 bg-yellow-50 border border-yellow-200 rounded-md">
            <h2 className="font-semibold text-yellow-800 mb-2">유사한 리소스가 발견되었습니다</h2>
            <ul className="space-y-2">
              {similarResults.map((sr, idx) => (
                <li key={idx} className="flex justify-between items-center p-2 bg-white rounded border">
                  <span className="text-sm">
                    <strong>{sr.resource.resourceKey}</strong> — {sr.resource.content}
                    <span className="ml-2 text-gray-500">(거리: {sr.distance})</span>
                  </span>
                  <button
                    onClick={() => handleConfirmChoice('USE_EXISTING', sr.resource.id)}
                    className="px-3 py-1 text-sm bg-green-600 text-white rounded hover:bg-green-700"
                    data-testid={`use-existing-${idx}`}
                  >
                    사용
                  </button>
                </li>
              ))}
            </ul>
          </div>
          <div className="space-y-2">
            <textarea
              placeholder="신규 등록 사유 (선택)"
              value={reason}
              onChange={(e) => setReason(e.target.value)}
              className="w-full px-3 py-2 border rounded-md"
              rows={2}
            />
            <button
              onClick={() => handleConfirmChoice('CREATE_NEW')}
              disabled={loading}
              className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50"
              data-testid="create-new-button"
            >
              신규 등록 강행
            </button>
          </div>
        </div>
      ) : (
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium mb-1">리소스 키</label>
            <input
              type="text"
              value={form.resourceKey}
              onChange={(e) => setForm({ ...form, resourceKey: e.target.value })}
              required
              className="w-full px-3 py-2 border rounded-md"
              placeholder="msg.hello.world"
              data-testid="resource-key-input"
            />
          </div>
          <div>
            <label className="block text-sm font-medium mb-1">유형</label>
            <select
              value={form.resourceType}
              onChange={(e) => setForm({ ...form, resourceType: e.target.value as ResourceType })}
              className="w-full px-3 py-2 border rounded-md"
              data-testid="resource-type-select"
            >
              <option value="MESSAGE_KEY">메시지 키</option>
              <option value="FUNCTION_ID">기능 ID</option>
              <option value="MENU_ID">메뉴 ID</option>
            </select>
          </div>
          <div>
            <label className="block text-sm font-medium mb-1">내용</label>
            <input
              type="text"
              value={form.content}
              onChange={(e) => setForm({ ...form, content: e.target.value })}
              required
              className="w-full px-3 py-2 border rounded-md"
              placeholder="안녕하세요"
              data-testid="resource-content-input"
            />
          </div>
          <div>
            <label className="block text-sm font-medium mb-1">설명 (선택)</label>
            <textarea
              value={form.description}
              onChange={(e) => setForm({ ...form, description: e.target.value })}
              className="w-full px-3 py-2 border rounded-md"
              rows={3}
              placeholder="리소스 설명..."
              data-testid="resource-description-input"
            />
          </div>
          <div className="flex gap-3">
            <button
              type="submit"
              disabled={loading}
              className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50"
              data-testid="resource-submit-button"
            >
              {loading ? '처리 중...' : '등록'}
            </button>
            <button
              type="button"
              onClick={() => navigate('/resources')}
              className="px-4 py-2 border rounded-md hover:bg-gray-50"
            >
              취소
            </button>
          </div>
        </form>
      )}
    </div>
  );
}
