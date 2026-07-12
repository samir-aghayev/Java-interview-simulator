import { useMemo, useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { t } from '../i18n/strings';
import { ApiError } from '../api/client';

interface PasswordChecks {
  length: boolean;
  caseMix: boolean;
  number: boolean;
  special: boolean;
}

function checkPassword(value: string): PasswordChecks {
  return {
    length: value.length >= 8,
    caseMix: /[a-z]/.test(value) && /[A-Z]/.test(value),
    number: /[0-9]/.test(value),
    special: /[^a-zA-Z0-9]/.test(value)
  };
}

function strengthClass(value: string): string {
  if (value.length === 0) return '';
  const checks = checkPassword(value);
  let strength = 0;
  if (value.length >= 8) strength += 1;
  if (value.length >= 12) strength += 1;
  if (checks.caseMix) strength += 1;
  if (checks.number) strength += 1;
  if (checks.special) strength += 1;
  if (strength <= 2) return 'weak';
  if (strength <= 4) return 'medium';
  return 'strong';
}

function PasswordInput({
  id,
  value,
  placeholder,
  autoComplete,
  onChange,
  onFocus,
  onBlur
}: {
  id: string;
  value: string;
  placeholder: string;
  autoComplete: string;
  onChange: (value: string) => void;
  onFocus?: () => void;
  onBlur?: () => void;
}) {
  const [visible, setVisible] = useState(false);
  return (
    <div className="input-wrapper">
      <input
        type={visible ? 'text' : 'password'}
        id={id}
        value={value}
        placeholder={placeholder}
        autoComplete={autoComplete}
        onChange={e => onChange(e.target.value)}
        onFocus={onFocus}
        onBlur={onBlur}
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
  );
}

export default function AuthPage() {
  const { login, register } = useAuth();
  const navigate = useNavigate();
  const [mode, setMode] = useState<'login' | 'register'>('login');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  // Login state
  const [loginEmail, setLoginEmail] = useState('');
  const [loginPassword, setLoginPassword] = useState('');

  // Register state
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [birthDate, setBirthDate] = useState('');
  const [gender, setGender] = useState('');
  const [country, setCountry] = useState('');
  const [employment, setEmployment] = useState('');
  const [education, setEducation] = useState('');
  const [regEmail, setRegEmail] = useState('');
  const [regPassword, setRegPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showRequirements, setShowRequirements] = useState(false);

  const checks = useMemo(() => checkPassword(regPassword), [regPassword]);
  const strength = strengthClass(regPassword);
  const confirmClass =
    confirmPassword.length === 0 ? '' : confirmPassword === regPassword ? 'match' : 'no-match';

  function switchMode(next: 'login' | 'register') {
    setMode(next);
    setError('');
  }

  async function handleLogin(e: FormEvent) {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await login(loginEmail.trim(), loginPassword);
      navigate('/');
    } catch (err) {
      setError(err instanceof ApiError ? err.message : t.loginFailed);
    } finally {
      setLoading(false);
    }
  }

  async function handleRegister(e: FormEvent) {
    e.preventDefault();
    setError('');
    if (regPassword !== confirmPassword) {
      setError(t.passwordsDontMatch);
      return;
    }
    if (!checks.length || !checks.caseMix || !checks.number || !checks.special) {
      setError(t.passwordWeak);
      return;
    }
    setLoading(true);
    try {
      await register({
        email: regEmail.trim(),
        password: regPassword,
        firstName: firstName.trim(),
        lastName: lastName.trim(),
        birthDate: birthDate || null,
        gender: gender || null,
        country: country.trim() || null,
        employmentStatus: employment || null,
        educationStatus: education || null
      });
      navigate('/');
    } catch (err) {
      setError(err instanceof ApiError ? err.message : t.registerFailed);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="auth-page">
      <div className="container">
        {mode === 'login' ? (
          <div className="form">
            <header>{t.loginTitle}</header>
            <form onSubmit={handleLogin}>
              {error && <div className="error-message">{error}</div>}
              <div className="input-wrapper">
                <input
                  type="email"
                  id="loginEmail"
                  value={loginEmail}
                  placeholder={t.emailPlaceholder}
                  autoComplete="email"
                  required
                  onChange={e => setLoginEmail(e.target.value)}
                />
                <i className="fas fa-envelope input-icon" />
              </div>
              <PasswordInput
                id="loginPassword"
                value={loginPassword}
                placeholder={t.passwordPlaceholder}
                autoComplete="current-password"
                onChange={setLoginPassword}
              />
              <div className="submit-wrapper">
                <button type="submit" className="button" disabled={loading}>
                  {t.loginButton}
                </button>
              </div>
            </form>
            <div className="signup">
              <span>
                {t.noAccount}{' '}
                <a href="#" onClick={e => { e.preventDefault(); switchMode('register'); }}>
                  {t.registerLink}
                </a>
              </span>
            </div>
          </div>
        ) : (
          <div className="form">
            <header>{t.registerTitle}</header>
            <form onSubmit={handleRegister} noValidate>
              {error && <div className="error-message">{error}</div>}

              <div className="input-row">
                <div className="input-wrapper">
                  <input
                    type="text"
                    id="firstName"
                    value={firstName}
                    placeholder={t.firstNamePlaceholder}
                    required
                    onChange={e => setFirstName(e.target.value)}
                  />
                  <i className="fas fa-user input-icon" />
                </div>
                <div className="input-wrapper">
                  <input
                    type="text"
                    id="lastName"
                    value={lastName}
                    placeholder={t.lastNamePlaceholder}
                    required
                    onChange={e => setLastName(e.target.value)}
                  />
                  <i className="fas fa-user-tag input-icon" />
                </div>
              </div>

              <div className="input-wrapper">
                <input
                  type={birthDate ? 'date' : 'text'}
                  id="birthDate"
                  value={birthDate}
                  placeholder={t.birthDatePlaceholder}
                  onFocus={e => (e.target.type = 'date')}
                  onBlur={e => { if (!e.target.value) e.target.type = 'text'; }}
                  onChange={e => setBirthDate(e.target.value)}
                />
                <i className="fas fa-calendar-alt input-icon" />
              </div>

              <div className="input-row">
                <div className="input-wrapper">
                  <select id="gender" value={gender} onChange={e => setGender(e.target.value)}>
                    <option value="">{t.genderPlaceholder}</option>
                    <option value="MALE">{t.genderMale}</option>
                    <option value="FEMALE">{t.genderFemale}</option>
                  </select>
                  <i className="fas fa-venus-mars input-icon" />
                  <i className="fas fa-chevron-down select-arrow" />
                </div>
                <div className="input-wrapper">
                  <input
                    type="text"
                    id="country"
                    value={country}
                    placeholder={t.countryPlaceholder}
                    onChange={e => setCountry(e.target.value)}
                  />
                  <i className="fas fa-globe input-icon" />
                </div>
              </div>

              <div className="input-row">
                <div className="input-wrapper">
                  <select id="employment" value={employment} onChange={e => setEmployment(e.target.value)}>
                    <option value="">{t.employmentPlaceholder}</option>
                    <option value="STUDENT">{t.employmentStudent}</option>
                    <option value="EMPLOYED">{t.employmentEmployed}</option>
                    <option value="UNEMPLOYED">{t.employmentUnemployed}</option>
                    <option value="FREELANCER">{t.employmentFreelancer}</option>
                    <option value="OTHER">{t.employmentOther}</option>
                  </select>
                  <i className="fas fa-briefcase input-icon" />
                  <i className="fas fa-chevron-down select-arrow" />
                </div>
                <div className="input-wrapper">
                  <select id="education" value={education} onChange={e => setEducation(e.target.value)}>
                    <option value="">{t.educationPlaceholder}</option>
                    <option value="HIGH_SCHOOL">{t.educationHighSchool}</option>
                    <option value="BACHELOR">{t.educationBachelor}</option>
                    <option value="MASTER">{t.educationMaster}</option>
                    <option value="PHD">{t.educationPhd}</option>
                    <option value="OTHER">{t.educationOther}</option>
                  </select>
                  <i className="fas fa-graduation-cap input-icon" />
                  <i className="fas fa-chevron-down select-arrow" />
                </div>
              </div>

              <div className="input-wrapper">
                <input
                  type="email"
                  id="registerEmail"
                  value={regEmail}
                  placeholder={t.emailPlaceholder}
                  autoComplete="email"
                  required
                  onChange={e => setRegEmail(e.target.value)}
                />
                <i className="fas fa-envelope input-icon" />
              </div>

              <PasswordInput
                id="registerPassword"
                value={regPassword}
                placeholder={t.passwordSetPlaceholder}
                autoComplete="new-password"
                onChange={setRegPassword}
                onFocus={() => setShowRequirements(true)}
                onBlur={() => { if (!regPassword) setShowRequirements(false); }}
              />

              <div className="password-strength">
                <div className={`password-strength-bar ${strength}`} />
              </div>

              {showRequirements && (
                <div className="password-requirements">
                  <p>{t.passwordRequirementsTitle}</p>
                  <ul>
                    <li className={checks.length ? 'valid' : ''}>
                      <i className={checks.length ? 'fas fa-check' : 'fas fa-circle'} /> {t.reqLength}
                    </li>
                    <li className={checks.caseMix ? 'valid' : ''}>
                      <i className={checks.caseMix ? 'fas fa-check' : 'fas fa-circle'} /> {t.reqCase}
                    </li>
                    <li className={checks.number ? 'valid' : ''}>
                      <i className={checks.number ? 'fas fa-check' : 'fas fa-circle'} /> {t.reqNumber}
                    </li>
                    <li className={checks.special ? 'valid' : ''}>
                      <i className={checks.special ? 'fas fa-check' : 'fas fa-circle'} /> {t.reqSpecial}
                    </li>
                  </ul>
                </div>
              )}

              <div className={`input-wrapper confirm-${confirmClass}`}>
                <input
                  type="password"
                  id="confirmPassword"
                  className={confirmClass}
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
                  {t.registerButton}
                </button>
              </div>
            </form>
            <div className="signup">
              <span>
                {t.haveAccount}{' '}
                <a href="#" onClick={e => { e.preventDefault(); switchMode('login'); }}>
                  {t.loginLink}
                </a>
              </span>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
