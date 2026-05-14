import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { useEffect } from 'react';
import { useAuthStore } from './stores/useAuthStore';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import AdminPage from './pages/AdminPage';
import ResourcePage from './pages/ResourcePage';
import HistoryPage from './pages/HistoryPage';
import VersionPage from './pages/VersionPage';
import AppLayout from './components/common/AppLayout';
import ProtectedRoute from './components/common/ProtectedRoute';


export default function App() {
  const loadFromStorage = useAuthStore((s) => s.loadFromStorage);

  useEffect(() => { loadFromStorage(); }, [loadFromStorage]);

  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route element={
          <ProtectedRoute requiredRole="READER">
            <AppLayout />
          </ProtectedRoute>
        }>
          <Route path="/resources" element={<ResourcePage />} />
          <Route path="/history" element={<HistoryPage />} />
          <Route path="/versions" element={
            <ProtectedRoute requiredRole="ADMIN"><VersionPage /></ProtectedRoute>
          } />
          <Route path="/admin" element={
            <ProtectedRoute requiredRole="ADMIN"><AdminPage /></ProtectedRoute>
          } />
        </Route>
        <Route path="*" element={<Navigate to="/resources" replace />} />
      </Routes>
    </BrowserRouter>
  );
}
