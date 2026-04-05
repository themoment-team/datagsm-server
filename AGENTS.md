**한국어로 응답하고 작업해주세요 (Please respond and work in Korean).**

## Project Overview

DataGSM is a Spring Boot REST API service providing school information (students, clubs, meals, schedules) for Gwangju Software Meister High School. The system uses Google OAuth2 authentication with JWT token and API key management.

## Tech Stack

- **Backend**: Kotlin, Spring Boot 4.0, Spring Security, Spring Data JPA
- **Database**: MySQL (main data), Redis (caching, sessions)
- **Query & Integration**: QueryDSL for complex queries, OpenFeign for external APIs
- **Serialization**: Jackson 3.0 for JSON processing
- **Testing**: Kotest + MockK + JUnit 5 (Given-When-Then style)

## Project Structure (Multi-module)

```
datagsm-server/
├── datagsm-common/            # Shared library (Entity, DTO, Repository, Config, Health API)
├── datagsm-oauth-authorization/ # OAuth2 authentication, account lifecycle (signup, password reset)
├── datagsm-oauth-userinfo/    # OAuth2 UserInfo endpoint (external clients)
├── datagsm-openapi/           # Public read-only API (students, clubs, NEIS)
└── datagsm-web/               # Web service API (user features, admin features, Excel)
```

Each module follows: `controller/`, `service/`, `repository/`, `entity/`, `dto/`

**Note**: `/v1/health` endpoint is provided by `HealthController` in `datagsm-common/global/controller/` and is shared across all modules.

## Commands

- Build: `./gradlew build`
- Test: `./gradlew test`
- Format: `./gradlew ktlintFormat`
- Run: `./gradlew :<module>:bootRun`

## Coding Conventions

@.claude/rules/kotlin-style.md

@.claude/rules/dto-annotations.md

@.claude/rules/api-conventions.md

@.claude/rules/logging.md

@.claude/rules/exception.md

@.claude/rules/commit-conventions.md

## Key Practices

### Security
- No hardcoded secrets
- Validate JWT/API keys properly

### JPA
- Avoid N+1 problems
- Use `@Transactional(readOnly = true)` for read operations

### API
- Use `CommonApiResponse` wrapper for all responses
- Validate request DTOs with `@Valid`

### Testing
- Write Kotest tests for business logic
- Use Given-When-Then pattern with `DescribeSpec`
- Use MockK for mocking
- Test names in Korean: `describe("클래스명 클래스의")`, `describe("메서드명 메서드는")`

## Notes

- This project uses Java 25 for Gradle builds
- Always check `.gitignore` and `.geminiignore` when suggesting file changes
- When analyzing code, consider the multi-module structure
