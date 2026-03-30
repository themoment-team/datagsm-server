---
name: pr-draft
description: Generate PR title suggestions and a Korean formal-style PR body based on commits since the base branch. Writes output to PR_BODY.md.
allowed-tools: Bash, Read, Write
---

## Gather Context

```bash
# Get base branch and commits
gh pr view --json baseRefName,number 2>/dev/null || git log --oneline -20
git log origin/$(git log --oneline -1 | head -1)..HEAD --oneline 2>/dev/null || git log --oneline -10
git diff origin/develop...HEAD --stat 2>/dev/null || git diff HEAD~5...HEAD --stat
```

## PR Title

Generate **3 title suggestions** in the format `[scope] description`:

- Scope: domain name (e.g., `[student]`, `[auth]`)
- Description: concise, in Korean
- Max 50 characters total

## PR Body

Write to `PR_BODY.md` using the following template (Korean 합쇼체, max 2500 characters):

```markdown
## 개요

<!-- 변경 사유와 목적을 1-3문장으로 작성 -->

## 변경 사항

<!-- 주요 변경 내용을 불릿으로 작성 -->
-

## 테스트

<!-- 테스트 방법 또는 테스트 케이스 -->
- [ ]

## 참고 사항

<!-- 리뷰어가 알아야 할 사항 (선택) -->
```

Use formal endings: `~하였습니다`, `~되었습니다`, `~추가하였습니다`.

After writing the file, display the 3 title suggestions and the path to `PR_BODY.md`.