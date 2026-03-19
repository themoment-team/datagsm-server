---
name: commit
description: Create Git commits by splitting changes into logical units following project conventions
---

Create Git commits following these rules:

- Follow commit message format: `type(scope): 설명`
    - Types: add/update/fix/refactor/test/docs/merge (English)
    - Scopes (English):
        - **Primary**: Domain names (auth, account, student, club, project, neis, client, oauth, utility)
        - **Cross-cutting concerns only**: Module names (web, oauth, openapi) or global
        - Use domain names by default. Only use module names when changes affect multiple modules or cross-cutting concerns.
    - Description: **Korean**, lowercase start, no period, avoid noun-ending style (e.g., "~ 추가", "~ 수정"), do NOT use "~한다/~된다" verb ending style
- Use subject line only (no commit body)
- Do NOT add AI tool as co-author
- Split changes into appropriate logical units with multiple commits
- Each commit should have a single responsibility

## Scope Selection Guide

**Priority**: Domain name > Module name

**Domain names (Primary)**:

- auth: Authentication/API keys
- account: Account management
- student: Student information
- club: Club/Project information
- neis: NEIS integration
- client: OAuth client
- oauth: OAuth2 flow

**Module names (Cross-cutting concerns only)**:

- global: Affects multiple modules
- ci/cd: Build/deployment
- web/oauth/openapi: Module-wide impact

**Wrong Examples**:

- `fix(web): API 키 삭제 버그 수정` → `fix(auth): API 키 삭제 버그 수정`
- `update(common): 학생 엔티티 수정` → `update(student): 엔티티 필드 추가`

**Correct Module Name Usage**:

- `refactor(global): 공통 예외 처리 로직 개선`
- `update(ci/cd): GitHub Actions 워크플로우 최적화`

Steps:

1. Check changes with `git status` and `git diff`
2. Categorize changes into logical units (e.g., feature addition, bug fix, refactoring)
3. Group files by each unit
4. For each group:
    - Stage only relevant files with `git add`
    - Write concise commit message following conventions (subject only)
    - Execute `git commit -m "message"`
5. Verify results with `git log --oneline -n [number of commits]`
