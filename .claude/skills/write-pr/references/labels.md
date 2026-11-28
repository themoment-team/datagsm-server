# GitHub Labels Reference

Select **exactly 1 label** from the PR-eligible list below. Even when several labels apply, pick only the single highest-priority one. Do NOT use issue-only or manual labels.

## PR-Eligible Labels (auto-selectable)

| Label                  | When to use                                               |
|------------------------|-----------------------------------------------------------|
| `enhancement:개선작업` | New feature, improvement to existing feature, refactoring |
| `bug:버그`             | Bug fix                                                   |
| `documentation:문서화` | Docs-only changes (README, CONTRIBUTING, comments)        |
| `release:릴리즈`       | Release preparation or version bump                       |

## Off-limits Labels (do NOT assign)

| Label                          | Reason                                                                     |
|--------------------------------|----------------------------------------------------------------------------|
| `waiting for review:검토 대기` | Applied manually by the author after the PR is ready — never auto-assigned |
| `help wanted:도움 필요`        | Issues only                                                                |
| `invalid:무효한`               | Issues only                                                                |
| `duplicate:중복`               | Issues only                                                                |
| `GFI:첫 기여 추천`             | Issues only                                                                |
| `blocked:차단됨`               | Applied manually when blocked by another PR/issue                          |

## Quick Decision

Walk the list top-down and stop at the first match — that is the one label to apply.

```
Bug fix?          → bug:버그
Release or version bump? → release:릴리즈
Docs only?        → documentation:문서화
New feature or improvement? → enhancement:개선작업
Unsure?           → enhancement:개선작업
```

## Label Not Present in the Repository

These labels are a convention, not a guarantee — a target repository may not have them.
If the chosen label does not exist there, create the PR **without any label** instead of
creating the label or substituting another one. `scripts/create-pr.sh` handles this
automatically: it verifies the label up front and drops it when missing.
