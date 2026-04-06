# API Conventions

## Query Parameter Binding

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

## DTO Variable Naming

- **`@RequestBody`** (Create/Update): `reqDto` → `service.execute(reqDto)`
- **`@ModelAttribute`** (Query): `queryReq` → `service.execute(queryReq)`
- **`@ModelAttribute`** (Search): `searchReq` (when search intent is clear)

## Controller → Service Passing

Pass DTO objects as-is. `@PathVariable` values can be passed individually.

```kotlin
@PostMapping
fun createStudent(@Valid @RequestBody reqDto: CreateStudentReqDto): StudentResDto =
    createStudentService.execute(reqDto)

@PutMapping("/{id}")
fun updateStudent(@PathVariable id: Long, @Valid @RequestBody reqDto: UpdateStudentReqDto): StudentResDto =
    updateStudentService.execute(id, reqDto)
```

## Transaction Rules

- `@Transactional` must be at **method level** — never class level
- Read operations: `@Transactional(readOnly = true)`
- Write operations: `@Transactional`

```kotlin
// CORRECT
@Transactional(readOnly = true)
override fun execute(queryReq: QueryStudentReqDto): StudentListResDto { ... }

// WRONG
@Service
@Transactional  // class-level is forbidden
class StudentServiceImpl { ... }
```