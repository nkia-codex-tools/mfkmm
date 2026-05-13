import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import client from '@/shared/api/client';
import { RESOURCES } from '@/shared/api/endpoints';
import { Resource } from '@/types';

export default function ResourceDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [resource, setResource] = useState<Resource | null>(null);
  const [editing, setEditing] = useState(false);
  const [form, setForm] = useState({ content: '', description: '' });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetch = async () => {
      try {
        const res = await client.get(`${RESOURCES.BASE}/${id}`);
        setResource(res.data);
        setForm({ content: res.data.content, description: res.data.description || '' });
      } catch {
        setResource(null);
      } finally {
        setLoading(false);
      }
    };
    fetch();
  }, [id]);

  const handleUpdate = async () => {
    try {
      const res = await client.put(`${RESOURCES.BASE}/${id}`, form);
      setResource(res.data);
      setEditing(false);
    } catch (err: any) {
      alert(err.response?.data?.message || '수정 실패');
    }
  };

  const handleDelete = async () => {
    if (!confirm('이 리소스를 삭제하시겠습니까?')) return;
    try {
      await client.delete(`${RESOURCES.BASE}/${id}`);
      navigate('/resources');
    } catch (err: any) {
      alert(err.response?.data?.message || '삭제 실패');
    }
  };

  if (loading) return <div className="p-6">로딩 중...</div>;
  if (!resource) return <div className="p-6 text-red-500">리소스를 찾을 수 없습니다.</div>;

  return (
    <div className="max-w-2xl">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold">리소스 상세</h1>
        <div className="space-x-2">
          {!editing && (
            <>
              <button onClick={() => setEditing(true)} className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700" data-testid="edit-button">수정</button>
              <button onClick={handleDelete} className="px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700" data-testid="delete-button">삭제</button>
            </>
          )}
          <button onClick={() => navigate('/resources')} className="px-4 py-2 border rounded-md hover:bg-gray-50">목록</button>
        </div>
      </div>

      <div className="bg-white border rounded-lg p-6 space-y-4">
        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-500">리소스 키</label>
            <p className="mt-1 text-lg font-mono">{resource.resourceKey}</p>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-500">유형</label>
            <p className="mt-1">{resource.resourceType}</p>
          </div>
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-500">내용</label>
          {editing ? (
            <input type="text" value={form.content} onChange={e => setForm({...form, content: e.target.value})} className="mt-1 w-full px-3 py-2 border rounded-md" data-testid="edit-content" />
          ) : (
            <p className="mt-1 text-lg">{resource.content}</p>
          )}
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-500">설명</label>
          {editing ? (
            <textarea value={form.description} onChange={e => setForm({...form, description: e.target.value})} className="mt-1 w-full px-3 py-2 border rounded-md" rows={3} data-testid="edit-description" />
          ) : (
            <p className="mt-1 text-gray-700">{resource.description || '-'}</p>
          )}
        </div>

        {editing && (
          <div className="flex gap-2 pt-2">
            <button onClick={handleUpdate} className="px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700" data-testid="save-button">저장</button>
            <button onClick={() => setEditing(false)} className="px-4 py-2 border rounded-md hover:bg-gray-50">취소</button>
          </div>
        )}

        <div className="grid grid-cols-2 gap-4 pt-4 border-t text-sm text-gray-500">
          <div>
            <span className="font-medium">등록자:</span> {resource.createdBy}
          </div>
          <div>
            <span className="font-medium">등록일:</span> {new Date(resource.createdAt).toLocaleString()}
          </div>
          <div>
            <span className="font-medium">수정자:</span> {resource.updatedBy}
          </div>
          <div>
            <span className="font-medium">수정일:</span> {new Date(resource.updatedAt).toLocaleString()}
          </div>
        </div>
      </div>
    </div>
  );
}
