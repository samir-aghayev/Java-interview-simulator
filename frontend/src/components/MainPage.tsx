import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { t } from '../i18n/strings';
import QuizView from './QuizView';
import WeakTopics from './WeakTopics';
import Progress from './Progress';

type Tab = 'quiz' | 'weak' | 'progress';

export default function MainPage() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [tab, setTab] = useState<Tab>('quiz');

  function handleLogout() {
    logout();
    navigate('/login');
  }

  return (
    <div className="app-shell">
      <header className="topbar">
        <div className="brand">{t.appName}</div>
        <div className="user-bar">
          {user?.role === 'ADMIN' && (
            <Link to="/admin" className="admin-link">
              <i className="fas fa-shield-halved" /> {t.adminPanelLink}
            </Link>
          )}
          <span className="user-name">
            <i className="fas fa-user" /> {user?.displayName}
          </span>
          <button className="btn-secondary" onClick={handleLogout}>
            <i className="fas fa-right-from-bracket" /> {t.logout}
          </button>
        </div>
      </header>

      <nav className="tabs">
        <button className={`tab ${tab === 'quiz' ? 'active' : ''}`} onClick={() => setTab('quiz')}>
          {t.tabQuiz}
        </button>
        <button className={`tab ${tab === 'weak' ? 'active' : ''}`} onClick={() => setTab('weak')}>
          {t.tabWeak}
        </button>
        <button
          className={`tab ${tab === 'progress' ? 'active' : ''}`}
          onClick={() => setTab('progress')}
        >
          {t.tabProgress}
        </button>
      </nav>

      <main className="content">
        {tab === 'quiz' && <QuizView />}
        {tab === 'weak' && <WeakTopics />}
        {tab === 'progress' && <Progress />}
      </main>
    </div>
  );
}
