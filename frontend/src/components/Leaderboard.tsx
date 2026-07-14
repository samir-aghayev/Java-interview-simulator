import { useEffect, useState } from 'react';
import { api } from '../api/client';
import { t } from '../i18n/strings';
import type { LeaderboardResponse } from '../types';

export default function Leaderboard() {
  const [data, setData] = useState<LeaderboardResponse | null>(null);
  const [failed, setFailed] = useState(false);

  useEffect(() => {
    api<LeaderboardResponse>('/api/stats/leaderboard')
      .then(setData)
      .catch(() => setFailed(true));
  }, []);

  return (
    <div className="card">
      <h2>{t.leaderboardTitle}</h2>
      <p className="muted">{t.leaderboardHint}</p>
      {failed && <p className="error-message">{t.loadFailed}</p>}
      {!data && !failed && <p className="muted">{t.loading}</p>}
      {data && data.entries.length === 0 && <p className="muted">{t.noLeaderboardData}</p>}
      {data && data.entries.length > 0 && (
        <table>
          <thead>
            <tr>
              <th>{t.rankColumn}</th>
              <th>{t.nameColumn}</th>
              <th>{t.sessionsColumn}</th>
              <th>{t.scoreColumn}</th>
              <th>{t.percentColumn}</th>
            </tr>
          </thead>
          <tbody>
            {data.entries.map(entry => (
              <tr key={entry.rank} className={entry.isCurrentUser ? 'leaderboard-you' : ''}>
                <td>{entry.rank}</td>
                <td>
                  {entry.displayName}
                  {entry.isCurrentUser && <span className="you-badge"> ({t.youBadge})</span>}
                </td>
                <td>{entry.sessionsCount}</td>
                <td>{entry.totalScore}</td>
                <td>{entry.averagePercent}%</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
