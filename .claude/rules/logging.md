# Logging Rules

## Style

- **Language**: English only — verb-led sentences
- **Format**: SLF4J `{}` placeholder only — no Kotlin string interpolation
- **Pattern**: `"<Verb> <subject/context> {}"`, value — no colon separators

```kotlin
// CORRECT
logger().info("Deleted {} expired API keys", deletedCount)
logger().error("Failed to issue OAuth token for scopeStr {}", scopeStr)
logger().warn("ExpectedException occurred with message {}", ex.message)

// WRONG
logger().error("에러 발생: $message")             // Korean + interpolation
logger().error("Failed to process: {}", message)  // colon separator
logger().error("Error occurred: ${ex.message}")   // string interpolation
```

## Common Mistakes

- WRONG: `logger().error("에러 발생: $message")` → CORRECT: `logger().error("Failed to process {}", message)`
- No colon separators between subject and `{}`
- No Korean characters in log messages
- Never use `println()` — use SLF4J Logger only