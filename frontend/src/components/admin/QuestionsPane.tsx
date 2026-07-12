import { useCallback, useEffect, useState } from 'react';
import { api, ApiError } from '../../api/client';
import { t } from '../../i18n/strings';
import type { AdminQuestion, Paged, QuestionUpsertPayload, SubjectTopics } from '../../types';
import Pager from '../Pager';

const OPTION_COUNT = 4;

const EMPTY_FORM = {
  subject: '',
  topic: '',
  text: '',
  difficulty: 'MEDIUM',
  options: ['', '', '', ''],
  correctIndex: 0
};

export default function QuestionsPane() {
  const [search, setSearch] = useState('');
  const [data, setData] = useState<Paged<AdminQuestion> | null>(null);
  const [error, setError] = useState('');
  const [formError, setFormError] = useState('');
  const [subjects, setSubjects] = useState<string[]>([]);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ ...EMPTY_FORM, options: [...EMPTY_FORM.options] });

  const load = useCallback(async (page: number, searchValue: string) => {
    setError('');
    try {
      const result = await api<Paged<AdminQuestion>>(
        `/api/admin/questions?search=${encodeURIComponent(searchValue)}&page=${page}&size=10`
      );
      setData(result);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : t.loadFailed);
    }
  }, []);

  useEffect(() => {
    load(0, '');
    api<SubjectTopics[]>('/api/meta/topics')
      .then(meta => setSubjects(meta.map(s => s.subject)))
      .catch(() => setSubjects([]));
  }, [load]);

  function openForm(question: AdminQuestion | null) {
    setFormError('');
    if (question) {
      setEditingId(question.id);
      setForm({
        subject: question.subject,
        topic: question.topic,
        text: question.text,
        difficulty: question.difficulty,
        options: Array.from({ length: OPTION_COUNT }, (_, i) => question.options[i]?.text ?? ''),
        correctIndex: question.options.findIndex(o => o.correct)
      });
    } else {
      setEditingId(null);
      setForm({ ...EMPTY_FORM, options: [...EMPTY_FORM.options] });
    }
    setShowForm(true);
  }

  async function save() {
    setFormError('');
    const payload: QuestionUpsertPayload = {
      subject: form.subject.trim(),
      topic: form.topic.trim(),
      text: form.text.trim(),
      difficulty: form.difficulty,
      options: form.options.map(o => o.trim()),
      correctIndex: form.correctIndex
    };
    try {
      await api(editingId ? `/api/admin/questions/${editingId}` : '/api/admin/questions', {
        method: editingId ? 'PUT' : 'POST',
        body: JSON.stringify(payload)
      });
      setShowForm(false);
      load(0, search);
    } catch (err) {
      setFormError(err instanceof ApiError ? err.message : t.saveFailed);
    }
  }

  async function deactivate(id: string) {
    await api(`/api/admin/questions/${id}`, { method: 'DELETE' });
    load(data?.page ?? 0, search);
  }

  async function restore(id: string) {
    await api(`/api/admin/questions/${id}/restore`, { method: 'POST' });
    load(data?.page ?? 0, search);
  }

  return (
    <div className="card">
      <div className="admin-toolbar">
        <input
          type="text"
          value={search}
          placeholder={t.searchQuestions}
          onChange={e => setSearch(e.target.value)}
          onKeyDown={e => {
            if (e.key === 'Enter') load(0, search);
          }}
        />
        <button className="btn-secondary" onClick={() => load(0, search)}>
          <i className="fas fa-magnifying-glass" /> {t.searchButton}
        </button>
        <button className="btn-primary" onClick={() => openForm(null)}>
          {t.newQuestion}
        </button>
      </div>

      {showForm && (
        <div className="question-form">
          <h3>{editingId ? t.editQuestion : t.newQuestion}</h3>
          {formError && <div className="error-message">{formError}</div>}
          <label>{t.subjectField}</label>
          <input
            type="text"
            list="subject-list"
            value={form.subject}
            onChange={e => setForm(f => ({ ...f, subject: e.target.value }))}
          />
          <datalist id="subject-list">
            {subjects.map(s => (
              <option key={s} value={s} />
            ))}
          </datalist>
          <label>{t.topicField}</label>
          <input
            type="text"
            value={form.topic}
            onChange={e => setForm(f => ({ ...f, topic: e.target.value }))}
          />
          <label>{t.textField}</label>
          <textarea
            rows={3}
            value={form.text}
            onChange={e => setForm(f => ({ ...f, text: e.target.value }))}
          />
          <label>{t.difficultyField}</label>
          <select
            value={form.difficulty}
            onChange={e => setForm(f => ({ ...f, difficulty: e.target.value }))}
          >
            <option value="EASY">EASY (5)</option>
            <option value="MEDIUM">MEDIUM (10)</option>
            <option value="HARD">HARD (15)</option>
          </select>
          <label>{t.optionsField}</label>
          {form.options.map((option, i) => (
            <div className="qf-option-row" key={i}>
              <input
                type="radio"
                name="qfCorrect"
                checked={form.correctIndex === i}
                onChange={() => setForm(f => ({ ...f, correctIndex: i }))}
              />
              <input
                type="text"
                value={option}
                placeholder={`${t.optionPlaceholder} ${i + 1}`}
                onChange={e =>
                  setForm(f => {
                    const options = [...f.options];
                    options[i] = e.target.value;
                    return { ...f, options };
                  })
                }
              />
            </div>
          ))}
          <div className="admin-toolbar">
            <button className="btn-primary" onClick={save}>
              {t.saveButton}
            </button>
            <button className="btn-secondary" onClick={() => setShowForm(false)}>
              {t.cancelButton}
            </button>
          </div>
        </div>
      )}

      {error && <div className="error-message">{error}</div>}
      {!data && <p className="muted">{t.loading}</p>}
      {data && data.content.length === 0 && <p className="muted">{t.noResults}</p>}
      {data?.content.map(q => (
        <div className="admin-row" key={q.id}>
          <div className="admin-row-main">
            <div className="admin-row-title">{q.text}</div>
            <div className="admin-row-sub">
              {q.subject} · {q.topic} · {q.difficulty}{' '}
              <span className={`badge ${q.active ? 'active-q' : 'inactive'}`}>
                {q.active ? t.activeBadge : t.inactiveBadge}
              </span>
            </div>
          </div>
          <div className="admin-row-actions">
            <button className="btn-small" onClick={() => openForm(q)}>
              {t.editButton}
            </button>
            {q.active ? (
              <button className="btn-small danger" onClick={() => deactivate(q.id)}>
                {t.deactivateButton}
              </button>
            ) : (
              <button className="btn-small" onClick={() => restore(q.id)}>
                {t.restoreButton}
              </button>
            )}
          </div>
        </div>
      ))}
      {data && <Pager data={data} onPage={p => load(p, search)} />}
    </div>
  );
}
