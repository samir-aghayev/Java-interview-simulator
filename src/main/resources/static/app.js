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
  document.getElementById('adminTab').classList.toggle('hidden', !user || user.role !== 'ADMIN');
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
    if (tab.dataset.view === 'admin') loadAdminQuestions(0);
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

// ===== Admin Panel =====

const OPTION_COUNT = 4;
let editingQuestionId = null;

document.querySelectorAll('.subtab').forEach(st => {
  st.addEventListener('click', () => {
    document.querySelectorAll('.subtab').forEach(s => s.classList.remove('active'));
    document.querySelectorAll('.admin-pane').forEach(p => p.classList.remove('active'));
    st.classList.add('active');
    document.getElementById('admin-pane-' + st.dataset.pane).classList.add('active');
    if (st.dataset.pane === 'questions') loadAdminQuestions(0);
    if (st.dataset.pane === 'users') loadAdminUsers(0);
    if (st.dataset.pane === 'audit') loadAudit(0);
  });
});

function renderPager(containerId, data, loadFn) {
  const pager = document.getElementById(containerId);
  pager.innerHTML = '';
  if (data.totalPages <= 1) return;
  const prev = document.createElement('button');
  prev.className = 'small';
  prev.textContent = '← Əvvəlki';
  prev.disabled = data.page === 0;
  prev.addEventListener('click', () => loadFn(data.page - 1));
  const info = document.createElement('span');
  info.textContent = `Səhifə ${data.page + 1}/${data.totalPages} (cəmi ${data.totalElements})`;
  const next = document.createElement('button');
  next.className = 'small';
  next.textContent = 'Növbəti →';
  next.disabled = data.page >= data.totalPages - 1;
  next.addEventListener('click', () => loadFn(data.page + 1));
  pager.appendChild(prev);
  pager.appendChild(info);
  pager.appendChild(next);
}

function badge(text, cls) {
  const b = document.createElement('span');
  b.className = 'badge ' + cls;
  b.textContent = text;
  return b;
}

// --- Suallar ---

document.getElementById('questionSearchBtn').addEventListener('click', () => loadAdminQuestions(0));
document.getElementById('questionSearch').addEventListener('keydown', e => {
  if (e.key === 'Enter') { e.preventDefault(); loadAdminQuestions(0); }
});
document.getElementById('newQuestionBtn').addEventListener('click', () => openQuestionForm(null));
document.getElementById('qfCancelBtn').addEventListener('click', closeQuestionForm);
document.getElementById('qfSaveBtn').addEventListener('click', saveQuestion);

function buildOptionInputs(options) {
  const wrap = document.getElementById('qfOptions');
  wrap.innerHTML = '';
  for (let i = 0; i < OPTION_COUNT; i++) {
    const row = document.createElement('div');
    row.className = 'qf-option-row';
    const radio = document.createElement('input');
    radio.type = 'radio';
    radio.name = 'qfCorrect';
    radio.value = i;
    if (options ? options[i] && options[i].correct : i === 0) radio.checked = true;
    const input = document.createElement('input');
    input.type = 'text';
    input.placeholder = `Variant ${i + 1}`;
    input.dataset.optIndex = i;
    if (options && options[i]) input.value = options[i].text;
    row.appendChild(radio);
    row.appendChild(input);
    wrap.appendChild(row);
  }
}

function openQuestionForm(question) {
  editingQuestionId = question ? question.id : null;
  document.getElementById('question-form-title').textContent = question ? 'Sualı redaktə et' : 'Yeni sual';
  document.getElementById('qfTopic').value = question ? question.topic : '';
  document.getElementById('qfText').value = question ? question.text : '';
  document.getElementById('qfDifficulty').value = question ? question.difficulty : 'MEDIUM';
  buildOptionInputs(question ? question.options : null);
  document.getElementById('qf-error').textContent = '';
  document.getElementById('question-form').classList.remove('hidden');
  document.getElementById('question-form').scrollIntoView({ behavior: 'smooth' });
}

function closeQuestionForm() {
  editingQuestionId = null;
  document.getElementById('question-form').classList.add('hidden');
}

async function saveQuestion() {
  const errorEl = document.getElementById('qf-error');
  errorEl.textContent = '';
  const options = [...document.querySelectorAll('#qfOptions input[type="text"]')].map(i => i.value.trim());
  const correctRadio = document.querySelector('input[name="qfCorrect"]:checked');
  const body = {
    topic: document.getElementById('qfTopic').value.trim(),
    text: document.getElementById('qfText').value.trim(),
    difficulty: document.getElementById('qfDifficulty').value,
    options,
    correctIndex: correctRadio ? parseInt(correctRadio.value, 10) : 0
  };
  const url = editingQuestionId ? '/api/admin/questions/' + editingQuestionId : '/api/admin/questions';
  const res = await authFetch(url, {
    method: editingQuestionId ? 'PUT' : 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  });
  if (!res.ok) {
    errorEl.textContent = await errorMessage(res, 'Yadda saxlamaq mümkün olmadı.');
    return;
  }
  closeQuestionForm();
  loadAdminQuestions(0);
}

let adminQuestionsPage = 0;

async function loadAdminQuestions(page) {
  adminQuestionsPage = page;
  const container = document.getElementById('admin-questions-list');
  container.innerHTML = '<p class="muted">Yüklənir...</p>';
  const search = encodeURIComponent(document.getElementById('questionSearch').value.trim());
  const res = await authFetch(`/api/admin/questions?search=${search}&page=${page}&size=10`);
  if (!res.ok) {
    container.innerHTML = '<p class="error">Yükləmək mümkün olmadı.</p>';
    return;
  }
  const data = await res.json();
  container.innerHTML = '';
  if (data.content.length === 0) {
    container.innerHTML = '<p class="muted">Sual tapılmadı.</p>';
  }
  data.content.forEach(q => {
    const row = document.createElement('div');
    row.className = 'admin-row';

    const main = document.createElement('div');
    main.className = 'admin-row-main';
    const title = document.createElement('div');
    title.className = 'admin-row-title';
    title.textContent = q.text;
    const sub = document.createElement('div');
    sub.className = 'admin-row-sub';
    sub.textContent = `${q.topic} · ${q.difficulty} `;
    sub.appendChild(q.active ? badge('aktiv', 'active-q') : badge('deaktiv', 'inactive'));
    main.appendChild(title);
    main.appendChild(sub);

    const actions = document.createElement('div');
    actions.className = 'admin-row-actions';
    const editBtn = document.createElement('button');
    editBtn.className = 'small';
    editBtn.textContent = 'Redaktə';
    editBtn.addEventListener('click', () => openQuestionForm(q));
    actions.appendChild(editBtn);
    if (q.active) {
      const delBtn = document.createElement('button');
      delBtn.className = 'small danger';
      delBtn.textContent = 'Deaktiv et';
      delBtn.addEventListener('click', async () => {
        await authFetch('/api/admin/questions/' + q.id, { method: 'DELETE' });
        loadAdminQuestions(adminQuestionsPage);
      });
      actions.appendChild(delBtn);
    } else {
      const restoreBtn = document.createElement('button');
      restoreBtn.className = 'small';
      restoreBtn.textContent = 'Bərpa et';
      restoreBtn.addEventListener('click', async () => {
        await authFetch('/api/admin/questions/' + q.id + '/restore', { method: 'POST' });
        loadAdminQuestions(adminQuestionsPage);
      });
      actions.appendChild(restoreBtn);
    }

    row.appendChild(main);
    row.appendChild(actions);
    container.appendChild(row);
  });
  renderPager('admin-questions-pager', data, loadAdminQuestions);
}

// --- İstifadəçilər ---

document.getElementById('userSearchBtn').addEventListener('click', () => loadAdminUsers(0));
document.getElementById('userSearch').addEventListener('keydown', e => {
  if (e.key === 'Enter') { e.preventDefault(); loadAdminUsers(0); }
});

let adminUsersPage = 0;

async function loadAdminUsers(page) {
  adminUsersPage = page;
  const container = document.getElementById('admin-users-list');
  container.innerHTML = '<p class="muted">Yüklənir...</p>';
  const search = encodeURIComponent(document.getElementById('userSearch').value.trim());
  const res = await authFetch(`/api/admin/users?search=${search}&page=${page}&size=10`);
  if (!res.ok) {
    container.innerHTML = '<p class="error">Yükləmək mümkün olmadı.</p>';
    return;
  }
  const data = await res.json();
  const me = getUser();
  container.innerHTML = '';
  data.content.forEach(u => {
    const row = document.createElement('div');
    row.className = 'admin-row';

    const main = document.createElement('div');
    main.className = 'admin-row-main';
    const title = document.createElement('div');
    title.className = 'admin-row-title';
    title.textContent = `${u.displayName} (${u.email}) `;
    title.appendChild(badge(u.role, u.role === 'ADMIN' ? 'admin' : 'user'));
    const sub = document.createElement('div');
    sub.className = 'admin-row-sub';
    sub.textContent = 'Qeydiyyat: ' + u.createdAt;
    main.appendChild(title);
    main.appendChild(sub);

    const actions = document.createElement('div');
    actions.className = 'admin-row-actions';
    if (me && u.email !== me.email) {
      const toggleBtn = document.createElement('button');
      toggleBtn.className = 'small';
      const newRole = u.role === 'ADMIN' ? 'USER' : 'ADMIN';
      toggleBtn.textContent = newRole === 'ADMIN' ? 'Admin et' : 'User et';
      toggleBtn.addEventListener('click', async () => {
        const errEl = document.getElementById('admin-users-error');
        errEl.textContent = '';
        const r = await authFetch('/api/admin/users/' + u.id + '/role', {
          method: 'PATCH',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ role: newRole })
        });
        if (!r.ok) {
          errEl.textContent = await errorMessage(r, 'Rolu dəyişmək mümkün olmadı.');
          return;
        }
        loadAdminUsers(adminUsersPage);
      });
      actions.appendChild(toggleBtn);
    }

    row.appendChild(main);
    row.appendChild(actions);
    container.appendChild(row);
  });
  renderPager('admin-users-pager', data, loadAdminUsers);
}

// --- Audit ---

async function loadAudit(page) {
  const container = document.getElementById('admin-audit-list');
  container.innerHTML = '<p class="muted">Yüklənir...</p>';
  const res = await authFetch(`/api/admin/audit?page=${page}&size=15`);
  if (!res.ok) {
    container.innerHTML = '<p class="error">Yükləmək mümkün olmadı.</p>';
    return;
  }
  const data = await res.json();
  container.innerHTML = '';
  if (data.content.length === 0) {
    container.innerHTML = '<p class="muted">Audit qeydi yoxdur.</p>';
  }
  data.content.forEach(l => {
    const row = document.createElement('div');
    row.className = 'admin-row';
    const main = document.createElement('div');
    main.className = 'admin-row-main';
    const title = document.createElement('div');
    title.className = 'admin-row-title';
    title.textContent = `${l.action} — ${l.details || ''}`;
    const sub = document.createElement('div');
    sub.className = 'admin-row-sub';
    sub.textContent = `${l.createdAt} · ${l.adminEmail}`;
    main.appendChild(title);
    main.appendChild(sub);
    row.appendChild(main);
    container.appendChild(row);
  });
  renderPager('admin-audit-pager', data, loadAudit);
}

if (getToken()) {
  showAppView();
} else {
  showAuthView();
}
