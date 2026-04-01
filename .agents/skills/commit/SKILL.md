---
name: commit
description: Create Git commits by splitting changes into logical units following project conventions. Handles Git Flow automatically — detects develop branch and checks out a feature branch before committing.
---

## Step 0 — Branch Check (Required)

Check the current branch first:

```bash
git branch --show-current
```

**If current branch is `develop`:**

1. Analyze all changes with `git status` and `git diff`
2. Infer an appropriate feature branch name from the changes:
   - Format: `feature/<kebab-case-description>`
   - Reflect the domain scope in the name
   - Examples: `feature/add-student-major-filter`, `feature/fix-auth-api-key-deletion`
3. Create and checkout the feature branch:
   ```bash
   git checkout -b feature/<inferred-name>
   ```
4. Proceed with the commit flow below

**If current branch is NOT `develop`:** proceed directly to the commit flow.

---

## Commit Message Rules

Format: `type(scope): 설명`

- **Types**: `add` / `update` / `fix` / `refactor` / `test` / `docs` / `merge` (English)
- **Scopes** (English):
  - **Primary**: Domain names (`auth`, `account`, `student`, `club`, `project`, `neis`, `client`, `oauth`, `utility`)
  - **Cross-cutting concerns only**: Module names (`web`, `oauth`, `openapi`) or `global`
  - Use domain names by default. Only use module names when changes affect multiple modules or are cross-cutting.
- **Description**: Korean, no period, avoid endings: `~한다/~된다`, `~하기/~하기 위해`, `~합니다/~됩니다`, `~했습니다`
  - Good examples: `엔티티 필드 추가`, `트랜잭션 롤백 방지`, `로직 개선`
- Subject line only (no body)
- Do NOT add AI tool as co-author

## Scope Selection

For the full scope selection table and examples, read `${CLAUDE_SKILL_DIR}/references/scope-guide.md`.

Quick rule: use domain name (`auth`, `student`, `club`, `neis`, etc.) by default. Use `global` / `ci/cd` / module names only for cross-cutting changes.

## Commit Flow

1. Inspect changes: `git status`, `git diff`
2. Categorize into logical units (feature / bug fix / refactoring / etc.)
3. Group files per unit
4. For each group:
   - Stage only relevant files with `git add`
   - Write a commit message following the rules above
   - `git commit -m "message"`
5. Verify with `git log --oneline -n <count>`
