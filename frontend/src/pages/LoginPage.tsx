import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { useNavigate, Link } from 'react-router-dom';
import { useAuthStore } from '../stores/useAuthStore';

interface LoginForm {
  email: string;
  password: string;
}

export default function LoginPage() {
  const navigate = useNavigate();
  const login = useAuthStore((s) => s.login);
  const [error, setError] = useState<string | null>(null);

  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<LoginForm>();

  const onSubmit = async (data: LoginForm) => {
    setError(null);
    try {
      await login(data.email, data.password);
      navigate('/resources');
    } catch (err: unknown) {
      const axiosErr = err as { response?: { data?: { message?: string } } };
      setError(axiosErr.response?.data?.message || '로그인에 실패했습니다.');
    }
  };

  return (
    <div data-testid="login-page" style={{ maxWidth: 400, margin: '100px auto', padding: 24 }}>
      <h1>리소스 관리 시스템</h1>
      <h2>로그인</h2>
      <form onSubmit={handleSubmit(onSubmit)} data-testid="login-form">
        <div style={{ marginBottom: 16 }}>
          <label htmlFor="email">이메일</label>
          <input
            id="email"
            type="email"
            data-testid="login-email-input"
            {...register('email', { required: '이메일을 입력해주세요.' })}
            style={{ display: 'block', width: '100%', padding: 8 }}
          />
          {errors.email && <span style={{ color: 'red' }}>{errors.email.message}</span>}
        </div>
        <div style={{ marginBottom: 16 }}>
          <label htmlFor="password">비밀번호</label>
          <input
            id="password"
            type="password"
            data-testid="login-password-input"
            {...register('password', { required: '비밀번호를 입력해주세요.' })}
            style={{ display: 'block', width: '100%', padding: 8 }}
          />
          {errors.password && <span style={{ color: 'red' }}>{errors.password.message}</span>}
        </div>
        {error && <div data-testid="login-error" style={{ color: 'red', marginBottom: 16 }}>{error}</div>}
        <button type="submit" data-testid="login-submit-button" disabled={isSubmitting}
          style={{ width: '100%', padding: 12, cursor: 'pointer' }}>
          {isSubmitting ? '로그인 중...' : '로그인'}
        </button>
      </form>
      <p style={{ marginTop: 16, textAlign: 'center' }}>
        계정이 없으신가요? <Link to="/register">회원가입</Link>
      </p>
    </div>
  );
}
