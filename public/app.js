const candidateInput = document.getElementById('candidateName');
candidateInput.value = localStorage.getItem('candidateName') || '';
candidateInput.addEventListener('input', () => {
  localStorage.setItem('candidateName', candidateInput.value.trim());
});

function candidateName() {
  return candidateInput.value.trim();
}

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
  if (!candidateName()) {
    setupError.textContent = 'Zəhmət olmasa əvvəlcə adınızı daxil edin.';
    return;
  }
  const count = parseInt(document.getElementById('questionCount').value, 10) || 10;

  const res = await fetch('/api/quiz/start', {
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

    q.options.forEach((option, optIndex) => {
      const label = document.createElement('label');
      label.className = 'option';
      const radio = document.createElement('input');
      radio.type = 'radio';
      radio.name = 'q-' + q.id;
      radio.value = optIndex;
      label.appendChild(radio);
      label.appendChild(document.createTextNode(option));
      card.appendChild(label);
    });

    quizForm.appendChild(card);
  });

  const submitBtn = document.createElement('button');
  submitBtn.type = 'button';
  submitBtn.className = 'primary';
  submitBtn.textContent = 'Cavabları göndər';
  submitBtn.addEventListener('click', submitQuiz);
  quizForm.appendChild(submitBtn);
}

async function submitQuiz() {
  const answers = currentQuestions.map(q => {
    const checked = quizForm.querySelector(`input[name="q-${q.id}"]:checked`);
    return { questionId: q.id, selectedIndex: checked ? parseInt(checked.value, 10) : -1 };
  });

  const res = await fetch('/api/quiz/submit', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ candidateName: candidateName(), answers })
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

    detail.options.forEach((option, optIndex) => {
      const row = document.createElement('div');
      row.className = 'option';
      if (optIndex === detail.correctIndex) {
        row.classList.add('correct');
      } else if (optIndex === detail.selectedIndex && !detail.correct) {
        row.classList.add('incorrect');
      }
      row.textContent = option;
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
  if (!candidateName()) {
    container.innerHTML = '<p class="muted">Statistikanı görmək üçün əvvəlcə adınızı daxil edin.</p>';
    return;
  }
  container.innerHTML = '<p class="muted">Yüklənir...</p>';
  const res = await fetch('/api/stats/weak?candidate=' + encodeURIComponent(candidateName()));
  const data = await res.json();

  if (data.topics.length === 0) {
    container.innerHTML = '<p class="muted">Bu istifadəçi üçün müsahibə tarixçəsi tapılmadı.</p>';
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
  if (!candidateName()) {
    container.innerHTML = '<p class="muted">Statistikanı görmək üçün əvvəlcə adınızı daxil edin.</p>';
    return;
  }
  container.innerHTML = '<p class="muted">Yüklənir...</p>';
  const res = await fetch('/api/stats/progress?candidate=' + encodeURIComponent(candidateName()));
  const data = await res.json();

  if (data.sessions.length === 0) {
    container.innerHTML = '<p class="muted">Bu istifadəçi üçün müsahibə tarixçəsi tapılmadı.</p>';
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
