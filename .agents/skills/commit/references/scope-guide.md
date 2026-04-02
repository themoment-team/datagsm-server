# Commit Scope Selection Guide

## Priority Rule

**Domain name > Module name**

Always use a domain name. Only fall back to a module name when the change is genuinely cross-cutting (affects multiple domains or the entire module).

## Domain Names (Primary)

| Scope | When to use |
|-------|-------------|
| `auth` | Authentication, API keys |
| `account` | Account management |
| `student` | Student information |
| `club` | Club / project information |
| `neis` | NEIS integration |
| `client` | OAuth client |
| `oauth` | OAuth2 flow |
| `utility` | Shared utilities |

## Module / Cross-cutting Names (Secondary)

| Scope | When to use |
|-------|-------------|
| `global` | Affects multiple modules |
| `ci/cd` | Build / deployment pipelines |
| `web` | Changes scoped to the entire datagsm-web module |
| `openapi` | Changes scoped to the entire datagsm-openapi module |

## Wrong vs Correct Examples

| Wrong | Correct | Reason |
|-------|---------|--------|
| `fix(web): API 키 삭제 버그 수정` | `fix(auth): API 키 삭제 버그 수정` | API key is auth domain |
| `update(common): 학생 엔티티 수정` | `update(student): 엔티티 필드 추가` | Student entity is student domain |
| `add(web): 학생 조회 필터 추가` | `add(student): 학생 조회 필터 추가` | Student feature is student domain |

## Correct Module Name Usage

```
refactor(global): 공통 예외 처리 로직 개선
update(ci/cd): GitHub Actions 워크플로우 최적화
```
