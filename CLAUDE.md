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

## Password reset

`EmailSender` is a pluggable interface; `ConsoleEmailSender` (the only implementation so far) just
logs the reset link instead of sending it. Swap in an SMTP-backed implementation
(`spring-boot-starter-mail` + `JavaMailSender`) once real mail credentials are available — no other
code needs to change.

## Other conventions

- Build: Gradle (not Maven). DB migrations: Liquibase (not Flyway). DB: PostgreSQL.
- Frontend: React + TypeScript (Vite) in `frontend/`, built into the boot jar by Gradle;
  all UI strings live in `frontend/src/i18n/strings.ts` (Turkish support planned).
- Questions keep canonical option order in the DB; shuffling happens per-request at serve time.
- Question deletion is always soft delete (`question.active`).
- Admin actions must be written to the audit log.
