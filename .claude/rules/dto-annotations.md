# DTO Annotation Rules

## Jackson Serialization

Always use `@field:` target — never `@param:`:

```kotlin
// CORRECT
@field:JsonProperty("user_name")
@field:JsonAlias("userName")
val userName: String

// WRONG
@param:JsonProperty("user_name")  // Jackson ignores this
val userName: String
```

## Swagger / OpenAPI

- **Request DTOs** (`*ReqDto`): use `@param:Schema`
- **Response DTOs** (`*ResDto`): use `@field:Schema`

```kotlin
// Request DTO
data class CreateStudentReqDto(
    @param:Schema(description = "Student name")
    @field:JsonProperty("student_name")
    val studentName: String
)

// Response DTO
data class StudentResDto(
    @field:Schema(description = "Student ID")
    @field:JsonProperty("student_id")
    val studentId: Long
)
```

## Common Mistakes

- WRONG: `@param:JsonProperty` → CORRECT: `@field:JsonProperty`
- WRONG: Response DTO with `@param:Schema` → CORRECT: `@field:Schema`