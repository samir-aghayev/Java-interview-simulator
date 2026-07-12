import { useState } from 'react';
import { api, ApiError } from '../api/client';
import { t } from '../i18n/strings';

export default function ReportModal({
  questionId,
  onClose
}: {
  questionId: string;
  onClose: () => void;
}) {
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [sent, setSent] = useState(false);
  const [sending, setSending] = useState(false);

  async function send() {
    setError('');
    setSending(true);
    try {
      await api('/api/reports', {
        method: 'POST',
        body: JSON.stringify({ questionId, message: message.trim() })
      });
      setSent(true);
      setTimeout(onClose, 1200);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : t.reportFailed);
    } finally {
      setSending(false);
    }
  }

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal" onClick={e => e.stopPropagation()}>
        <h3>
          <i className="fas fa-flag" /> {t.reportTitle}
        </h3>
        {sent ? (
          <div className="success-message">{t.reportSuccess}</div>
        ) : (
          <>
            {error && <div className="error-message">{error}</div>}
            <textarea
              rows={4}
              value={message}
              placeholder={t.reportPlaceholder}
              onChange={e => setMessage(e.target.value)}
            />
            <div className="modal-actions">
              <button className="btn-primary" disabled={sending || message.trim().length < 5} onClick={send}>
                {t.reportSend}
              </button>
              <button className="btn-secondary" onClick={onClose}>
                {t.reportCancel}
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  );
}
