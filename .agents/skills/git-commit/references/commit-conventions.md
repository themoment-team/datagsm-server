# Commit & PR Conventions

## Commit Message Format

`type(scope): description`

- **Types**: `add` / `update` / `fix` / `refactor` / `ci/cd` / `docs` / `test` / `merge` (English)
- **Scope**: domain name (not module name)
- **Description**: Korean, no period, avoid endings: `~한다/~된다`, `~하기/~하기 위해`, `~합니다/~됩니다`, `~했습니다`
  - Good examples: `엔티티 필드 추가`, `트랜잭션 롤백 방지`, `로직 개선`
- Subject line only (no body)

## Scope Rules

Use **domain names** as scope, not module names:

```
// CORRECT
fix(auth): OAuth 토큰 발급 로직 수정
update(student): 학생 조회 필터 추가

// WRONG — module names as scope
fix(web): 버그 수정
update(common): 엔티티 수정
```

Use module-level scope **only** for cross-cutting concerns:

```
refactor(global): 공통 예외 처리 구조 개선
update(ci/cd): GitHub Actions 워크플로우 수정
```
