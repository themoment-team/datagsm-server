---
name: code-review
description: Run a structured checklist over changed files — DTO annotations, Kotlin style, JPA/transaction correctness, test coverage, commit conventions, and security basics. Produces a ✓/⚠/✗ report.
---

# Code Review Guide

Analyze changed files and verify against project conventions.

## Step 1 — Load Rules

Discover all rule files dynamically:

```bash
find .claude/rules -name "*.md" 2>/dev/null
```

Read each returned file in full. These define the authoritative rules for this review.

## Step 2 — Check Changes

1. Run `git diff` or `git diff develop...HEAD` to see changed files
2. Read each changed file with the Read tool for detailed analysis

## Step 3 — Review Checklist

Use the rules loaded in Step 1 as the authoritative source. Common areas to verify:

### DTO
- [ ] Using `@field:JsonProperty`? (not `@param:`)
- [ ] Request DTO uses `@param:Schema`, Response DTO uses `@field:Schema`?
- [ ] DTO name has `ReqDto` or `ResDto` suffix?

### Kotlin Style
- [ ] Using `val` vs `var` appropriately?
- [ ] Using constructor injection?
- [ ] Handling null safety properly?

### JPA/Database
- [ ] Applied `@Transactional(readOnly = true)` for read operations?
- [ ] `@Transactional` at method level only (not class level)?
- [ ] No N+1 problem? (Need Fetch Join?)

### Logging
- [ ] English only, SLF4J `{}` placeholders?
- [ ] No Korean characters or string interpolation in log messages?
- [ ] No colon separators before `{}`?

### Exception
- [ ] Using `ExpectedException` directly (no subclassing)?
- [ ] Message is Korean 합쇼체 + period, no dynamic data?

### Test
- [ ] Test code written?
- [ ] Following Given-When-Then pattern with `DescribeSpec`?
- [ ] Using MockK verify appropriately?

### Commit
- [ ] Following commit message convention?
- [ ] Using domain name for scope? (module names allowed only for cross-cutting concerns)
- [ ] Commits split into logical units?

### Other
- [ ] No excessive comments?
- [ ] No hardcoded secrets?
- [ ] No sensitive information in logs?

## Report Format

For each item:
- ✓ Pass
- ⚠ Warning (recommendation)
- ✗ Error (needs fix)

Final summary:
- Total {n} items verified
- {p} passed, {w} warnings, {e} errors
