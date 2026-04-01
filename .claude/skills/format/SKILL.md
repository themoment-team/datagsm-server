---
name: format
description: Run KtLint formatting on all Kotlin source files via Gradle. Use when .kt files need formatting or after bulk edits.
allowed-tools: Bash
---

Run KtLint formatting:

```bash
./gradlew ktlintFormat
```

After formatting completes, report whether any files were reformatted and confirm there are no remaining violations.