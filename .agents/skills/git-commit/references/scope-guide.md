# Commit Scope Selection Guide

## Priority Rule

**Domain name > Module name**

Always use a domain name. Only fall back to a module name when the change is genuinely cross-cutting (affects multiple domains or the entire module).

## Discover Scope at Runtime

Do not use a hardcoded list. Determine scope from:

1. `git diff --name-only` — look at changed file paths
2. Directory structure — infer domain/module from path segments (e.g., `src/auth/`, `packages/user/`, `services/payment/`)
3. Existing `AGENTS.md` files applicable to the changed paths, and any relevant `.claude/rules/` or `CONTRIBUTING.md` conventions

Respect the scope of nested `AGENTS.md` files. Skip missing documents and do not create them. Explicit project scope conventions take precedence over this guide’s domain-first default; otherwise infer the scope from changed paths and recent commits, and label it as inferred rather than citing a missing document.

Do not invent a priority between conflicting reference documents. Follow an explicitly documented project choice; otherwise report the conflict before choosing a scope for the affected commit.

For Codex, respect the runtime-selected file in each directory: `AGENTS.override.md`, then `AGENTS.md`, then configured fallback filenames. Do not reapply a file superseded by that selection. A document audit may still review a superseded file as content, without treating it as active authority.

## Module / Cross-cutting Names (Secondary)

| Scope    | When to use                              |
|----------|------------------------------------------|
| `global` | Affects multiple modules or the whole project |
| `ci/cd`  | Build / deployment pipelines             |
| (module) | Changes scoped to one module but multiple domains |

## Examples

| Wrong | Correct | Reason |
|-------|---------|--------|
| `fix(module): 로그인 버그 수정` | `fix(auth): 로그인 버그 수정` | auth is the domain |
| `update(common): 유저 엔티티 수정` | `update(user): 엔티티 필드 추가` | user entity belongs to user domain |
| `add(module): 결제 필터 추가` | `add(payment): 결제 필터 추가` | payment feature is payment domain |

## Correct Cross-cutting Usage

```
refactor(global): 공통 예외 처리 로직 개선
update(ci/cd): GitHub Actions 워크플로우 최적화
```