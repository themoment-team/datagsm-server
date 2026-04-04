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

This project uses Git Flow. Feature branches must be created from `develop` and merged back into `develop`.

1. Analyze all changes with `git status` and `git diff`
2. Infer an appropriate branch name from the changes:
   - Format: `<type>/<kebab-case-description>` — use the same type as the planned commit (exception: use `cicd/` for `ci/cd` type)
   - Reflect the domain scope in the name
   - Examples: `add/add-student-major-filter`, `fix/auth-api-key-deletion`, `refactor/optimize-club-query`
3. Create and checkout the branch:
   ```bash
   git checkout -b <type>/<inferred-name>
   ```
4. Proceed with the commit flow below

**If current branch is NOT `develop`:** proceed directly to the commit flow.

---

## Commit Message Rules

Format: `type(scope): 설명`

- **Types**: `add` / `update` / `fix` / `refactor` / `ci/cd` / `docs` / `test` / `merge` (English)
- **Scopes** (English):
  - **Primary**: Domain names (`account`, `application`, `auth`, `client`, `club`, `neis`, `oauth`, `project`, `student`, `utility`)
  - **Cross-cutting concerns only**: Module names (`web`, `oauth`, `openapi`) or `global`
  - Use domain names by default. Only use module names when changes affect multiple modules or are cross-cutting.
- **Description**: Korean, no period, avoid endings: `~한다/~된다`, `~하기/~하기 위해`, `~합니다/~됩니다`, `~했습니다`
  - Good examples: `엔티티 필드 추가`, `트랜잭션 롤백 방지`, `로직 개선`
- Subject line only (no body)
- Do NOT add AI tool as co-author

## Scope Selection

For the full scope selection table and examples, read `references/scope-guide.md`.

Quick rule: use domain name (`auth`, `application`, `student`, `club`, `neis`, etc.) by default. Use `global` / `ci/cd` / module names only for cross-cutting changes.

## Commit Flow

1. Inspect changes: `git status`, `git diff`
2. Categorize into logical units (feature / bug fix / refactoring / etc.)
3. Group files per unit
4. For each group:
   - Stage only relevant files with `git add`
   - Write a commit message following the rules above
   - `git commit -m "message"`
5. Verify with `git log --oneline -n <count>`
