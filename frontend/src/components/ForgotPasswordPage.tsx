import { useState, type FormEvent } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { t } from '../i18n/strings';

export default function ForgotPasswordPage() {
  const { forgotPassword } = useAuth();
  const [email, setEmail] = useState('');
  const [sent, setSent] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await forgotPassword(email.trim());
      setSent(true);
    } catch {
      setError(t.forgotPasswordFailed);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="auth-page">
      <div className="container">
        <div className="form">
          <header>{t.forgotPasswordTitle}</header>
          {sent ? (
            <p className="muted">{t.forgotPasswordSent}</p>
          ) : (
            <form onSubmit={handleSubmit}>
              {error && <div className="error-message">{error}</div>}
              <p className="muted">{t.forgotPasswordHint}</p>
              <div className="input-wrapper">
                <input
                  type="email"
                  value={email}
                  placeholder={t.emailPlaceholder}
                  autoComplete="email"
                  required
                  onChange={e => setEmail(e.target.value)}
                />
                <i className="fas fa-envelope input-icon" />
              </div>
              <div className="submit-wrapper">
                <button type="submit" className="button" disabled={loading}>
                  {t.forgotPasswordSend}
                </button>
              </div>
            </form>
          )}
          <div className="signup">
            <Link to="/login">{t.backToLogin}</Link>
          </div>
        </div>
      </div>
    </div>
  );
}
