---
description: Kotlin + Spring Boot 4.0 architecture detailed guide
---

# Kotlin + Spring Boot Architecture Guide

## Layer Structure

### Controller
- Role: Request validation, DTO conversion, HTTP response
- Annotations: `@RestController`, `@RequestMapping`
- Validation: `@Valid`, `@Validated`
- Response: Use `CommonApiResponse` wrapper

### Service
- Role: Business logic, transaction management
- Pattern: interface + implementation
- Transaction:
  - Read: `@Transactional(readOnly = true)`
  - Write: `@Transactional`
- Dependencies: Inject Repository via constructor injection

### Repository
- Role: Data access
- JPA: Extend `JpaRepository`
- QueryDSL: Complex queries
- Avoid N+1: Fetch Join, `@EntityGraph`

## Transaction Strategy

### Read-only Optimization
```kotlin
@Transactional(readOnly = true)
fun findApiKeys(): List<ApiKeyResDto> {
    return repository.findAll()
        .map { it.toResDto() }
}
```

### N+1 Problem Resolution
```kotlin
// ❌ N+1 occurs
repository.findAll() // 1 query
entity.relatedEntity // N queries

// ✅ Fetch Join
@Query("SELECT e FROM Entity e JOIN FETCH e.relatedEntity")
fun findAllWithRelated(): List<Entity>
```

## Exception Handling

### Custom Exception
```kotlin
class ApiKeyNotFoundException : ExpectedException(
    status = HttpStatus.NOT_FOUND,
    message = "API key not found"
)
```

### Global Handler
See `datagsm-common/.../global/common/error/GlobalExceptionHandler.kt`

## DTO Conversion Pattern

```kotlin
// Entity → ResDto
fun Entity.toResDto() = EntityResDto(
    id = this.id,
    name = this.name
)

// ReqDto → Entity
fun EntityReqDto.toEntity() = Entity(
    name = this.name
)
```
