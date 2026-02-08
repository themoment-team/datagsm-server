---
description: Code review checklist and automatic verification
---

# Code Review Guide

Analyze changed files and verify the following items.

## Check Changes

1. Run `git diff` or `git diff develop...HEAD` to see changed files
2. Read each file with Read tool for detailed analysis

## Checklist

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
- [ ] No N+1 problem? (Need Fetch Join?)
- [ ] Using QueryDSL correctly?

### Test
- [ ] Test code written?
- [ ] Following Given-When-Then pattern?
- [ ] Using MockK verify appropriately?

### Commit
- [ ] Following commit message convention?
- [ ] Using domain name for scope? (not module name)
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