import { useEffect, useRef, useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { t } from '../i18n/strings';

export default function OAuthCallbackPage() {
  const { loginWithToken } = useAuth();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [error, setError] = useState(false);
  const started = useRef(false);

  useEffect(() => {
    if (started.current) return;
    started.current = true;
    const token = searchParams.get('token');
    if (!token) {
      setError(true);
      return;
    }
    loginWithToken(token)
      .then(() => navigate('/'))
      .catch(() => setError(true));
  }, [searchParams, loginWithToken, navigate]);

  return (
    <div className="auth-page">
      <div className="container">
        <div className="form">
          <header>{t.loginTitle}</header>
          {error ? (
            <>
              <div className="error-message">{t.loginFailed}</div>
              <div className="signup">
                <Link to="/login">{t.backToLogin}</Link>
              </div>
            </>
          ) : (
            <p className="muted">{t.loading}</p>
          )}
        </div>
      </div>
    </div>
  );
}
