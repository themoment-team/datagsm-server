# DataGSM Copilot Instructions

**Please provide all code suggestions, reviews, and responses in Korean (한국어).**

## Project Overview

DataGSM is a Spring Boot REST API service providing school information (students, clubs, meals, schedules) for Gwangju Software Meister High School. Uses Google OAuth2 authentication with JWT token and API key management.

## Tech Stack

- Kotlin with Spring Boot 4.0, Spring Security, Spring Data JPA
- MySQL (main data), Redis (caching, sessions)
- QueryDSL for complex queries, OpenFeign for external APIs
- Jackson 3.0 for JSON processing
- Kotest + MockK + JUnit 5 for testing (Given-When-Then style)

## Project Structure (Multi-module)

- `datagsm-common/`: Shared library (Entity, DTO, Repository, Config, Health API)
- `datagsm-oauth-authorization/`: OAuth2 authentication, account lifecycle (signup, password reset)
- `datagsm-oauth-userinfo/`: OAuth2 UserInfo endpoint (external clients)
- `datagsm-openapi/`: Public read-only API (students, clubs, NEIS)
- `datagsm-web/`: Web service API (user features, admin features, Excel)
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

### DTO Annotations

- **Jackson**: Always `@field:JsonProperty`, `@field:JsonAlias` (not `@param:`)
- **Swagger**: Request DTO → `@param:Schema`, Response DTO → `@field:Schema`
- See CONTRIBUTING.md for examples

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

- Security: No hardcoded secrets, use SLF4J Logger (with Logback, not println()), validate JWT/API keys properly
- JPA: Avoid N+1 problems, use `@Transactional(readOnly = true)` for queries
- API: Use `CommonApiResponse` wrapper, validate with `@Valid`
- Testing: Write Kotest tests for business logic using Given-When-Then pattern
- Exceptions: Use `ExpectedException` for custom exceptions with appropriate HTTP status. Message must be Korean (합쇼체) ending with a period, no dynamic data. Example: `ExpectedException("학생을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)`
- Logging: English only, verb-led sentences, SLF4J `{}` placeholder (not string interpolation, no colon separators). Example: `logger().error("Failed to process {}", message)`

## Common Mistakes (Avoid These!)

### DTO Annotations
- WRONG: `@param:JsonProperty` → CORRECT: `@field:JsonProperty`
- WRONG: Response DTO with `@param:Schema` → CORRECT: `@field:Schema`

### Commit Scope
- WRONG: `fix(web):` (module name) → CORRECT: `fix(auth):` (domain name)
- Domain names first: account, application, auth, client, club, neis, oauth, project, student, utility
- Module names only for cross-cutting: global, ci/cd
