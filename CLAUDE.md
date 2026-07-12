# Project conventions

## Roadmap priority (owner decision, 2026-07-12)

1. Turkish language support (UI + question translations)
2. Question banks for new subjects (SQL, frontend, data analysis, AI, Excel, SAP, math)
3. Optional features (Google OAuth, password reset, leaderboard, JUnit tests)
4. Java 25 upgrade (see below) — **deprioritized to last by the owner**

## Java version policy

**Minimum Java 25** (owner requirement, 2026-07) — scheduled as roadmap item 4, not before.

The codebase currently targets Java 21. The upgrade steps:

1. Gradle wrapper → 9.x (`gradle/wrapper/gradle-wrapper.properties`, Java 25 toolchains need Gradle 9+)
2. Spring Boot → 3.5.x or newer (Java 25 support)
3. `build.gradle` toolchain `languageVersion` → 25 (Foojay resolver already configured in
   `settings.gradle`, so the JDK auto-downloads on developer machines)
4. Full verification: `./gradlew build` + end-to-end run against PostgreSQL before pushing

Note: the remote sandbox's network policy blocks downloads of Gradle distributions and JDKs
(only Java 21 + Gradle 8.14.3 are preinstalled there), so this upgrade must be verified on a
machine with normal network access, or the sandbox environment must be updated first.

## Other conventions

- Build: Gradle (not Maven). DB migrations: Liquibase (not Flyway). DB: PostgreSQL.
- Frontend: React + TypeScript (Vite) in `frontend/`, built into the boot jar by Gradle;
  all UI strings live in `frontend/src/i18n/strings.ts` (Turkish support planned).
- Questions keep canonical option order in the DB; shuffling happens per-request at serve time.
- Question deletion is always soft delete (`question.active`).
- Admin actions must be written to the audit log.
