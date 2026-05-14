import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { Link } from 'react-router-dom';
import { useAuthStore } from '../stores/useAuthStore';

interface RegisterForm {
  name: string;
  email: string;
  password: string;
  confirmPassword: string;
}

export default function RegisterPage() {
  const registerUser = useAuthStore((s) => s.register);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);

  const { register, handleSubmit, watch, formState: { errors, isSubmitting } } = useForm<RegisterForm>();
  const password = watch('password');

  const onSubmit = async (data: RegisterForm) => {
    setError(null);
    try {
      await registerUser(data.name, data.email, data.password);
      setSuccess(true);
    } catch (err: unknown) {
      const axiosErr = err as { response?: { data?: { message?: string } } };
      setError(axiosErr.response?.data?.message || '회원가입에 실패했습니다.');
    }
  };

  if (success) {
    return (
      <div data-testid="register-success" style={{ maxWidth: 400, margin: '100px auto', padding: 24 }}>
        <h2>회원가입 완료</h2>
        <p>관리자 승인 후 서비스를 이용할 수 있습니다.</p>
        <Link to="/login">로그인 페이지로 이동</Link>
      </div>
    );
  }

  return (
    <div data-testid="register-page" style={{ maxWidth: 400, margin: '100px auto', padding: 24 }}>
      <h1>회원가입</h1>
      <form onSubmit={handleSubmit(onSubmit)} data-testid="register-form">
        <div style={{ marginBottom: 16 }}>
          <label htmlFor="name">이름</label>
          <input id="name" data-testid="register-name-input"
            {...register('name', { required: '이름을 입력해주세요.', maxLength: { value: 100, message: '100자 이하' } })}
            style={{ display: 'block', width: '100%', padding: 8 }} />
          {errors.name && <span style={{ color: 'red' }}>{errors.name.message}</span>}
        </div>
        <div style={{ marginBottom: 16 }}>
          <label htmlFor="email">이메일</label>
          <input id="email" type="email" data-testid="register-email-input"
            {...register('email', { required: '이메일을 입력해주세요.' })}
            style={{ display: 'block', width: '100%', padding: 8 }} />
          {errors.email && <span style={{ color: 'red' }}>{errors.email.message}</span>}
        </div>
        <div style={{ marginBottom: 16 }}>
          <label htmlFor="password">비밀번호</label>
          <input id="password" type="password" data-testid="register-password-input"
            {...register('password', { required: '비밀번호를 입력해주세요.', minLength: { value: 8, message: '8자 이상 입력해주세요.' } })}
            style={{ display: 'block', width: '100%', padding: 8 }} />
          {errors.password && <span style={{ color: 'red' }}>{errors.password.message}</span>}
        </div>
        <div style={{ marginBottom: 16 }}>
          <label htmlFor="confirmPassword">비밀번호 확인</label>
          <input id="confirmPassword" type="password" data-testid="register-confirm-input"
            {...register('confirmPassword', { required: '비밀번호 확인을 입력해주세요.', validate: v => v === password || '비밀번호가 일치하지 않습니다.' })}
            style={{ display: 'block', width: '100%', padding: 8 }} />
          {errors.confirmPassword && <span style={{ color: 'red' }}>{errors.confirmPassword.message}</span>}
        </div>
        {error && <div data-testid="register-error" style={{ color: 'red', marginBottom: 16 }}>{error}</div>}
        <button type="submit" data-testid="register-submit-button" disabled={isSubmitting}
          style={{ width: '100%', padding: 12, cursor: 'pointer' }}>
          {isSubmitting ? '가입 중...' : '회원가입'}
        </button>
      </form>
      <p style={{ marginTop: 16, textAlign: 'center' }}>
        이미 계정이 있으신가요? <Link to="/login">로그인</Link>
      </p>
    </div>
  );
}
