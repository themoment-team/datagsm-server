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
- Run: `./gradlew :datagsm-oauth-authorization:bootRun`
- Run: `./gradlew :datagsm-openapi:bootRun`
- Run: `./gradlew :datagsm-oauth-userinfo:bootRun`
- Run: `./gradlew :datagsm-web:bootRun`

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

**Guidelines:**
- **1-2 simple parameters**: Use `@RequestParam`
- **3+ parameters or validation required**: Use `@ModelAttribute` + DTO

**Examples:**
```kotlin
// 1-2 parameters → @RequestParam
@GetMapping("/scopes")
fun getScopes(@RequestParam role: AccountRole): ApiScopeListResDto

// 3+ parameters → @ModelAttribute + DTO
@GetMapping("/students")
fun getStudents(@Valid @ModelAttribute queryReq: QueryStudentReqDto): StudentListResDto

data class QueryStudentReqDto(
    @field:Positive @param:Schema(description = "Student ID")
    val studentId: Long? = null,
    @field:Min(0) @param:Schema(description = "Page", defaultValue = "0")
    val page: Int = 0,
    @field:Min(1) @field:Max(1000) @param:Schema(description = "Size", defaultValue = "300")
    val size: Int = 300
)
```

### DTO Variable Naming

- **@RequestBody (Create/Update)**: Use `reqDto`
- **@ModelAttribute (Query)**: Use `queryReq`

```kotlin
@PostMapping
fun createStudent(@Valid @RequestBody reqDto: CreateStudentReqDto): StudentResDto =
    createStudentService.execute(reqDto)

@GetMapping
fun getStudents(@Valid @ModelAttribute queryReq: QueryStudentReqDto): StudentListResDto =
    queryStudentService.execute(queryReq)
```

### Controller-Service Value Passing

Pass request body/query DTO objects to service layer. PathVariable can be passed individually.

**Correct Pattern:**
```kotlin
@PostMapping
fun createStudent(@Valid @RequestBody reqDto: CreateStudentReqDto): StudentResDto =
    createStudentService.execute(reqDto)

@PutMapping("/{id}")
fun updateStudent(@PathVariable id: Long, @Valid @RequestBody reqDto: UpdateStudentReqDto): StudentResDto =
    updateStudentService.execute(id, reqDto)
```

**Wrong Pattern:**
```kotlin
@PostMapping
fun createStudent(@Valid @RequestBody reqDto: CreateStudentReqDto): StudentResDto =
    createStudentService.execute(reqDto.name, reqDto.email)  // Don't extract DTO fields
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

- This project uses Java 25 for Gradle builds
- Always check `.gitignore` and `.geminiignore` when suggesting file changes
- When analyzing code, consider the multi-module structure
