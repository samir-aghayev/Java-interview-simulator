import { useCallback, useEffect, useState } from 'react';
import { api, ApiError } from '../../api/client';
import { useAuth } from '../../context/AuthContext';
import { t } from '../../i18n/strings';
import type { Paged, UserSummary } from '../../types';
import Pager from '../Pager';

export default function UsersPane() {
  const { user: me } = useAuth();
  const [search, setSearch] = useState('');
  const [data, setData] = useState<Paged<UserSummary> | null>(null);
  const [error, setError] = useState('');

  const load = useCallback(
    async (page: number, searchValue: string) => {
      setError('');
      try {
        const result = await api<Paged<UserSummary>>(
          `/api/admin/users?search=${encodeURIComponent(searchValue)}&page=${page}&size=10`
        );
        setData(result);
      } catch (err) {
        setError(err instanceof ApiError ? err.message : t.loadFailed);
      }
    },
    []
  );

  useEffect(() => {
    load(0, '');
  }, [load]);

  async function toggleRole(target: UserSummary) {
    setError('');
    const newRole = target.role === 'ADMIN' ? 'USER' : 'ADMIN';
    try {
      await api(`/api/admin/users/${target.id}/role`, {
        method: 'PATCH',
        body: JSON.stringify({ role: newRole })
      });
      load(data?.page ?? 0, search);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : t.roleChangeFailed);
    }
  }

  function profileLine(u: UserSummary): string {
    const parts = [
      `${t.registeredAt}: ${u.createdAt}`,
      u.birthDate,
      u.gender,
      u.country,
      u.employmentStatus,
      u.educationStatus
    ].filter(Boolean);
    return parts.join(' · ');
  }

  return (
    <div className="card">
      <div className="admin-toolbar">
        <input
          type="text"
          value={search}
          placeholder={t.searchUsers}
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
      {data && data.content.length === 0 && <p className="muted">{t.noResults}</p>}
      {data?.content.map(u => (
        <div className="admin-row" key={u.id}>
          <div className="admin-row-main">
            <div className="admin-row-title">
              {u.displayName} ({u.email}){' '}
              <span className={`badge ${u.role === 'ADMIN' ? 'admin' : 'user'}`}>{u.role}</span>
            </div>
            <div className="admin-row-sub">{profileLine(u)}</div>
          </div>
          <div className="admin-row-actions">
            {me && u.email !== me.email && (
              <button className="btn-small" onClick={() => toggleRole(u)}>
                {u.role === 'ADMIN' ? t.makeUser : t.makeAdmin}
              </button>
            )}
          </div>
        </div>
      ))}
      {data && <Pager data={data} onPage={p => load(p, search)} />}
    </div>
  );
}
