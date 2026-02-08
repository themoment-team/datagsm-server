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

- **Jackson**: Always use `@field:JsonProperty`, `@field:JsonAlias` (not `@param:`)
- **Swagger**: Request DTO uses `@param:Schema`, Response DTO uses `@field:Schema`
- Example:
  ```kotlin
  // Request DTO
  data class CreateReqDto(
      @param:Schema(description = "Name")
      @field:JsonProperty("user_name")
      val userName: String
  )

  // Response DTO
  data class UserResDto(
      @field:Schema(description = "ID")
      @field:JsonProperty("user_id")
      val userId: Long
  )
  ```

## Common Mistakes (이런 실수를 피하세요!)

### DTO Annotations
- ❌ WRONG: `@param:JsonProperty` → ✅ CORRECT: `@field:JsonProperty`
- ❌ WRONG: Response DTO에 `@param:Schema` → ✅ CORRECT: `@field:Schema`

### Commit Scope
- ❌ WRONG: `fix(web):` (모듈명) → ✅ CORRECT: `fix(auth):` (도메인명)
- ❌ WRONG: `update(common):` → ✅ CORRECT: `update(student):`
- Only use module names for cross-cutting concerns: `refactor(global):`, `update(ci/cd):`

### Kotlin Style
- ❌ WRONG: `var` 남용 → ✅ CORRECT: `val` 우선
- ❌ WRONG: field injection → ✅ CORRECT: constructor injection
- ❌ WRONG: 과도한 주석 → ✅ CORRECT: 로직이 명확하지 않은 곳만 주석

## Context Compaction Rules

대화 압축 시 우선순위 (중요도 순):
1. Project Overview (모듈 구조)
2. Common Mistakes 섹션
3. DTO Annotations 규칙
4. Commit/PR 컨벤션
5. Tech Stack
6. Reduce: Commands, Key Paths

## 상세 가이드 (Skills)

더 자세한 내용은 해당 skill을 실행하세요:
- **kotlin-spring-arch**: 상세 레이어 구조, 트랜잭션 전략, 예외 처리 패턴
- **kotest-guide**: Kotest + MockK Given-When-Then 패턴, 코루틴 테스트
- **code-review**: 체크리스트 기반 자동 검증
- **security-checklist**: 하드코딩 시크릿, SQL Injection, JWT 검증 체크

## Key Paths

- Common Entity: `datagsm-common/src/main/kotlin/.../domain/`
- Exception Handler: `datagsm-common/.../global/common/error/`
- API Response: Use `CommonApiResponse` wrapper
