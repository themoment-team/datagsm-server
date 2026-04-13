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

### Kotlin Style

- Prefer `val` over `var`. Use `var` only when reassignment is strictly required.
- Always use constructor injection — never `@Autowired` field injection.
- Use Kotlin null-safety features (`?.`, `?:`) instead of `!!`.
- Do NOT add excessive comments — only where logic is not self-evident.

### DTO Annotations

- Jackson: always use `@field:` target — never `@param:` (e.g., `@field:JsonProperty("user_name")`)
- Swagger/OpenAPI:
  - Request DTOs (`*ReqDto`): use `@param:Schema`
  - Response DTOs (`*ResDto`): use `@field:Schema`

### API Conventions

- 1–2 query params: use `@RequestParam`; 3+ or with validation: use `@ModelAttribute` + DTO
- `@RequestBody` variable: `reqDto`; `@ModelAttribute` query: `queryReq`
- `@Transactional` must be at **method level only** — never class level
- Read operations: `@Transactional(readOnly = true)` / Write operations: `@Transactional`
- Use `CommonApiResponse` wrapper for all API responses

### Logging

- English only — verb-led sentences
- SLF4J `{}` placeholder only — no Kotlin string interpolation, no colon separators
- Correct: `logger().info("Deleted {} expired API keys", deletedCount)`
- Wrong: `logger().error("에러 발생: $message")` or `logger().error("Failed: {}", msg)`

### Exception Handling

- Use `ExpectedException` directly — do NOT subclass it
- Message: Korean (합쇼체) + period, no dynamic data (IDs, names, variables)
- Correct: `ExpectedException("학생을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)`
- Wrong: `ExpectedException("학생 ID: $id 없음", HttpStatus.NOT_FOUND)`

### Commit Conventions

Format: `type(scope): 설명`

- Types: `add` / `update` / `fix` / `refactor` / `ci/cd` / `docs` / `test` / `merge`
- Scope: domain name (`auth`, `student`, `club`, `application`, etc.) — NOT module names
- Cross-cutting only: `global`, `ci/cd`, or module names (`web`, `openapi`, `oauth`)
- Description: Korean, no period

## Key Practices

### JPA
- Avoid N+1 problems — use Fetch Join or `@EntityGraph`
- Use `@Transactional(readOnly = true)` for read operations

### Testing
- Write Kotest tests for business logic
- Use Kotest `DescribeSpec` with `describe/context/it` blocks
- Use MockK for mocking; Given-When-Then structure inside `it` blocks
- Test names in Korean: `describe("클래스명 클래스의")`, `describe("메서드명 메서드는")`

## Notes

- This project uses Java 25 for Gradle builds
- Always check `.gitignore` and `.geminiignore` when suggesting file changes
- When analyzing code, consider the multi-module structure
