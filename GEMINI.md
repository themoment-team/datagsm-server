# DataGSM Server

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
├── datagsm-common/       # Shared library (Entity, DTO, Repository, Config)
├── datagsm-authorization/ # OAuth2 authentication server
├── datagsm-resource/      # Resource API server (students, clubs, NEIS)
└── datagsm-web/           # Admin web API server (Excel processing)
```

Each module follows: `controller/`, `service/`, `repository/`, `entity/`, `dto/`

## Commands

- Build: `./gradlew build`
- Test: `./gradlew test`
- Format: `./gradlew ktlintFormat`
- Run: `./gradlew :datagsm-{module}:bootRun`

## Coding Conventions

### Kotlin Style
- Use Kotlin idioms: `val` over `var`, null safety, extension functions
- Naming: PascalCase (classes), camelCase (functions/variables)
- DTO naming: Request/Response suffix (e.g., `UserReqDto`, `UserResDto`)
- Follow KtLint rules

### Architecture Pattern
- **Layer Structure**: Controller → Service (interface + impl) → Repository
- **Dependency Injection**: Always use constructor injection
- **Entity vs DTO**: Separate Entity and DTO clearly
- **Comments**: Do NOT add excessive comments - only where logic is not self-evident

### Commit & PR Conventions

**Commit Message Format**: `type(scope): description`
- Types: add/update/fix/refactor/test/docs/merge
- Scopes: ONLY domain names (auth, account, student, club, project, neis, client, oauth) OR module names (web, authorization, resource, common) OR global
- Description: Korean, lowercase start, no period, avoid noun-ending style

**PR Title Format**: `[scope] description`
- Examples: `[global] 기여자 지침 문서 추가`, `[student] 졸업생 전환 및 저장 기능 구현`

## Key Practices

### Security
- No hardcoded secrets
- Use SLF4J Logger with Logback (not println())
- Validate JWT/API keys properly

### JPA
- Avoid N+1 problems
- Use `@Transactional(readOnly = true)` for read operations

### API
- Use `CommonApiResponse` wrapper for all responses
- Validate request DTOs with `@Valid`

### Testing
- Write Kotest tests for business logic
- Use Given-When-Then pattern
- Use MockK for mocking

### Exception Handling
- Use `ExpectedException` for custom exceptions
- Map to appropriate HTTP status codes
- Exception Handler: `datagsm-common/.../global/common/error/`

## Key Paths

- Common Entity: `datagsm-common/src/main/kotlin/.../domain/`
- Exception Handler: `datagsm-common/.../global/common/error/`
- API Response: Use `CommonApiResponse` wrapper

## Custom Commands

Use the following slash commands for common tasks:
- `/pr-draft` - Generate PR title suggestions and body
- `/format` - Run KtLint formatting
- `/commit` - Create Git commits by splitting changes into logical units

## Notes

- This project uses Java 24 for Gradle builds
- Always check `.gitignore` and `.geminiignore` when suggesting file changes
- When analyzing code, consider the multi-module structure