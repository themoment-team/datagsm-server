# DataGSM Server

**Please respond and work in Korean.**

## Project Overview

School information API server for Gwangju Software Meister High School (students, clubs, meals, schedules)

## Module Structure

- datagsm-common: Shared Entity/DTO/Repository
- datagsm-authorization: OAuth2 authentication, account management
- datagsm-resource: Resource API (students, clubs, NEIS integration)
- datagsm-web: Admin web API (including Excel processing)

## Commands

- Build: `./gradlew build`
- Test: `./gradlew test`
- Format: `./gradlew ktlintFormat`
- Run: `./gradlew :datagsm-{module}:bootRun`

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
