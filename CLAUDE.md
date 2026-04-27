## Project Overview

School information API server for Gwangju Software Meister High School (students, clubs, meals, schedules)

## Module Structure

- datagsm-common: Shared Entity/DTO/Repository, Health API
- datagsm-oauth-authorization: OAuth2 authentication, account lifecycle (signup, password reset)
- datagsm-oauth-userinfo: OAuth2 UserInfo endpoint (external clients)
- datagsm-openapi: Public read-only API (students, clubs, NEIS integration)
- datagsm-web: Web service API (user features, admin features, Excel processing)

**Note**: `/v1/health` endpoint is provided by `HealthController` in `datagsm-common` module and is shared across all modules.

## Commands

- Build: `./gradlew build`
- Test: `./gradlew test`
- Format: `./gradlew ktlintFormat`
- Run: `./gradlew :<module>:bootRun` (modules: `datagsm-oauth-authorization`, `datagsm-openapi`, `datagsm-oauth-userinfo`, `datagsm-web`)

## Tech Stack

Kotlin, Spring Boot 4.0, Spring Data JPA, QueryDSL, Redis, MySQL

## Coding Rules

- Controller → Service → Repository pattern
- Use constructor injection
- Test: Kotest + MockK (Given-When-Then)
- Do NOT add excessive comments - only add comments where logic is not self-evident

Detailed rules are split into `.claude/rules/`:
- `dto-annotations.md` — `@field:` vs `@param:` rules for Jackson and Swagger
- `logging.md` — English-only, SLF4J `{}` placeholders, no colon separators
- `exception.md` — `ExpectedException` usage and message format
- `kotlin-style.md` — `val/var`, constructor injection, null safety
- `api-conventions.md` — `@RequestParam` vs `@ModelAttribute`, DTO naming, `@Transactional` placement
- `commit-conventions.md` — commit type/scope rules

## Context Compaction Rules

Priority order when compressing conversation history:
1. Project Overview (module structure)
2. Common Mistakes section
3. DTO Annotations rules
4. Commit/PR conventions
5. Tech Stack
6. Reduce: Commands, Key Paths

## Key Paths

- Common Entity: `datagsm-common/src/main/kotlin/.../domain/`
- Exception Handler: `datagsm-common/.../global/common/error/`
- API Response: Use `CommonApiResponse` wrapper
