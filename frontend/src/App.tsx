import { lazy, Suspense } from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from '@/features/auth/AuthContext';
import { ToastProvider } from '@/shared/components/Toast';
import ProtectedRoute from '@/features/auth/ProtectedRoute';
import Layout from '@/shared/components/Layout';
import LoginPage from '@/features/auth/LoginPage';

const ResourceListPage = lazy(() => import('@/features/resource/ResourceListPage'));
const ResourceCreatePage = lazy(() => import('@/features/resource/ResourceCreatePage'));
const ResourceDetailPage = lazy(() => import('@/features/resource/ResourceDetailPage'));
const ImportPage = lazy(() => import('@/features/dataio/ImportPage'));
const ExportPage = lazy(() => import('@/features/dataio/ExportPage'));
const DeployPage = lazy(() => import('@/features/admin/DeployPage'));
const UserManagementPage = lazy(() => import('@/features/admin/UserManagementPage'));
const HistoryPage = lazy(() => import('@/features/admin/HistoryPage'));
const LoginHistoryPage = lazy(() => import('@/features/admin/LoginHistoryPage'));

function AppRoutes() {
  return (
    <Suspense fallback={<div className="p-6">로딩 중...</div>}>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/resources" element={
          <ProtectedRoute><Layout><ResourceListPage /></Layout></ProtectedRoute>
        } />
        <Route path="/resources/new" element={
          <ProtectedRoute requiredRole="WRITE"><Layout><ResourceCreatePage /></Layout></ProtectedRoute>
        } />
        <Route path="/resources/:id" element={
          <ProtectedRoute><Layout><ResourceDetailPage /></Layout></ProtectedRoute>
        } />
        <Route path="/import" element={
          <ProtectedRoute requiredRole="WRITE"><Layout><ImportPage /></Layout></ProtectedRoute>
        } />
        <Route path="/export" element={
          <ProtectedRoute><Layout><ExportPage /></Layout></ProtectedRoute>
        } />
        <Route path="/admin/users" element={
          <ProtectedRoute requiredRole="ADMIN"><Layout><UserManagementPage /></Layout></ProtectedRoute>
        } />
        <Route path="/admin/history" element={
          <ProtectedRoute requiredRole="ADMIN"><Layout><HistoryPage /></Layout></ProtectedRoute>
        } />
        <Route path="/admin/login-history" element={
          <ProtectedRoute requiredRole="ADMIN"><Layout><LoginHistoryPage /></Layout></ProtectedRoute>
        } />
        <Route path="/admin/deploy" element={
          <ProtectedRoute requiredRole="ADMIN"><Layout><DeployPage /></Layout></ProtectedRoute>
        } />
        <Route path="/" element={<Navigate to="/resources" replace />} />
      </Routes>
    </Suspense>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <ToastProvider>
        <AppRoutes />
      </ToastProvider>
    </AuthProvider>
  );
}
