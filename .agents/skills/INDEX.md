# Skills Index

Quick reference for all available skills. Claude loads skill details only when invoked (Progressive Disclosure).

## Workflow Skills

| Skill | When to use | Key feature |
|-------|-------------|-------------|
| [commit](commit/SKILL.md) | Creating Git commits | Auto Git Flow: detects `develop` branch and checks out `feature/*` before committing |
| [format](format/SKILL.md) | Formatting `.kt` files | Runs `./gradlew ktlintFormat` |
| [test](test/SKILL.md) | Running tests | Scoped by class / module / all; analyzes failures |
| [pr-draft](pr-draft/SKILL.md) | Writing PR title & body | Generates 3 title options + Korean formal-style body in `PR_BODY.md` |
| [resolve-pr-comments](resolve-pr-comments/SKILL.md) | Replying to PR review comments | Judges resolution from diff and posts commit hash replies. Script: [get-pr-data.sh](resolve-pr-comments/get-pr-data.sh) |

## Reference Skills (loaded on demand)

| Skill | When to use |
|-------|-------------|
| [code-review](code-review/SKILL.md) | Before merging — full ✓/⚠/✗ checklist |
| [kotest-guide](kotest-guide/SKILL.md) | Writing or reviewing Kotest + MockK tests |
| [kotlin-spring-arch](kotlin-spring-arch/SKILL.md) | Layer design, transactions, exception handling, DTO conversion |
| [api-design](api-design/SKILL.md) | Designing new REST endpoints |
| [migration-guide](migration-guide/SKILL.md) | DB schema / Entity changes |
| [security-checklist](security-checklist/SKILL.md) | Security review before merging auth/API changes |
| [plan-deep-dive](plan-deep-dive/SKILL.md) | Structured requirements interview → spec file |
