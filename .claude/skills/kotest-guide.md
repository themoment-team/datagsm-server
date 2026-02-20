---
description: Kotest + MockK testing guide
---

# Kotest + MockK Testing Guide

## Given-When-Then Pattern

```kotlin
@Test
fun `create API key successfully`() = runTest {
    // Given
    val reqDto = CreateApiKeyReqDto(clientId = "test-client")
    val apiKey = ApiKey(id = 1L, key = "generated-key")

    every { repository.save(any()) } returns apiKey

    // When
    val result = service.execute(reqDto)

    // Then
    result shouldNotBe null
    result.apiKey shouldBe "generated-key"
    verify(exactly = 1) { repository.save(any()) }
}
```

## MockK Patterns

### Mock Creation
```kotlin
private val repository: ApiKeyRepository = mockk()
private val service = ApiKeyService(repository)
```

### Stubbing
```kotlin
// Regular function
every { repository.findById(1L) } returns Optional.of(apiKey)

// Coroutine
coEvery { repository.save(any()) } returns apiKey

// Any matcher
every { repository.save(any()) } returns apiKey
```

### Verification
```kotlin
// Verify call count
verify(exactly = 1) { repository.save(any()) }
verify(atLeast = 1) { repository.findAll() }

// Verify not called
verify(exactly = 0) { repository.delete(any()) }

// Coroutine
coVerify(exactly = 1) { repository.save(any()) }
```

### Argument Capture
```kotlin
val slot = slot<ApiKey>()
every { repository.save(capture(slot)) } returns apiKey

service.execute(reqDto)

slot.captured.key shouldBe "expected-key"
```

## Coroutine Testing

```kotlin
@Test
fun `test async operation`() = runTest {
    // Given
    coEvery { repository.findById(1L) } coAnswers {
        delay(100)
        Optional.of(apiKey)
    }

    // When
    val result = service.execute(1L)

    // Then
    result shouldNotBe null
}
```

## Exception Testing

```kotlin
@Test
fun `throw exception when API key not found`() = runTest {
    // Given
    every { repository.findById(1L) } returns Optional.empty()

    // When & Then
    shouldThrow<ApiKeyNotFoundException> {
        service.execute(1L)
    }
}
```

## Reference Files

Real test examples:
- `datagsm-oauth-authorization/src/test/kotlin/.../auth/service/ApiKeyServiceTest.kt`
- `datagsm-web/src/test/kotlin/.../student/service/StudentServiceTest.kt`
