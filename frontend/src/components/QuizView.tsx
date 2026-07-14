import { useEffect, useState } from 'react';
import { api, ApiError } from '../api/client';
import { getLang, t } from '../i18n/strings';
import { translateTopic } from '../i18n/topics';
import type { GradeResponse, Question, QuizStartResponse, SubjectTopics } from '../types';
import ReportModal from './ReportModal';

export type Phase = 'setup' | 'running' | 'result';

const DIFFICULTIES = [
  { value: 'EASY', label: t.difficultyEasy },
  { value: 'MEDIUM', label: t.difficultyMedium },
  { value: 'HARD', label: t.difficultyHard }
];

export default function QuizView({ onPhaseChange }: { onPhaseChange?: (phase: Phase) => void }) {
  const [phase, setPhase] = useState<Phase>('setup');
  const [meta, setMeta] = useState<SubjectTopics[]>([]);
  const [count, setCount] = useState(10);
  const [subject, setSubject] = useState('');
  const [selectedTopics, setSelectedTopics] = useState<string[]>([]);
  const [questions, setQuestions] = useState<Question[]>([]);
  const [answers, setAnswers] = useState<Record<string, number>>({});
  const [perceived, setPerceived] = useState<Record<string, string>>({});
  const [result, setResult] = useState<GradeResponse | null>(null);
  const [reportFor, setReportFor] = useState<string | null>(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    api<SubjectTopics[]>('/api/meta/topics')
      .then(setMeta)
      .catch(() => setMeta([]));
  }, []);

  useEffect(() => {
    onPhaseChange?.(phase);
    return () => onPhaseChange?.('setup');
  }, [phase]);

  useEffect(() => {
    if (phase !== 'running') return;
    function handleBeforeUnload(e: BeforeUnloadEvent) {
      e.preventDefault();
      e.returnValue = '';
    }
    window.addEventListener('beforeunload', handleBeforeUnload);
    return () => window.removeEventListener('beforeunload', handleBeforeUnload);
  }, [phase]);

  const currentTopics = meta.find(s => s.subject === subject)?.topics ?? [];

  function toggleTopic(topic: string) {
    setSelectedTopics(prev =>
      prev.includes(topic) ? prev.filter(x => x !== topic) : [...prev, topic]
    );
  }

  async function start() {
    setError('');
    setLoading(true);
    try {
      const body: Record<string, unknown> = { questionCount: count, locale: getLang() };
      if (subject && selectedTopics.length > 0) {
        body.topics = selectedTopics;
      } else if (subject) {
        body.subject = subject;
      }
      const data = await api<QuizStartResponse>('/api/quiz/start', {
        method: 'POST',
        body: JSON.stringify(body)
      });
      if (data.questions.length === 0) {
        setError(t.noQuestionsAvailable);
        return;
      }
      setQuestions(data.questions);
      setAnswers({});
      setPerceived({});
      setPhase('running');
    } catch (err) {
      setError(err instanceof ApiError ? err.message : t.loadFailed);
    } finally {
      setLoading(false);
    }
  }

  async function submit() {
    setError('');
    setLoading(true);
    try {
      const payload = {
        locale: getLang(),
        answers: questions.map(q => ({
          questionId: q.id,
          selectedIndex: answers[q.id] ?? -1,
          perceivedDifficulty: perceived[q.id] ?? null
        }))
      };
      const data = await api<GradeResponse>('/api/quiz/submit', {
        method: 'POST',
        body: JSON.stringify(payload)
      });
      setResult(data);
      setPhase('result');
    } catch (err) {
      setError(err instanceof ApiError ? err.message : t.loadFailed);
    } finally {
      setLoading(false);
    }
  }

  if (phase === 'setup') {
    return (
      <div className="card">
        <h2>{t.quizSetupTitle}</h2>
        {error && <div className="error-message">{error}</div>}

        <label htmlFor="questionCount">{t.questionCountLabel}</label>
        <input
          id="questionCount"
          type="number"
          min={1}
          max={1000}
          value={count}
          onChange={e => setCount(parseInt(e.target.value, 10) || 10)}
        />

        <label htmlFor="subjectSelect">{t.subjectLabel}</label>
        <select
          id="subjectSelect"
          value={subject}
          onChange={e => {
            setSubject(e.target.value);
            setSelectedTopics([]);
          }}
        >
          <option value="">{t.mixedOption}</option>
          {meta.map(s => (
            <option key={s.subject} value={s.subject}>
              {s.subject}
            </option>
          ))}
        </select>

        {subject && currentTopics.length > 0 && (
          <>
            <label>{t.topicsLabel}</label>
            <div className="topic-chips">
              <button
                type="button"
                className={`chip ${selectedTopics.length === 0 ? 'active' : ''}`}
                onClick={() => setSelectedTopics([])}
              >
                {t.allTopics}
              </button>
              {currentTopics.map(topic => (
                <button
                  type="button"
                  key={topic}
                  className={`chip ${selectedTopics.includes(topic) ? 'active' : ''}`}
                  onClick={() => toggleTopic(topic)}
                >
                  {translateTopic(topic)}
                </button>
              ))}
            </div>
          </>
        )}

        <button className="btn-primary big" id="startBtn" disabled={loading} onClick={start}>
          <i className="fas fa-play" /> {t.startButton}
        </button>
      </div>
    );
  }

  if (phase === 'running') {
    return (
      <div>
        {error && <div className="error-message">{error}</div>}
        {questions.map((q, index) => (
          <div className="question-card" key={q.id}>
            <div className="question-head">
              <div className="question-meta">
                {index + 1}/{questions.length} · {translateTopic(q.topic)}
              </div>
              <button className="btn-report" onClick={() => setReportFor(q.id)}>
                <i className="fas fa-flag" /> {t.reportButton}
              </button>
            </div>
            <div className="question-text">{q.text}</div>
            {q.options.map(option => (
              <label
                key={option.index}
                className={`option ${answers[q.id] === option.index ? 'selected' : ''}`}
              >
                <input
                  type="radio"
                  name={`q-${q.id}`}
                  checked={answers[q.id] === option.index}
                  onChange={() => setAnswers(prev => ({ ...prev, [q.id]: option.index }))}
                />
                {option.text}
              </label>
            ))}
            <div className="difficulty-picker">
              <span className="difficulty-label">{t.difficultyPrompt}</span>
              {DIFFICULTIES.map(d => (
                <button
                  type="button"
                  key={d.value}
                  className={`difficulty-btn ${perceived[q.id] === d.value ? 'active' : ''}`}
                  onClick={() => setPerceived(prev => ({ ...prev, [q.id]: d.value }))}
                >
                  {d.label}
                </button>
              ))}
            </div>
          </div>
        ))}
        <button className="btn-primary big" disabled={loading} onClick={submit}>
          <i className="fas fa-paper-plane" /> {t.submitAnswers}
        </button>
        {reportFor && <ReportModal questionId={reportFor} onClose={() => setReportFor(null)} />}
      </div>
    );
  }

  // phase === 'result'
  return (
    <div>
      <div className="card">
        <h2>{t.resultTitle}</h2>
        <div className="score-summary">
          <div className="stat-tile">
            <div className="value">{result?.score}</div>
            <div className="label">{t.scoreLabel}</div>
          </div>
          <div className="stat-tile">
            <div className="value">
              {result?.correctAnswers}/{result?.totalQuestions}
            </div>
            <div className="label">{t.correctLabel}</div>
          </div>
        </div>
        {result && result.weakTopics.length === 0 ? (
          <p>{t.noWeakTopics}</p>
        ) : (
          <p>
            {t.weakTopicsLabel}{' '}
            {result?.weakTopics.map(topic => (
              <span className="weak-badge" key={topic}>
                {translateTopic(topic)}
              </span>
            ))}
          </p>
        )}
        <button
          className="btn-primary"
          onClick={() => {
            setPhase('setup');
            setResult(null);
          }}
        >
          <i className="fas fa-rotate-right" /> {t.retryButton}
        </button>
      </div>

      {result?.details.map((detail, index) => {
        const skipped = detail.selectedIndex < 0;
        return (
          <div className="question-card" key={detail.id}>
            <div className="question-head">
              <div className="question-meta">
                {index + 1}. {translateTopic(detail.topic)}
                {skipped && <span className="not-answered-badge">{t.notAnswered}</span>}
              </div>
              <button className="btn-report" onClick={() => setReportFor(detail.id)}>
                <i className="fas fa-flag" /> {t.reportButton}
              </button>
            </div>
            <div className="question-text">{detail.text}</div>
            {detail.options.map(option => {
              let cls = 'option readonly';
              if (option.index === detail.correctIndex) cls += ' correct';
              else if (option.index === detail.selectedIndex && !detail.correct) cls += ' incorrect';
              return (
                <div className={cls} key={option.index}>
                  {option.text}
                </div>
              );
            })}
          </div>
        );
      })}
      {reportFor && <ReportModal questionId={reportFor} onClose={() => setReportFor(null)} />}
    </div>
  );
}
