const TOKEN_KEY = 'jits_token';
const USER_KEY = 'jits_user';

function getToken() {
  return localStorage.getItem(TOKEN_KEY);
}

function getUser() {
  const raw = localStorage.getItem(USER_KEY);
  return raw ? JSON.parse(raw) : null;
}

function setSession(token, user) {
  localStorage.setItem(TOKEN_KEY, token);
  localStorage.setItem(USER_KEY, JSON.stringify(user));
}

function clearSession() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
}

async function authFetch(url, options = {}) {
  const token = getToken();
  const headers = Object.assign({}, options.headers, token ? { Authorization: 'Bearer ' + token } : {});
  const res = await fetch(url, Object.assign({}, options, { headers }));
  if (res.status === 401) {
    clearSession();
    showAuthView();
    throw new Error('Unauthorized');
  }
  return res;
}

async function errorMessage(res, fallback) {
  const body = await res.json().catch(() => ({}));
  return body.message || body.error || fallback;
}

const authView = document.getElementById('view-auth');
const appTabs = document.getElementById('app-tabs');
const userBar = document.getElementById('user-bar');
const userDisplayName = document.getElementById('userDisplayName');
const loginCard = document.getElementById('login-card');
const registerCard = document.getElementById('register-card');

function showAuthView() {
  document.querySelectorAll('.view').forEach(v => v.classList.remove('active'));
  authView.classList.add('active');
  appTabs.classList.add('hidden');
  userBar.classList.add('hidden');
}

function showAppView() {
  const user = getUser();
  userDisplayName.textContent = user ? user.displayName : '';
  userBar.classList.remove('hidden');
  appTabs.classList.remove('hidden');
  document.querySelectorAll('.view').forEach(v => v.classList.remove('active'));
  document.getElementById('view-quiz').classList.add('active');
  document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
  document.querySelector('.tab[data-view="quiz"]').classList.add('active');
}

document.getElementById('showRegister').addEventListener('click', (e) => {
  e.preventDefault();
  loginCard.classList.add('hidden');
  registerCard.classList.remove('hidden');
});

document.getElementById('showLogin').addEventListener('click', (e) => {
  e.preventDefault();
  registerCard.classList.add('hidden');
  loginCard.classList.remove('hidden');
});

document.getElementById('loginBtn').addEventListener('click', async () => {
  const errorEl = document.getElementById('login-error');
  errorEl.textContent = '';
  const email = document.getElementById('loginEmail').value.trim();
  const password = document.getElementById('loginPassword').value;
  const res = await fetch('/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password })
  });
  if (!res.ok) {
    errorEl.textContent = await errorMessage(res, 'Daxil olma uğursuz oldu.');
    return;
  }
  const data = await res.json();
  setSession(data.token, { email: data.email, displayName: data.displayName, role: data.role });
  showAppView();
});

document.getElementById('registerBtn').addEventListener('click', async () => {
  const errorEl = document.getElementById('register-error');
  errorEl.textContent = '';
  const displayName = document.getElementById('registerDisplayName').value.trim();
  const email = document.getElementById('registerEmail').value.trim();
  const password = document.getElementById('registerPassword').value;
  const res = await fetch('/api/auth/register', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password, displayName })
  });
  if (!res.ok) {
    errorEl.textContent = await errorMessage(res, 'Qeydiyyat uğursuz oldu.');
    return;
  }
  const data = await res.json();
  setSession(data.token, { email: data.email, displayName: data.displayName, role: data.role });
  showAppView();
});

document.getElementById('logoutBtn').addEventListener('click', () => {
  clearSession();
  showAuthView();
});

document.querySelectorAll('.tab').forEach(tab => {
  tab.addEventListener('click', () => {
    document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
    document.querySelectorAll('.view').forEach(v => v.classList.remove('active'));
    tab.classList.add('active');
    document.getElementById('view-' + tab.dataset.view).classList.add('active');
    if (tab.dataset.view === 'weak') loadWeakTopics();
    if (tab.dataset.view === 'progress') loadProgress();
  });
});

const setupError = document.getElementById('setup-error');
const quizSetup = document.getElementById('quiz-setup');
const quizForm = document.getElementById('quiz-form');
const quizResult = document.getElementById('quiz-result');
let currentQuestions = [];

document.getElementById('startBtn').addEventListener('click', startQuiz);

async function startQuiz() {
  setupError.textContent = '';
  const count = parseInt(document.getElementById('questionCount').value, 10) || 10;

  const res = await authFetch('/api/quiz/start', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ questionCount: count })
  });
  const data = await res.json();
  currentQuestions = data.questions;
  renderQuiz(currentQuestions);
}

function renderQuiz(questions) {
  quizSetup.classList.add('hidden');
  quizResult.classList.add('hidden');
  quizForm.classList.remove('hidden');
  quizForm.innerHTML = '';

  questions.forEach((q, index) => {
    const card = document.createElement('div');
    card.className = 'question-card';

    const meta = document.createElement('div');
    meta.className = 'question-meta';
    meta.textContent = `${index + 1}/${questions.length} · ${q.topic}`;
    card.appendChild(meta);

    const text = document.createElement('div');
    text.className = 'question-text';
    text.textContent = q.text;
    card.appendChild(text);

    q.options.forEach(option => {
      const label = document.createElement('label');
      label.className = 'option';
      const radio = document.createElement('input');
      radio.type = 'radio';
      radio.name = 'q-' + q.id;
      radio.value = option.index;
      label.appendChild(radio);
      label.appendChild(document.createTextNode(option.text));
      card.appendChild(label);
    });

    card.appendChild(buildDifficultyPicker(q.id));

    quizForm.appendChild(card);
  });

  const submitBtn = document.createElement('button');
  submitBtn.type = 'button';
  submitBtn.className = 'primary';
  submitBtn.textContent = 'Cavabları göndər';
  submitBtn.addEventListener('click', submitQuiz);
  quizForm.appendChild(submitBtn);
}

const DIFFICULTY_LEVELS = [
  { value: 'EASY', label: 'Asan' },
  { value: 'MEDIUM', label: 'Orta' },
  { value: 'HARD', label: 'Çətin' }
];

function buildDifficultyPicker(questionId) {
  const wrap = document.createElement('div');
  wrap.className = 'difficulty-picker';
  wrap.dataset.questionId = questionId;

  const label = document.createElement('span');
  label.className = 'difficulty-label';
  label.textContent = 'Bu sual sizə necə gəldi?';
  wrap.appendChild(label);

  DIFFICULTY_LEVELS.forEach(level => {
    const btn = document.createElement('button');
    btn.type = 'button';
    btn.className = 'difficulty-btn';
    btn.textContent = level.label;
    btn.dataset.value = level.value;
    btn.addEventListener('click', () => {
      wrap.querySelectorAll('.difficulty-btn').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      wrap.dataset.selected = level.value;
    });
    wrap.appendChild(btn);
  });

  return wrap;
}

async function submitQuiz() {
  const answers = currentQuestions.map(q => {
    const checked = quizForm.querySelector(`input[name="q-${q.id}"]:checked`);
    const picker = quizForm.querySelector(`.difficulty-picker[data-question-id="${q.id}"]`);
    return {
      questionId: q.id,
      selectedIndex: checked ? parseInt(checked.value, 10) : -1,
      perceivedDifficulty: picker ? picker.dataset.selected || null : null
    };
  });

  const res = await authFetch('/api/quiz/submit', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ answers })
  });
  const result = await res.json();
  renderResult(result);
}

function renderResult(result) {
  quizForm.classList.add('hidden');
  quizResult.classList.remove('hidden');
  quizResult.innerHTML = '';

  const heading = document.createElement('h2');
  heading.textContent = 'Nəticə';
  quizResult.appendChild(heading);

  const summary = document.createElement('div');
  summary.className = 'score-summary';
  summary.appendChild(statTile(result.score, 'Bal'));
  summary.appendChild(statTile(`${result.correctAnswers}/${result.totalQuestions}`, 'Düzgün cavab'));
  quizResult.appendChild(summary);

  if (result.weakTopics.length === 0) {
    const p = document.createElement('p');
    p.textContent = 'Zəif mövzu aşkarlanmadı. Əla nəticə!';
    quizResult.appendChild(p);
  } else {
    const p = document.createElement('p');
    p.textContent = 'Zəif mövzular:';
    quizResult.appendChild(p);
    result.weakTopics.forEach(topic => {
      const badge = document.createElement('span');
      badge.className = 'weak-badge';
      badge.textContent = topic;
      quizResult.appendChild(badge);
    });
  }

  result.details.forEach((detail, index) => {
    const card = document.createElement('div');
    card.className = 'question-card';

    const meta = document.createElement('div');
    meta.className = 'question-meta';
    meta.textContent = `${index + 1}. ${detail.topic}`;
    card.appendChild(meta);

    const text = document.createElement('div');
    text.className = 'question-text';
    text.textContent = detail.text;
    card.appendChild(text);

    detail.options.forEach(option => {
      const row = document.createElement('div');
      row.className = 'option';
      if (option.index === detail.correctIndex) {
        row.classList.add('correct');
      } else if (option.index === detail.selectedIndex && !detail.correct) {
        row.classList.add('incorrect');
      }
      row.textContent = option.text;
      card.appendChild(row);
    });

    quizResult.appendChild(card);
  });

  const retryBtn = document.createElement('button');
  retryBtn.className = 'primary';
  retryBtn.textContent = 'Yenidən başla';
  retryBtn.addEventListener('click', () => {
    quizResult.classList.add('hidden');
    quizSetup.classList.remove('hidden');
  });
  quizResult.appendChild(retryBtn);
}

function statTile(value, label) {
  const tile = document.createElement('div');
  tile.className = 'stat-tile';
  const v = document.createElement('div');
  v.className = 'value';
  v.textContent = value;
  const l = document.createElement('div');
  l.className = 'label';
  l.textContent = label;
  tile.appendChild(v);
  tile.appendChild(l);
  return tile;
}

async function loadWeakTopics() {
  const container = document.getElementById('weak-content');
  container.innerHTML = '<p class="muted">Yüklənir...</p>';
  const res = await authFetch('/api/stats/weak');
  const data = await res.json();

  if (data.topics.length === 0) {
    container.innerHTML = '<p class="muted">Müsahibə tarixçəsi tapılmadı.</p>';
    return;
  }

  container.innerHTML = '';
  data.topics.forEach(row => {
    const isWeak = data.weakTopics.includes(row.topic);
    const wrap = document.createElement('div');
    wrap.className = 'topic-row';

    const header = document.createElement('div');
    header.className = 'topic-row-header';
    header.innerHTML = `<span>${row.topic}${isWeak ? ' ⚠' : ''}</span><span>${row.correct}/${row.total} (${row.percent}%)</span>`;
    wrap.appendChild(header);

    const track = document.createElement('div');
    track.className = 'bar-track';
    const fill = document.createElement('div');
    fill.className = 'bar-fill' + (isWeak ? ' weak' : '');
    fill.style.width = row.percent + '%';
    track.appendChild(fill);
    wrap.appendChild(track);

    container.appendChild(wrap);
  });

  if (data.weakTopics.length > 0) {
    const p = document.createElement('p');
    p.style.marginTop = '16px';
    p.textContent = 'Diqqət yetirməli olduğunuz mövzular yuxarıda ⚠ işarəsi ilə göstərilib.';
    container.appendChild(p);
  }
}

async function loadProgress() {
  const container = document.getElementById('progress-content');
  container.innerHTML = '<p class="muted">Yüklənir...</p>';
  const res = await authFetch('/api/stats/progress');
  const data = await res.json();

  if (data.sessions.length === 0) {
    container.innerHTML = '<p class="muted">Müsahibə tarixçəsi tapılmadı.</p>';
    return;
  }

  container.innerHTML = '';
  const summary = document.createElement('div');
  summary.className = 'score-summary';
  summary.appendChild(statTile(data.averageScore, 'Orta bal'));
  if (typeof data.improvement === 'number') {
    const sign = data.improvement >= 0 ? '+' : '';
    summary.appendChild(statTile(sign + data.improvement, 'İlk→son dəyişmə'));
  }
  container.appendChild(summary);

  const table = document.createElement('table');
  table.innerHTML = `<thead><tr><th>Tarix</th><th>Bal</th><th>Düzgün</th></tr></thead>`;
  const tbody = document.createElement('tbody');
  data.sessions.forEach(s => {
    const tr = document.createElement('tr');
    tr.innerHTML = `<td>${s.dateTime}</td><td>${s.score}</td><td>${s.correctAnswers}/${s.totalQuestions}</td>`;
    tbody.appendChild(tr);
  });
  table.appendChild(tbody);
  container.appendChild(table);
}

if (getToken()) {
  showAppView();
} else {
  showAuthView();
}
