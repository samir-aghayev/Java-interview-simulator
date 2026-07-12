import { useCallback, useEffect, useState } from 'react';
import { api, ApiError } from '../../api/client';
import { t } from '../../i18n/strings';
import type { AuditLog, Paged } from '../../types';
import Pager from '../Pager';

export default function AuditPane() {
  const [data, setData] = useState<Paged<AuditLog> | null>(null);
  const [error, setError] = useState('');

  const load = useCallback(async (page: number) => {
    setError('');
    try {
      const result = await api<Paged<AuditLog>>(`/api/admin/audit?page=${page}&size=15`);
      setData(result);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : t.loadFailed);
    }
  }, []);

  useEffect(() => {
    load(0);
  }, [load]);

  return (
    <div className="card">
      {error && <div className="error-message">{error}</div>}
      {!data && <p className="muted">{t.loading}</p>}
      {data && data.content.length === 0 && <p className="muted">{t.noAudit}</p>}
      {data?.content.map(l => (
        <div className="admin-row" key={l.id}>
          <div className="admin-row-main">
            <div className="admin-row-title">
              {l.action}
              {l.details ? ` — ${l.details}` : ''}
            </div>
            <div className="admin-row-sub">
              {l.createdAt} · {l.adminEmail}
            </div>
          </div>
        </div>
      ))}
      {data && <Pager data={data} onPage={load} />}
    </div>
  );
}
