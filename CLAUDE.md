# Project conventions

## Roadmap priority (owner decision, 2026-07-12)

1. Turkish language support (UI + question translations) — **done** (1000/1000 questions translated)
2. Question banks for new subjects (SQL, frontend, data analysis, AI, Excel, SAP, math)
3. Optional features (Google OAuth, password reset, leaderboard, JUnit tests) — **done** (2026-07-13)
4. Java 25 upgrade (see below) — **done** (2026-07-13)

## Java version policy

**Minimum Java 25** (owner requirement, 2026-07) — completed 2026-07-13.

The codebase targets Java 25 / Spring Boot 3.5.16:

- `build.gradle` toolchain `languageVersion` = 25, Spring Boot plugin = `3.5.16`
- Gradle wrapper stays on 8.14.3 (`gradle/wrapper/gradle-wrapper.properties`) — it auto-detects
  and uses any locally installed JDK 25 as the toolchain without needing Gradle 9; no wrapper
  bump was necessary
- Foojay resolver (`settings.gradle`) auto-downloads a JDK 25 on developer machines that don't
  have one; verified in the sandbox with `apt-get install openjdk-25-jdk-headless` since direct
  JDK/Gradle-distribution downloads from services.gradle.org / adoptium / foojay are blocked by
  the sandbox's network policy (`apt` against the Ubuntu archive is not blocked)
- Verified: `gradle build` + full end-to-end run against live PostgreSQL (auth, quiz start/submit,
  meta/topics, AZ + TR locale serving) all pass on Java 25.0.3

## Google OAuth login

Fully wired but inactive without real credentials — the app starts and runs normally either way:

- Set `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET` (Google Cloud Console → OAuth client, redirect URI
  `<frontend-url>/login/oauth2/code/google`) to activate it; leave them unset to keep it off.
- `GoogleOAuthEnabledCondition` gates `GoogleOAuthClientConfig`'s `ClientRegistrationRepository` bean
  on those two env vars being non-blank (Spring Boot's own oauth2-client autoconfiguration throws a
  hard startup error on blank client-id/secret, so registration is built manually instead).
  `SecurityConfig` only calls `.oauth2Login(...)` when that bean is present.
- Session policy is STATELESS, so the in-flight authorization request can't live in an `HttpSession`
  — `CookieOAuth2AuthorizationRequestRepository` stores it in a short-lived, HTTP-only cookie instead.
- `OAuth2LoginSuccessHandler` finds-or-creates the `UserEntity` by email (new users get a random
  unusable password hash — they can only sign in via Google), issues a normal JWT, and redirects to
  `${app.frontend-url}/oauth-callback?token=...`; the frontend's `OAuthCallbackPage` reads the token,
  calls `GET /api/auth/me` to populate the session, then redirects to `/`.

## Password reset / SMTP

`EmailSender` is a pluggable interface with two implementations, chosen deterministically by a
single `@Bean` method with an if/else (`EmailConfig`) — not competing `@Conditional` beans, to
avoid any ordering ambiguity:

- No `SMTP_HOST` set → `ConsoleEmailSender` (just logs the email). This is what plain
  `gradlew bootRun` gets with no env vars, same as before.
- `SMTP_HOST` set → `SmtpEmailSender` (real `JavaMailSenderImpl`). Auth is optional — only
  applied when `SMTP_USERNAME`/`SMTP_PASSWORD` are both non-blank, since Mailpit (the local dev
  catcher wired into `docker-compose.yml`) and other unauthenticated relays don't need it, but
  most real providers (Gmail, SendGrid, ...) do.
- `SmtpEmailSender` catches `MailException` and logs instead of throwing: a transient SMTP outage
  must not turn `POST /api/auth/forgot-password` into a 500, and must not let the response shape
  leak whether the email existed (the reset token is still created either way — verified by
  starting the app with an unreachable `SMTP_HOST` and confirming both the 200 response and the
  persisted token).
- `docker-compose.yml` runs `axllent/mailpit:v1.30` (SMTP on 1025, web UI on `:8025`) and points
  the app at it by default — `docker compose up` lets you see password-reset emails at
  http://localhost:8025 with zero configuration. Override `SMTP_HOST`/`SMTP_USERNAME`/etc. in
  `.env` to send through a real provider instead.

## Docker

`Dockerfile` (multi-stage, `eclipse-temurin:25-jdk-noble` build → `eclipse-temurin:25-jre-noble`
runtime, non-root user) + `docker-compose.yml` (`app` + `db` services, `postgres:17-alpine`,
healthcheck-gated startup, named volume for data). Versions were picked as the current latest
stable as of 2026-07 (verified via web search, not guessed): Postgres 17.x (18 not released yet),
Node 24.x (current Active LTS, installed via NodeSource in the build stage since Gradle's
`buildFrontend` task shells out to `npm`), Eclipse Temurin 25 (matches the toolchain above).

Config comes from `.env` (see `.env.example`); unset variables fall back to the same insecure
dev defaults `application.yml` already uses, so `docker compose up --build` works with zero
setup — override `JWT_SECRET` etc. for anything beyond local use.

Not verified end-to-end in this sandbox: the Docker daemon isn't reachable here
(`/var/run/docker.sock` doesn't exist, and `service docker start` fails on a `ulimit` permission
error), so `docker build`/`docker compose up` could not actually be run. What *was* verified here:
`docker compose config` resolves the compose file and env-var interpolation correctly, and both
`eclipse-temurin:25-jdk-noble`/`25-jre-noble` and `postgres:17-alpine` are real, existing tags
(checked against Docker Hub). The Dockerfile's build steps (`gradlew build`, jar selection
excluding `-plain.jar`) mirror exactly what's already been verified working via plain `gradle
build` elsewhere in this doc — but the containerized build itself should get a real
`docker compose up --build` run on a machine with a working Docker daemon before relying on it.

## Other conventions

- Build: Gradle (not Maven). DB migrations: Liquibase (not Flyway). DB: PostgreSQL.
- Frontend: React + TypeScript (Vite) in `frontend/`, built into the boot jar by Gradle;
  all UI strings live in `frontend/src/i18n/strings.ts` (Turkish support planned).
- Questions keep canonical option order in the DB; shuffling happens per-request at serve time.
- Question deletion is always soft delete (`question.active`).
- Admin actions must be written to the audit log.
