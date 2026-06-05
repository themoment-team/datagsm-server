---
name: kotest-guide
description: Kotest + MockK testing patterns for this project — Given/When/Then structure, mock creation, stubbing, argument capture, coroutine testing, and exception assertions. Reference when writing or reviewing test code.
---

# Kotest + MockK Testing Guide

## Given-When-Then Pattern

Use Kotest `DescribeSpec` with `describe` / `context` / `it` blocks.

```kotlin
class CreateApiKeyServiceTest : DescribeSpec({
    lateinit var repository: ApiKeyRepository
    lateinit var service: CreateApiKeyService

    beforeEach {
        repository = mockk()
        service = CreateApiKeyServiceImpl(repository)
    }

    describe("CreateApiKeyService 클래스의") {
        describe("execute 메서드는") {
            context("유효한 요청인 경우") {
                it("API 키를 생성하고 반환한다") {
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
            }
        }
    }
})
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
context("비동기 처리가 필요한 경우") {
    it("결과를 정상적으로 반환한다") {
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
}
```

## Exception Testing

```kotlin
context("API 키가 존재하지 않는 경우") {
    it("ExpectedException을 던진다") {
        // Given
        every { repository.findById(1L) } returns Optional.empty()

        // When & Then
        shouldThrow<ExpectedException> {
            service.execute(1L)
        }
    }
}
```

## Reference Files

Discover real test examples dynamically:
```bash
find . -name "*ServiceTest.kt" ! -path "*/build/*" | head -5
```
