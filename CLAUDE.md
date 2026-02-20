# DataGSM Server

**Please respond and work in Korean.**

## Project Overview

School information API server for Gwangju Software Meister High School (students, clubs, meals, schedules)

## Module Structure

- datagsm-common: Shared Entity/DTO/Repository, Health API
- datagsm-oauth-authorization: OAuth2 authentication, account lifecycle (signup, password reset)
- datagsm-oauth-userinfo: OAuth2 UserInfo endpoint (external clients)
- datagsm-openapi: Public read-only API (students, clubs, NEIS integration)
- datagsm-web: Web service API (user features, admin features, Excel processing)

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

### DTO Annotations

- **Jackson**: Always `@field:JsonProperty`, `@field:JsonAlias` (not `@param:`)
- **Swagger**: Request DTO → `@param:Schema`, Response DTO → `@field:Schema`
- See CONTRIBUTING.md for examples

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

**Good:**
```kotlin
@PostMapping
fun createStudent(@Valid @RequestBody reqDto: CreateStudentReqDto): StudentResDto =
    createStudentService.execute(reqDto)

@PutMapping("/{id}")
fun updateStudent(@PathVariable id: Long, @Valid @RequestBody reqDto: UpdateStudentReqDto): StudentResDto =
    updateStudentService.execute(id, reqDto)
```

**Bad:**
```kotlin
@PostMapping
fun createStudent(@Valid @RequestBody reqDto: CreateStudentReqDto): StudentResDto =
    createStudentService.execute(reqDto.name, reqDto.email)  // Don't extract DTO fields
```

## Common Mistakes

### DTO Annotations
- WRONG: `@param:JsonProperty` → CORRECT: `@field:JsonProperty`
- WRONG: Response DTO with `@param:Schema` → CORRECT: `@field:Schema`

### Commit Scope
- WRONG: `fix(web):` (module name) → CORRECT: `fix(auth):` (domain name)
- WRONG: `update(common):` → CORRECT: `update(student):`
- Only use module names for cross-cutting concerns: `refactor(global):`, `update(ci/cd):`

### Kotlin Style
- WRONG: Overusing `var` → CORRECT: Prefer `val`
- WRONG: Field injection → CORRECT: Constructor injection
- WRONG: Excessive comments → CORRECT: Comment only non-obvious logic

## Context Compaction Rules

Priority order when compressing conversation history:
1. Project Overview (module structure)
2. Common Mistakes section
3. DTO Annotations rules
4. Commit/PR conventions
5. Tech Stack
6. Reduce: Commands, Key Paths

## Detailed Guides (Skills)

For more details, execute the corresponding skill:
- **kotlin-spring-arch**: Detailed layer structure, transaction strategy, exception handling patterns
- **kotest-guide**: Kotest + MockK Given-When-Then pattern, coroutine testing
- **code-review**: Checklist-based automatic validation
- **security-checklist**: Hardcoded secrets, SQL Injection, JWT validation checks

## Key Paths

- Common Entity: `datagsm-common/src/main/kotlin/.../domain/`
- Exception Handler: `datagsm-common/.../global/common/error/`
- API Response: Use `CommonApiResponse` wrapper
