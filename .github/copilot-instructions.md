# DataGSM Copilot Instructions

**Please provide all code suggestions, reviews, and responses in Korean (한국어).**

## Project Overview
DataGSM is a Spring Boot REST API service providing school information (students, clubs, meals, schedules) for Gwangju Software Meister High School. Uses Google OAuth2 authentication with JWT token and API key management.

## Tech Stack
- Kotlin with Spring Boot 3.x, Spring Security, Spring Data JPA
- MySQL (main data), Redis (caching, sessions)
- QueryDSL for complex queries, OpenFeign for external APIs
- Kotest + MockK for testing

## Project Structure
- `domain/`: Domain modules (auth, student, club, neis, project)
  - Each domain: `controller/`, `service/`, `repository/`, `entity/`, `dto/`
- `global/`: Cross-cutting concerns (config, security, exception, thirdparty)

## Coding Conventions
- Use Kotlin idioms: `val` over `var`, null safety, extension functions
- Naming: PascalCase (classes), camelCase (functions/variables), Request/Response DTO suffix (e.g., `UserReqDto`, `UserResDto`)
- Follow KtLint rules
- Architecture: Controller → Service (interface + impl) → Repository pattern
- Always use constructor injection for dependencies
- Separate Entity and DTO clearly

## Key Practices
- Security: No hardcoded secrets, use SLF4J Logger (with Logback, not println), validate JWT/API keys properly
- JPA: Avoid N+1 problems, use `@Transactional(readOnly = true)` for queries
- API: Use `CommonApiResponse` wrapper, validate with `@Valid`
- Testing: Write Kotest tests for business logic using Given-When-Then pattern
- Exceptions: Use `ExpectedException` for custom exceptions with appropriate HTTP status
