import { useCallback, useEffect, useState } from 'react';
import { api, ApiError } from '../../api/client';
import { t } from '../../i18n/strings';
import type { Paged, Report } from '../../types';
import Pager from '../Pager';

const STATUS_BADGES: Record<string, string> = {
  OPEN: 'admin',
  RESOLVED: 'active-q',
  DISMISSED: 'inactive'
};

export default function ReportsPane() {
  const [status, setStatus] = useState('OPEN');
  const [data, setData] = useState<Paged<Report> | null>(null);
  const [error, setError] = useState('');

  const load = useCallback(async (page: number, statusValue: string) => {
    setError('');
    try {
      const result = await api<Paged<Report>>(
        `/api/admin/reports?status=${encodeURIComponent(statusValue)}&page=${page}&size=10`
      );
      setData(result);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : t.loadFailed);
    }
  }, []);

  useEffect(() => {
    load(0, 'OPEN');
  }, [load]);

  async function resolve(id: string, resolution: 'RESOLVED' | 'DISMISSED') {
    setError('');
    try {
      await api(`/api/admin/reports/${id}`, {
        method: 'PATCH',
        body: JSON.stringify({ status: resolution })
      });
      load(data?.page ?? 0, status);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : t.saveFailed);
    }
  }

  return (
    <div className="card">
      <div className="admin-toolbar">
        <select
          value={status}
          onChange={e => {
            setStatus(e.target.value);
            load(0, e.target.value);
          }}
        >
          <option value="">{t.statusFilterAll}</option>
          <option value="OPEN">OPEN</option>
          <option value="RESOLVED">RESOLVED</option>
          <option value="DISMISSED">DISMISSED</option>
        </select>
      </div>
      {error && <div className="error-message">{error}</div>}
      {!data && <p className="muted">{t.loading}</p>}
      {data && data.content.length === 0 && <p className="muted">{t.noReports}</p>}
      {data?.content.map(r => (
        <div className="admin-row" key={r.id}>
          <div className="admin-row-main">
            <div className="admin-row-title">
              <span className={`badge ${STATUS_BADGES[r.status] ?? 'user'}`}>{r.status}</span>{' '}
              {r.message}
            </div>
            <div className="admin-row-sub">
              {t.reportedQuestion}: {r.questionText}
            </div>
            <div className="admin-row-sub">
              {t.reporterLabel}: {r.reporterEmail} · {r.createdAt}
            </div>
          </div>
          <div className="admin-row-actions">
            {r.status === 'OPEN' && (
              <>
                <button className="btn-small" onClick={() => resolve(r.id, 'RESOLVED')}>
                  <i className="fas fa-check" /> {t.resolveButton}
                </button>
                <button className="btn-small danger" onClick={() => resolve(r.id, 'DISMISSED')}>
                  <i className="fas fa-xmark" /> {t.dismissButton}
                </button>
              </>
            )}
          </div>
        </div>
      ))}
      {data && <Pager data={data} onPage={p => load(p, status)} />}
    </div>
  );
}
