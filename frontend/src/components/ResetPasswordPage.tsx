import { useMemo, useState, type FormEvent } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { t } from '../i18n/strings';
import { ApiError } from '../api/client';

export default function ResetPasswordPage() {
  const { resetPassword } = useAuth();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const token = useMemo(() => searchParams.get('token') ?? '', [searchParams]);
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [visible, setVisible] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError('');
    if (password !== confirmPassword) {
      setError(t.passwordsDontMatch);
      return;
    }
    if (password.length < 8) {
      setError(t.reqLength);
      return;
    }
    setLoading(true);
    try {
      await resetPassword(token, password);
      navigate('/');
    } catch (err) {
      setError(err instanceof ApiError ? err.message : t.resetPasswordFailed);
    } finally {
      setLoading(false);
    }
  }

  if (!token) {
    return (
      <div className="auth-page">
        <div className="container">
          <div className="form">
            <header>{t.resetPasswordTitle}</header>
            <div className="error-message">{t.resetPasswordMissingToken}</div>
            <div className="signup">
              <Link to="/login">{t.backToLogin}</Link>
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="auth-page">
      <div className="container">
        <div className="form">
          <header>{t.resetPasswordTitle}</header>
          <form onSubmit={handleSubmit}>
            {error && <div className="error-message">{error}</div>}
            <div className="input-wrapper">
              <input
                type={visible ? 'text' : 'password'}
                value={password}
                placeholder={t.passwordSetPlaceholder}
                autoComplete="new-password"
                required
                onChange={e => setPassword(e.target.value)}
              />
              <i className="fas fa-lock input-icon" />
              <button
                type="button"
                className="password-toggle"
                aria-label={visible ? 'Şifrəni gizlət' : 'Şifrəni göstər'}
                onClick={() => setVisible(v => !v)}
              >
                <i className={visible ? 'fas fa-eye' : 'fas fa-eye-slash'} />
              </button>
            </div>
            <div className="input-wrapper">
              <input
                type={visible ? 'text' : 'password'}
                value={confirmPassword}
                placeholder={t.passwordConfirmPlaceholder}
                autoComplete="new-password"
                required
                onChange={e => setConfirmPassword(e.target.value)}
              />
              <i className="fas fa-lock input-icon" />
            </div>
            <div className="submit-wrapper">
              <button type="submit" className="button" disabled={loading}>
                {t.resetPasswordButton}
              </button>
            </div>
          </form>
          <div className="signup">
            <Link to="/login">{t.backToLogin}</Link>
          </div>
        </div>
      </div>
    </div>
  );
}
