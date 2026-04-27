# Exception Handling Rules

## ExpectedException Usage

Use `ExpectedException` directly — do not subclass it:

```kotlin
// CORRECT
val student = studentRepository.findById(id).orElseThrow {
    ExpectedException("학생을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
}

// WRONG — subclassing is forbidden
class StudentNotFoundException : ExpectedException(...)
```

## Message Format

- Korean (합쇼체) + period — displayed directly to end users as toast/alert
- No dynamic data (IDs, names, variables) in the message string

```kotlin
// CORRECT
ExpectedException("학생을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
ExpectedException("이미 존재하는 이메일입니다.", HttpStatus.CONFLICT)

// WRONG — dynamic data in message
ExpectedException("학생을 찾을 수 없습니다. ID: $id", HttpStatus.NOT_FOUND)
```