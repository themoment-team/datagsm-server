# Commit Scope Selection Guide

## Priority Rule

**Domain name > Module name**

Always use a domain name. Only fall back to a module name when the change is genuinely cross-cutting (affects multiple domains or the entire module).

## Discover Domains at Runtime

Run this before selecting a scope — do not use a hardcoded list:

```bash
sh .agents/skills/commit/scripts/discover-domains.sh
```

Use the output as the candidate domain scope list.

## Discover Modules at Runtime

Run this to get the module list (strips `datagsm-` prefix for use as scope):

```bash
sh .agents/skills/commit/scripts/discover-modules.sh
```

Module scopes are secondary — use only when changes are cross-cutting across multiple domains within that module.

## Module / Cross-cutting Names (Secondary)

| Scope    | When to use                                                             |
|----------|-------------------------------------------------------------------------|
| `global` | Affects multiple modules                                                |
| `ci/cd`  | Build / deployment pipelines                                            |
| (module) | Changes scoped to an entire module (use name without `datagsm-` prefix) |

## Wrong vs Correct Examples

| Wrong                       | Correct                      | Reason                            |
|-----------------------------|------------------------------|-----------------------------------|
| `fix(web): API 키 삭제 버그 수정`  | `fix(auth): API 키 삭제 버그 수정`  | API key is auth domain            |
| `update(common): 학생 엔티티 수정` | `update(student): 엔티티 필드 추가` | Student entity is student domain  |
| `add(web): 학생 조회 필터 추가`     | `add(student): 학생 조회 필터 추가`  | Student feature is student domain |

## Correct Module Name Usage

```
refactor(global): 공통 예외 처리 로직 개선
update(ci/cd): GitHub Actions 워크플로우 최적화
```
