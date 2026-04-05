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
- Run: `./gradlew :<module>:bootRun` (modules: `datagsm-oauth-authorization`, `datagsm-openapi`, `datagsm-oauth-userinfo`, `datagsm-web`)

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

### DTO Annotations
- **Jackson**: Always use `@field:JsonProperty`, `@field:JsonAlias` (not `@param:`)
- **Swagger**: Request DTO uses `@param:Schema`, Response DTO uses `@field:Schema`
- See CONTRIBUTING.md for detailed examples

### Query Parameter Binding (@RequestParam vs @ModelAttribute)

- **1-2 simple parameters**: Use `@RequestParam`
- **3+ parameters or validation required**: Use `@ModelAttribute` + DTO

```kotlin
// 1-2 parameters → @RequestParam
@GetMapping("/scopes")
fun getScopes(@RequestParam role: AccountRole): ApiScopeListResDto

// 3+ parameters → @ModelAttribute + DTO
@GetMapping("/students")
fun getStudents(@Valid @ModelAttribute queryReq: QueryStudentReqDto): StudentListResDto
```

### DTO Variable Naming

- **@RequestBody (Create/Update)**: Use `reqDto` → `service.execute(reqDto)`
- **@ModelAttribute (Query)**: Use `queryReq` → `service.execute(queryReq)`
- **@ModelAttribute (Search)**: Use `searchReq` (검색 의미가 명확한 경우)

### Controller-Service Value Passing

Pass DTO objects to service layer as-is. PathVariable can be passed individually.

```kotlin
@PostMapping
fun createStudent(@Valid @RequestBody reqDto: CreateStudentReqDto): StudentResDto =
    createStudentService.execute(reqDto)

@PutMapping("/{id}")
fun updateStudent(@PathVariable id: Long, @Valid @RequestBody reqDto: UpdateStudentReqDto): StudentResDto =
    updateStudentService.execute(id, reqDto)
```

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
- **Message format**: Korean (합쇼체) + period, no dynamic data — messages are displayed directly to end users
  - CORRECT: `ExpectedException("학생을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)`
  - WRONG: `ExpectedException("학생을 찾을 수 없습니다. ID: $id", HttpStatus.NOT_FOUND)`

## Notes

- This project uses Java 25 for Gradle builds
- Always check `.gitignore` and `.geminiignore` when suggesting file changes
- When analyzing code, consider the multi-module structure
