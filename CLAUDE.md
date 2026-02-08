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

## Key Paths

- Common Entity: `datagsm-common/src/main/kotlin/.../domain/`
- Exception Handler: `datagsm-common/.../global/common/error/`
- API Response: Use `CommonApiResponse` wrapper
