# DataGSM Server

**Please respond and work in Korean.**

## Project Overview

School information API server for Gwangju Software Meister High School (students, clubs, meals, schedules)

## Module Structure

- datagsm-common: Shared Entity/DTO/Repository, Health API
- datagsm-oauth-authorization: OAuth2 authentication, account management
- datagsm-oauth-userinfo: OAuth2 user info API (profile, roles)
- datagsm-openapi: Resource API (students, clubs, NEIS integration)
- datagsm-web: Admin web API (including Excel processing)

**Note**: `/v1/health` endpoint is provided by `HealthController` in `datagsm-common` module and is shared across all modules.

## Commands

- Build: `./gradlew build`
- Test: `./gradlew test`
- Format: `./gradlew ktlintFormat`
- Run: `./gradlew :datagsm-oauth-authorization:bootRun`
- Run: `./gradlew :datagsm-openapi:bootRun`
- Run: `./gradlew :datagsm-oauth-userinfo:bootRun`
- Run: `./gradlew :datagsm-web:bootRun`

## Tech Stack

Kotlin, Spring Boot 4.0, Spring Data JPA, QueryDSL, Redis, MySQL

## Coding Rules

- Prefer `val` over `var`, use null safety
- Controller → Service → Repository pattern
- DTO suffix: ReqDto, ResDto
- Use constructor injection
- Test: Kotest + MockK (Given-When-Then)
- Do NOT add excessive comments - only add comments where logic is not self-evident

## Key Paths

- Common Entity: `datagsm-common/src/main/kotlin/.../domain/`
- Exception Handler: `datagsm-common/.../global/common/error/`
- API Response: Use `CommonApiResponse` wrapper
