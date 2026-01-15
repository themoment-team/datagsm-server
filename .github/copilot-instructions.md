# DataGSM Copilot Instructions

**Please provide all code suggestions, reviews, and responses in Korean (한국어).**

## Project Overview

DataGSM is a Spring Boot REST API service providing school information (students, clubs, meals, schedules) for Gwangju Software Meister High School. Uses Google OAuth2 authentication with JWT token and API key management.

## Tech Stack

- Kotlin with Spring Boot 4.0, Spring Security, Spring Data JPA
- MySQL (main data), Redis (caching, sessions)
- QueryDSL for complex queries, OpenFeign for external APIs
- Jackson 3.0 for JSON processing
- Kotest + MockK + JUnit 5 for testing

## Project Structure (Multi-module)

- `datagsm-common/`: Shared library (Entity, DTO, Repository, Config)
- `datagsm-authorization/`: OAuth2 authentication server
- `datagsm-resource/`: Resource API server (students, clubs, NEIS)
- `datagsm-web/`: Admin web API server (Excel processing)
- Each module: `controller/`, `service/`, `repository/`, `entity/`, `dto/`

## Commands

- Build: `./gradlew build`
- Test: `./gradlew test`
- Format: `./gradlew ktlintFormat`
- Run: `./gradlew :datagsm-{module}:bootRun`

## Coding Conventions

- Use Kotlin idioms: `val` over `var`, null safety, extension functions
- Naming: PascalCase (classes), camelCase (functions/variables), Request/Response DTO suffix (e.g., `UserReqDto`, `UserResDto`)
- Follow KtLint rules
- Architecture: Controller → Service (interface + impl) → Repository pattern
- Always use constructor injection for dependencies
- Separate Entity and DTO clearly
- Do NOT add excessive comments - only where logic is not self-evident

## Key Practices

- Security: No hardcoded secrets, use SLF4J Logger (with Logback, not println()), validate JWT/API keys properly
- JPA: Avoid N+1 problems, use `@Transactional(readOnly = true)` for queries
- API: Use `CommonApiResponse` wrapper, validate with `@Valid`
- Testing: Write Kotest tests for business logic using Given-When-Then pattern
- Exceptions: Use `ExpectedException` for custom exceptions with appropriate HTTP status
