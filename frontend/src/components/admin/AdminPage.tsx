import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { t } from '../../i18n/strings';
import QuestionsPane from './QuestionsPane';
import UsersPane from './UsersPane';
import ReportsPane from './ReportsPane';
import AuditPane from './AuditPane';
import SessionsPane from './SessionsPane';

type Pane = 'questions' | 'users' | 'reports' | 'audit' | 'sessions';

const PANES: { key: Pane; label: string }[] = [
  { key: 'questions', label: t.adminTabQuestions },
  { key: 'users', label: t.adminTabUsers },
  { key: 'reports', label: t.adminTabReports },
  { key: 'audit', label: t.adminTabAudit },
  { key: 'sessions', label: t.adminTabSessions }
];

export default function AdminPage() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [pane, setPane] = useState<Pane>('questions');

  function handleLogout() {
    logout();
    navigate('/login');
  }

  return (
    <div className="app-shell">
      <header className="topbar">
        <div className="brand">
          <i className="fas fa-shield-halved" /> {t.adminTitle}
        </div>
        <div className="user-bar">
          <Link to="/" className="admin-link">
            {t.backToApp}
          </Link>
          <span className="user-name">
            <i className="fas fa-user" /> {user?.displayName}
          </span>
          <button className="btn-secondary" onClick={handleLogout}>
            <i className="fas fa-right-from-bracket" /> {t.logout}
          </button>
        </div>
      </header>

      <nav className="tabs">
        {PANES.map(p => (
          <button
            key={p.key}
            className={`tab ${pane === p.key ? 'active' : ''}`}
            onClick={() => setPane(p.key)}
          >
            {p.label}
          </button>
        ))}
      </nav>

      <main className="content wide">
        {pane === 'questions' && <QuestionsPane />}
        {pane === 'users' && <UsersPane />}
        {pane === 'reports' && <ReportsPane />}
        {pane === 'audit' && <AuditPane />}
        {pane === 'sessions' && <SessionsPane />}
      </main>
    </div>
  );
}
