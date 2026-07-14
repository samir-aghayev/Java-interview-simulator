import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { t } from '../i18n/strings';
import QuizView, { type Phase } from './QuizView';
import WeakTopics from './WeakTopics';
import Progress from './Progress';
import Leaderboard from './Leaderboard';

type Tab = 'quiz' | 'weak' | 'progress' | 'leaderboard';

export default function MainPage() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [tab, setTab] = useState<Tab>('quiz');
  const [quizRunning, setQuizRunning] = useState(false);

  function confirmLeave(): boolean {
    return !quizRunning || window.confirm(t.confirmLeaveQuiz);
  }

  function switchTab(next: Tab) {
    if (next === tab) return;
    if (!confirmLeave()) return;
    setTab(next);
  }

  function handleLogout() {
    if (!confirmLeave()) return;
    logout();
    navigate('/login');
  }

  function handleAdminLinkClick(e: React.MouseEvent) {
    if (!confirmLeave()) e.preventDefault();
  }

  function handlePhaseChange(phase: Phase) {
    setQuizRunning(phase === 'running');
  }

  return (
    <div className="app-shell">
      <header className="topbar">
        <div className="brand">{t.appName}</div>
        <div className="user-bar">
          {user?.role === 'ADMIN' && (
            <Link to="/admin" className="admin-link" onClick={handleAdminLinkClick}>
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
        <button className={`tab ${tab === 'quiz' ? 'active' : ''}`} onClick={() => switchTab('quiz')}>
          {t.tabQuiz}
        </button>
        <button className={`tab ${tab === 'weak' ? 'active' : ''}`} onClick={() => switchTab('weak')}>
          {t.tabWeak}
        </button>
        <button
          className={`tab ${tab === 'progress' ? 'active' : ''}`}
          onClick={() => switchTab('progress')}
        >
          {t.tabProgress}
        </button>
        <button
          className={`tab ${tab === 'leaderboard' ? 'active' : ''}`}
          onClick={() => switchTab('leaderboard')}
        >
          {t.tabLeaderboard}
        </button>
      </nav>

      <main className="content">
        {tab === 'quiz' && <QuizView onPhaseChange={handlePhaseChange} />}
        {tab === 'weak' && <WeakTopics />}
        {tab === 'progress' && <Progress />}
        {tab === 'leaderboard' && <Leaderboard />}
      </main>
    </div>
  );
}
