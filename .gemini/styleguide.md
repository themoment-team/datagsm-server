## Core Principles

1. **Clarity over Cleverness**: Write code that is easy to understand and maintain
2. **Consistency**: Follow established patterns throughout the codebase
3. **Type Safety**: Leverage Kotlin's type system and null safety features
4. **Separation of Concerns**: Maintain clear boundaries between layers
5. **Minimal Comments**: Code should be self-documenting; add comments only for non-obvious logic

## Naming Conventions

### Services

**Pattern**: `{Action}{Domain}Service`

Services are named with an action verb followed by the domain entity they operate on.

**Examples:**
- `CreateStudentService` - Creates a student
- `QueryStudentService` - Queries students
- `UpdateStudentService` - Updates a student
- `DeleteStudentService` - Deletes a student
- `SearchApiKeyService` - Searches API keys
- `ModifyClubService` - Modifies a club

**Implementation:**
- Interface: `CreateStudentService`
- Implementation: `CreateStudentServiceImpl` (in `impl/` package)

### Controllers

**Pattern**: `{Domain}Controller`

Controllers are named after the domain they manage, without action verbs.

**Examples:**
- `StudentController` - Handles all student-related endpoints
- `ClubController` - Handles all club-related endpoints
- `AuthController` - Handles all authentication endpoints
- `ProjectController` - Handles all project-related endpoints

### Request/Response DTOs

**Pattern**: `{Action}{Domain}{Req|Res}Dto`

DTOs are named with an optional action, the domain, and a suffix indicating direction.

**Action is Optional**: Omit when the DTO is used across multiple actions or is generic.

**Request DTO Examples:**
- `CreateStudentReqDto` - Request for creating a student
- `QueryStudentReqDto` - Request for querying students
- `UpdateStudentReqDto` - Request for updating a student
- `SearchApiKeyReqDto` - Request for searching API keys
- `StudentReqDto` - Generic student request (action omitted)

**Response DTO Examples:**
- `StudentResDto` - Single student response
- `StudentListResDto` - List of students response
- `ApiKeyResDto` - API key response
- `ClubResDto` - Club response

### Entities

**Pattern**: `{Domain}{DatabaseType}Entity`

Entities are named with the domain and the database technology they belong to.

**Examples:**
- `StudentJpaEntity` - JPA entity for student
- `ClubJpaEntity` - JPA entity for club
- `AccountJpaEntity` - JPA entity for account
- `ApiKeyRedisEntity` - Redis entity for API key (if used)

**Note**: Most entities use JPA, so the suffix is typically `JpaEntity`.

### Repositories

**Pattern**: `{Domain}{DatabaseType}Repository`

Repositories follow the same pattern as entities.

**Examples:**
- `StudentJpaRepository` - JPA repository for student
- `ClubJpaRepository` - JPA repository for club
- `AccountJpaRepository` - JPA repository for account
- `ApiKeyRedisRepository` - Redis repository for API key (if used)

## Query Parameter Binding

Choose the appropriate method based on parameter count and validation needs.

### Use `@RequestParam` for 1-2 Simple Parameters

```kotlin
// Single parameter
@GetMapping("/scopes/{scopeName}")
fun getApiScope(
    @PathVariable scopeName: String
): ApiScopeResDto

// Two parameters
@GetMapping("/available-scopes")
fun getApiScopes(
    @RequestParam role: AccountRole,
    @RequestParam(required = false, defaultValue = "false") includeDeprecated: Boolean
): ApiScopeGroupListResDto
```

### Use `@ModelAttribute` + DTO for 3+ Parameters or Validation

Variable naming: use `queryReq` for general queries, `searchReq` when search intent is clear.

```kotlin
@GetMapping("/students")
fun getStudentInfo(
    @Valid @ModelAttribute queryReq: QueryStudentReqDto
): StudentListResDto

data class QueryStudentReqDto(
    @field:Positive
    @param:Schema(description = "Student ID")
    val studentId: Long? = null,

    @field:Min(1) @field:Max(3)
    @param:Schema(description = "Grade (1-3)")
    val grade: Int? = null,

    @field:Min(0)
    @param:Schema(description = "Page number", defaultValue = "0")
    val page: Int = 0,

    @field:Min(1) @field:Max(1000)
    @param:Schema(description = "Page size", defaultValue = "300")
    val size: Int = 300
)
```

**Benefits:**
- Jakarta Bean Validation with `@Valid`
- Improved readability (14 parameters → 1 DTO)
- Better maintainability
- Backward compatible (same query string parameter names)

## DTO Annotations

### Jackson Serialization

Always use `@field:` target for Jackson annotations, never `@param:`.

```kotlin
// CORRECT
data class UserReqDto(
    @field:JsonProperty("user_name")
    @field:JsonAlias("userName")
    val userName: String
)

// WRONG
data class UserReqDto(
    @param:JsonProperty("user_name")  // Jackson ignores this
    val userName: String
)
```

### Swagger Documentation

- **Request DTOs**: Use `@param:Schema`
- **Response DTOs**: Use `@field:Schema`

```kotlin
// Request DTO
data class CreateStudentReqDto(
    @param:Schema(description = "Student name", example = "John Doe")
    @field:JsonProperty("student_name")
    val studentName: String
)

// Response DTO
data class StudentResDto(
    @field:Schema(description = "Student ID", example = "1")
    @field:JsonProperty("student_id")
    val studentId: Long
)
```

## Architecture Patterns

### Layer Structure

Follow a strict three-layer architecture:

```
Controller → Service → Repository
```

**Controller Responsibilities:**
- HTTP request/response handling
- Input validation (`@Valid`)
- Route mapping
- Return DTOs wrapped in `CommonApiResponse`

**Service Responsibilities:**
- Business logic
- Transaction management
- Entity ↔ DTO conversion
- Orchestrate multiple repositories

**Repository Responsibilities:**
- Data access only
- QueryDSL complex queries
- No business logic

### Example

```kotlin
// Controller
@RestController
@RequestMapping("/v1/students")
class StudentController(
    private val createStudentService: CreateStudentService
) {
    @PostMapping
    fun createStudent(
        @Valid @RequestBody reqDto: CreateStudentReqDto
    ): CommonApiResponse<StudentResDto> {
        val result = createStudentService.execute(reqDto)
        return CommonApiResponse.success(result)
    }
}

// Service Interface
interface CreateStudentService {
    fun execute(reqDto: CreateStudentReqDto): StudentResDto
}

// Service Implementation
@Service
class CreateStudentServiceImpl(
    private val studentRepository: StudentJpaRepository
) : CreateStudentService {

    @Transactional
    override fun execute(reqDto: CreateStudentReqDto): StudentResDto {
        val student = StudentJpaEntity(
            name = reqDto.name,
            email = reqDto.email
        )
        val saved = studentRepository.save(student)
        return StudentResDto.from(saved)
    }
}

// Repository
interface StudentJpaRepository : JpaRepository<StudentJpaEntity, Long> {
    fun findByEmail(email: String): StudentJpaEntity?
}
```

## Dependency Injection

Always use constructor injection, never field injection.

```kotlin
// CORRECT: Constructor injection
@Service
class StudentService(
    private val studentRepository: StudentJpaRepository,
    private val clubRepository: ClubJpaRepository
)

// WRONG: Field injection
@Service
class StudentService {
    @Autowired
    lateinit var studentRepository: StudentJpaRepository
}
```

## Kotlin Style

### Prefer `val` over `var`

```kotlin
// CORRECT
val student = studentRepository.findById(id).orElseThrow()

// WRONG
var student = studentRepository.findById(id).orElseThrow()
```

### Null Safety

Use Kotlin's null safety features instead of throwing exceptions.

```kotlin
// CORRECT
fun findStudent(id: Long): Student? {
    return repository.findById(id).orElse(null)
}

val name = student?.name ?: "Unknown"

// WRONG
fun findStudent(id: Long): Student {
    return repository.findById(id).get()  // Can throw NoSuchElementException
}
```

### Type Inference

Use explicit types for public APIs, allow inference for local variables.

```kotlin
// Public API - explicit types
interface StudentService {
    fun execute(reqDto: CreateStudentReqDto): StudentResDto
}

// Local variables - inference allowed
fun processStudent() {
    val students = repository.findAll()  // Type inferred
    val count = students.size            // Type inferred
}
```

## Code Formatting

- **Tool**: KtLint with project configuration
- **Indentation**: 4 spaces (no tabs)
- **Line Length**: Maximum 120 characters
- **Import Order**: Alphabetical, with blank lines between groups
- **Format Command**: `./gradlew ktlintFormat`

## Error Handling

### Custom Exceptions

Use `ExpectedException` base class for all custom exceptions.

```kotlin
class StudentNotFoundException(
    message: String = "Student not found"
) : ExpectedException(
    status = HttpStatus.NOT_FOUND,
    message = message
)

class DuplicateStudentException(
    message: String = "Student already exists"
) : ExpectedException(
    status = HttpStatus.CONFLICT,
    message = message
)
```

### Exception Handler

All exceptions are caught by `GlobalExceptionHandler` in `datagsm-common` module.

```kotlin
@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(StudentNotFoundException::class)
    fun handleStudentNotFound(ex: StudentNotFoundException): CommonApiResponse<Nothing> {
        return CommonApiResponse.error(
            status = HttpStatus.NOT_FOUND,
            message = ex.message
        )
    }
}
```

## Logging

### Use SLF4J with Logback

Never use `println()` for logging.

```kotlin
// CORRECT
@Service
class StudentService {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun execute(reqDto: CreateStudentReqDto): StudentResDto {
        logger.info("Creating student: {}", reqDto.name)
        // ...
    }
}

// WRONG
fun execute(reqDto: CreateStudentReqDto) {
    println("Creating student: ${reqDto.name}")  // Never do this
}
```

### Log Levels

- `ERROR`: Unrecoverable errors
- `WARN`: Recoverable errors or unexpected states
- `INFO`: Important business events
- `DEBUG`: Detailed diagnostic information
- `TRACE`: Very detailed diagnostic information

## Testing

### Framework

Use Kotest with MockK for all tests.

```kotlin
class CreateStudentServiceTest : DescribeSpec({
    lateinit var mockRepository: StudentJpaRepository
    lateinit var service: CreateStudentService

    beforeEach {
        mockRepository = mockk()
        service = CreateStudentServiceImpl(mockRepository)
    }

    describe("CreateStudentService") {
        describe("execute") {
            context("when creating a new student") {
                it("should save and return the student") {
                    // Given
                    val reqDto = CreateStudentReqDto(
                        name = "John Doe",
                        email = "john@example.com"
                    )
                    val savedEntity = StudentJpaEntity(
                        id = 1L,
                        name = reqDto.name,
                        email = reqDto.email
                    )

                    every { mockRepository.save(any()) } returns savedEntity

                    // When
                    val result = service.execute(reqDto)

                    // Then
                    result.id shouldBe 1L
                    result.name shouldBe "John Doe"
                    verify(exactly = 1) { mockRepository.save(any()) }
                }
            }

            context("when student already exists") {
                it("should throw DuplicateStudentException") {
                    // Given
                    val reqDto = CreateStudentReqDto(
                        name = "John Doe",
                        email = "john@example.com"
                    )

                    every { mockRepository.existsByEmail(reqDto.email) } returns true

                    // When & Then
                    shouldThrow<DuplicateStudentException> {
                        service.execute(reqDto)
                    }
                }
            }
        }
    }
})
```

### Test Structure

- Use Given-When-Then pattern
- One assertion per test
- Clear test names in Korean
- Mock external dependencies
- Use `beforeEach` for setup, `afterEach` for cleanup

## Security

### No Hardcoded Secrets

```kotlin
// CORRECT
@Value("\${jwt.secret}")
private lateinit var jwtSecret: String

// WRONG
private val jwtSecret = "my-secret-key-123"  // Never do this
```

### SQL Injection Prevention

Always use parameterized queries or QueryDSL.

```kotlin
// CORRECT (QueryDSL)
fun findByName(name: String): List<StudentJpaEntity> {
    return queryFactory
        .selectFrom(student)
        .where(student.name.eq(name))
        .fetch()
}

// CORRECT (JPA Repository)
fun findByName(name: String): List<StudentJpaEntity>

// WRONG (SQL Injection risk)
@Query("SELECT * FROM student WHERE name = '$name'")  // Never do this
fun findByName(name: String): List<StudentJpaEntity>
```

## Common Mistakes to Avoid

### DTO Annotations
- WRONG: `@param:JsonProperty` → CORRECT: `@field:JsonProperty`
- WRONG: Response DTO with `@param:Schema` → CORRECT: `@field:Schema`

### Kotlin Style
- WRONG: Overusing `var` → CORRECT: Prefer `val`
- WRONG: Field injection → CORRECT: Constructor injection
- WRONG: Excessive comments → CORRECT: Comment only non-obvious logic

### Transaction Management
- WRONG: `@Transactional` on repository → CORRECT: `@Transactional` on service
- WRONG: `@Transactional` on class level → CORRECT: `@Transactional` on method level
- WRONG: Read operations without `readOnly = true` → CORRECT: `@Transactional(readOnly = true)`

**Apply `@Transactional` at method level, not class level**, for fine-grained control and explicit intent.

```kotlin
// CORRECT: Method-level transaction
@Service
class StudentServiceImpl(
    private val studentRepository: StudentJpaRepository
) : StudentService {

    @Transactional
    override fun createStudent(reqDto: CreateStudentReqDto): StudentResDto {
        // Write operation with transaction
    }

    @Transactional(readOnly = true)
    override fun getStudent(id: Long): StudentResDto {
        // Read operation with read-only transaction
    }
}

// WRONG: Class-level transaction
@Service
@Transactional  // Avoid this - use method level instead
class StudentServiceImpl {
    // All methods inherit the same transaction behavior
}
```

## Additional Resources

- Full conventions: `CONTRIBUTING.md`
