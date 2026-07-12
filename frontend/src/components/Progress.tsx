import { useEffect, useState } from 'react';
import { api } from '../api/client';
import { t } from '../i18n/strings';
import type { ProgressResponse } from '../types';

export default function Progress() {
  const [data, setData] = useState<ProgressResponse | null>(null);
  const [failed, setFailed] = useState(false);

  useEffect(() => {
    api<ProgressResponse>('/api/stats/progress')
      .then(setData)
      .catch(() => setFailed(true));
  }, []);

  return (
    <div className="card">
      <h2>{t.progressTitle}</h2>
      {failed && <p className="error-message">{t.loadFailed}</p>}
      {!data && !failed && <p className="muted">{t.loading}</p>}
      {data && data.sessions.length === 0 && <p className="muted">{t.noHistory}</p>}
      {data && data.sessions.length > 0 && (
        <>
          <div className="score-summary">
            <div className="stat-tile">
              <div className="value">{data.averagePercent}%</div>
              <div className="label">{t.averagePercent}</div>
            </div>
            {typeof data.improvementPercent === 'number' && (
              <div className="stat-tile">
                <div className="value">
                  {data.improvementPercent >= 0 ? '+' : ''}
                  {data.improvementPercent}%
                </div>
                <div className="label">{t.improvement}</div>
              </div>
            )}
          </div>
          <table>
            <thead>
              <tr>
                <th>{t.dateColumn}</th>
                <th>{t.scoreColumn}</th>
                <th>{t.correctColumn}</th>
                <th>{t.percentColumn}</th>
              </tr>
            </thead>
            <tbody>
              {data.sessions.map((s, i) => (
                <tr key={i}>
                  <td>{s.dateTime}</td>
                  <td>{s.score}</td>
                  <td>
                    {s.correctAnswers}/{s.totalQuestions}
                  </td>
                  <td>{s.percent}%</td>
                </tr>
              ))}
            </tbody>
          </table>
        </>
      )}
    </div>
  );
}
