import { useEffect, useState } from 'react';
import { api } from '../api/client';
import { t } from '../i18n/strings';
import { translateTopic } from '../i18n/topics';
import type { WeakTopicsResponse } from '../types';

export default function WeakTopics() {
  const [data, setData] = useState<WeakTopicsResponse | null>(null);
  const [failed, setFailed] = useState(false);

  useEffect(() => {
    api<WeakTopicsResponse>('/api/stats/weak')
      .then(setData)
      .catch(() => setFailed(true));
  }, []);

  return (
    <div className="card">
      <h2>{t.weakTitle}</h2>
      {failed && <p className="error-message">{t.loadFailed}</p>}
      {!data && !failed && <p className="muted">{t.loading}</p>}
      {data && data.topics.length === 0 && <p className="muted">{t.noHistory}</p>}
      {data &&
        data.topics.map(row => {
          const isWeak = data.weakTopics.includes(row.topic);
          return (
            <div className="topic-row" key={row.topic}>
              <div className="topic-row-header">
                <span>
                  {translateTopic(row.topic)}
                  {isWeak ? ' ⚠' : ''}
                </span>
                <span>
                  {row.correct}/{row.total} ({row.percent}%)
                </span>
              </div>
              <div className="bar-track">
                <div
                  className={`bar-fill ${isWeak ? 'weak' : ''}`}
                  style={{ width: `${row.percent}%` }}
                />
              </div>
            </div>
          );
        })}
      {data && data.weakTopics.length > 0 && <p className="hint">{t.weakHint}</p>}
    </div>
  );
}
