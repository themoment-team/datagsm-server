# Kotlin Style Rules

## Immutability

Prefer `val` over `var`. Use `var` only when reassignment is strictly required (loop accumulators, Logback-injected properties).

```kotlin
// CORRECT
val student = studentRepository.findById(id).orElseThrow()

// WRONG
var student = studentRepository.findById(id).orElseThrow()
```

## Dependency Injection

Always use constructor injection — never field injection:

```kotlin
// CORRECT
@Service
class StudentService(
    private val studentRepository: StudentJpaRepository,
    private val clubRepository: ClubJpaRepository
)

// WRONG
@Service
class StudentService {
    @Autowired
    lateinit var studentRepository: StudentJpaRepository
}
```

## Comments

Do NOT add excessive comments. Only add comments where the logic is not self-evident.

## Null Safety

Use Kotlin null-safety features instead of unchecked access:

```kotlin
// CORRECT
val name = student?.name ?: "Unknown"

// WRONG
val name = student!!.name
```