import { useCallback, useEffect, useState } from 'react';
import { api, ApiError } from '../../api/client';
import { t } from '../../i18n/strings';
import type { AdminSession, Paged } from '../../types';
import Pager from '../Pager';

export default function SessionsPane() {
  const [search, setSearch] = useState('');
  const [data, setData] = useState<Paged<AdminSession> | null>(null);
  const [error, setError] = useState('');

  const load = useCallback(async (page: number, searchValue: string) => {
    setError('');
    try {
      const result = await api<Paged<AdminSession>>(
        `/api/admin/sessions?search=${encodeURIComponent(searchValue)}&page=${page}&size=15`
      );
      setData(result);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : t.loadFailed);
    }
  }, []);

  useEffect(() => {
    load(0, '');
  }, [load]);

  return (
    <div className="card">
      <div className="admin-toolbar">
        <input
          type="text"
          value={search}
          placeholder={t.searchSessions}
          onChange={e => setSearch(e.target.value)}
          onKeyDown={e => {
            if (e.key === 'Enter') load(0, search);
          }}
        />
        <button className="btn-secondary" onClick={() => load(0, search)}>
          <i className="fas fa-magnifying-glass" /> {t.searchButton}
        </button>
      </div>
      {error && <div className="error-message">{error}</div>}
      {!data && <p className="muted">{t.loading}</p>}
      {data && data.content.length === 0 && <p className="muted">{t.noSessions}</p>}
      {data?.content.map(s => (
        <div className="admin-row" key={s.id}>
          <div className="admin-row-main">
            <div className="admin-row-title">
              {s.userDisplayName} ({s.userEmail})
            </div>
            <div className="admin-row-sub">
              {s.dateTime} · {t.scoreLabel}: {s.score} · {t.correctLabel}: {s.correctAnswers}/{s.totalQuestions} (
              {s.percent}%)
            </div>
          </div>
        </div>
      ))}
      {data && <Pager data={data} onPage={p => load(p, search)} />}
    </div>
  );
}
