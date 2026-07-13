# Interview Simulator

A web application that simulates technical interviews in the browser. It ships with a bank of
1000 Java questions across 50 topics, but the data model is subject-aware — question banks for
other fields (frontend, SQL, data analysis, AI, Excel, SAP, mathematics, ...) can be added
through the admin panel without any schema changes.

## Features

- **Quizzes with topic selection** — pick a subject and specific topics (or go fully mixed),
  choose the question count, and start
- **Per-request answer shuffling** — option order is reshuffled on every request, so seeing a
  question twice never reveals the answer's position
- **Scoring by difficulty** — EASY 5 / MEDIUM 10 / HARD 15 points
- **Weak-topic detection and progress history** — per-topic accuracy across all past sessions
  and a session-by-session score timeline
- **Self-rated difficulty with mastery** — after each question the user marks it Easy/Medium/Hard;
  a question rated *Easy* and answered correctly is never shown to that user again
- **Question reports** — users can flag a wrong question or answer with a message; admins
  review, resolve, or dismiss the reports
- **Accounts and roles** — email + password sign-up (bcrypt), stateless JWT sessions, USER/ADMIN
  roles; registration optionally collects birth date, gender, country, employment and education
  status for future analytics
- **Admin panel** (separate `/admin` page) — user role management, question CRUD with search
  and pagination, report handling, and an audit log of every admin action
- **Soft delete for questions** — deactivated questions leave the quiz pool but past results
  keep their integrity; questions can be restored
- **Dark mode** — animated toggle, persisted per browser
- **Two UI languages** — Azerbaijani and Turkish (AZ/TR toggle, persisted per browser); questions
  are served in the selected language when a translation exists, falling back to the original.
  All 1000 questions ship with hand-written Turkish translations
- **Leaderboard** — users ranked by average correct-answer percentage (minimum 3 sessions to
  qualify), with total score as a tiebreaker
- **Password reset** — tokenized forgot/reset-password flow; single-use, 30-minute tokens
- **Google OAuth login** — sign in with Google, wired end-to-end but inactive until
  `GOOGLE_CLIENT_ID`/`GOOGLE_CLIENT_SECRET` are configured (see CLAUDE.md)

## Tech stack

| Layer     | Technology |
|-----------|------------|
| Backend   | Java 25, Spring Boot 3.5 (Web, Data JPA, Security, Security OAuth2 Client, Validation) |
| Database  | PostgreSQL, Liquibase migrations (schema + 1000-question seed via CSV `loadData`) |
| Auth      | JWT (jjwt, HS512), bcrypt password hashing, per-IP rate limiting on auth endpoints |
| Frontend  | React 18 + TypeScript, Vite, react-router |
| Build     | Gradle (the boot jar embeds the built frontend) |
| Container | Docker (multi-stage build) + Docker Compose |

## Quick start

### Prerequisites

- **Java 17+** on your PATH to launch Gradle (the build itself auto-downloads **JDK 25** via the
  Foojay toolchain resolver if it's not installed)
- **Node.js 18+** (`npm` must be on your PATH) — used to build the React frontend
- **PostgreSQL** running locally (any recent version)

1. Create the database and user (once):

   ```sql
   CREATE DATABASE interview_simulator;
   CREATE USER interview_app WITH PASSWORD 'interview_app_pw';
   GRANT ALL PRIVILEGES ON DATABASE interview_simulator TO interview_app;
   ```

2. Run the application (Liquibase migrations run automatically and seed the question bank;
   the React frontend is built by Gradle via npm and packaged into the jar):

   Linux / macOS:

   ```bash
   ADMIN_EMAIL=admin@example.com ADMIN_PASSWORD=change-me ./gradlew bootRun
   ```

   Windows (PowerShell):

   ```powershell
   $env:ADMIN_EMAIL="admin@example.com"; $env:ADMIN_PASSWORD="change-me"; .\gradlew.bat bootRun
   ```

   `ADMIN_EMAIL`/`ADMIN_PASSWORD` bootstrap the first admin account: if the email exists it is
   promoted to ADMIN, otherwise the account is created.

3. Open http://localhost:8080

### Run with Docker

No local Java, Node, or PostgreSQL needed — everything runs in containers.

```bash
cp .env.example .env   # edit ADMIN_EMAIL/ADMIN_PASSWORD etc. as needed
docker compose up --build
```

Then open http://localhost:8080. The `db` service (PostgreSQL) and `app` service (the built jar)
are wired together automatically; `app` waits for `db`'s healthcheck before starting, and
Liquibase runs the migrations + seed on first boot. Data persists in the `db_data` named volume
across restarts — `docker compose down -v` to wipe it.

The image (`Dockerfile`) is a two-stage build: `eclipse-temurin:25-jdk-noble` (+ Node.js 24 for
the frontend's `npm run build`) compiles everything with `./gradlew build`, and the runtime stage
copies only the resulting jar onto `eclipse-temurin:25-jre-noble`, running as a non-root user.

| Variable (`.env`) | Default | Notes |
|---|---|---|
| `DB_PASSWORD` | `interview_app_pw` | Also used for the `db` container's Postgres password |
| `JWT_SECRET` | insecure dev default | **Override for anything beyond local use** |
| `JWT_EXPIRATION_MINUTES` | `1440` | Token lifetime |
| `ADMIN_EMAIL` / `ADMIN_PASSWORD` | unset | First-admin bootstrap (optional) |
| `FRONTEND_URL` | `http://localhost:8080` | Used to build password-reset and OAuth redirect links |
| `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET` | unset | Google login stays disabled until both are set |

### Troubleshooting

| Symptom | Fix |
|---|---|
| `Cannot run program "npm"` / `npm: command not found` | Install Node.js 18+ and reopen the terminal so `npm` is on PATH |
| `No matching toolchains found for Java 25` | Pull the latest code (the Foojay resolver auto-downloads JDK 25), or install JDK 25 manually |
| `Connection to localhost:5432 refused` | PostgreSQL is not running — start the service |
| `FATAL: password authentication failed for user "interview_app"` | Re-run the `CREATE USER` / `GRANT` statements from step 1, or set `DB_USER`/`DB_PASSWORD` |
| `FATAL: database "interview_simulator" does not exist` | Run the `CREATE DATABASE` statement from step 1 |
| Liquibase error `permission denied for schema public` (PostgreSQL 15+) | Run `GRANT ALL ON SCHEMA public TO interview_app;` inside the `interview_simulator` database |

### Configuration (environment variables)

| Variable | Default | Description |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/interview_simulator` | JDBC URL |
| `DB_USER` / `DB_PASSWORD` | `interview_app` / `interview_app_pw` | Database credentials |
| `SERVER_PORT` | `8080` | HTTP port |
| `JWT_SECRET` | insecure dev default | **Must be overridden in production** |
| `JWT_EXPIRATION_MINUTES` | `1440` | Token lifetime |
| `ADMIN_EMAIL` / `ADMIN_PASSWORD` | unset | First-admin bootstrap (optional) |

### Frontend development

The frontend lives in `frontend/` (Vite + React + TypeScript). For a fast dev loop run the
backend and the Vite dev server side by side — Vite proxies `/api` to `localhost:8080`:

```bash
./gradlew bootRun -PskipFrontend        # backend only
cd frontend && npm install && npm run dev   # http://localhost:5173
```

All UI strings are centralized in `frontend/src/i18n/strings.ts` with Azerbaijani and Turkish
maps; the language choice is stored in `localStorage` and applied on reload.

## API

All endpoints except `/api/auth/**` require an `Authorization: Bearer <token>` header.
Auth endpoints are rate-limited to 10 attempts per minute per IP.

### Auth

| Method | Path | Description |
|---|---|---|
| POST | `/api/auth/register` | `{email, password, firstName, lastName, birthDate?, gender?, country?, employmentStatus?, educationStatus?}` → `{token, email, displayName, role}` |
| POST | `/api/auth/login` | `{email, password}` → `{token, email, displayName, role}` |

### Quiz & stats

| Method | Path | Description |
|---|---|---|
| GET | `/api/meta/topics` | `[{subject, topics[]}]` — active subjects and their topics |
| POST | `/api/quiz/start` | `{questionCount, subject?, topics?, locale?}` → random questions from the filtered pool, excluding the user's mastered questions; options are `{index, text}` pairs freshly shuffled per request (`index` is the canonical authoring index); `locale` (`az`/`tr`) serves translated text where available |
| POST | `/api/quiz/submit` | `{answers: [{questionId, selectedIndex, perceivedDifficulty}], locale?}` → score, correct answers, weak topics, per-question details |
| GET | `/api/stats/weak` | Per-topic accuracy across the current user's sessions |
| GET | `/api/stats/progress` | Session history and average score |
| POST | `/api/reports` | `{questionId, message}` — flag a wrong question/answer |

### Admin (ADMIN role only)

| Method | Path | Description |
|---|---|---|
| GET | `/api/admin/users?search=&page=&size=` | User list incl. profile fields |
| PATCH | `/api/admin/users/{id}/role` | `{role: USER\|ADMIN}` — admins cannot change their own role |
| GET | `/api/admin/questions?search=&page=&size=` | Question list (search covers subject/topic/text, includes inactive) |
| POST | `/api/admin/questions` | `{subject, topic, text, difficulty, options[2..6], correctIndex}` |
| PUT | `/api/admin/questions/{id}` | Edit a question |
| DELETE | `/api/admin/questions/{id}` | Soft delete (deactivate) |
| POST | `/api/admin/questions/{id}/restore` | Reactivate |
| GET | `/api/admin/reports?status=&page=&size=` | Question reports (filter by OPEN/RESOLVED/DISMISSED) |
| PATCH | `/api/admin/reports/{id}` | `{status: RESOLVED\|DISMISSED}` |
| GET | `/api/admin/audit?page=&size=` | Audit log of admin actions |

## Design notes

- **Role changes take effect immediately**: the JWT filter reads the user's current role from
  the database on every request, so a demoted admin loses access even with an unexpired token,
  and a deleted user's token stops working.
- **Canonical option order is stored; shuffling happens at serve time.** Grading compares the
  submitted canonical index against the stored correct option, so shuffling never affects
  correctness.
- **Soft delete** keeps foreign-key integrity for past interview sessions that reference a
  question.
- The repository layout:

  ```
  src/main/java/com/interviewsimulator/
    entity/ repository/ service/ security/ dto/ web/
  src/main/resources/db/changelog/   Liquibase changelogs + seed CSVs
  frontend/                          React + TypeScript SPA (Vite)
  ```
