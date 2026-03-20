---
name: pr-draft
description: Generate PR body and title suggestions based on commits diff from develop branch
---

## Context gathering

Run the following to collect context:

```bash
git branch --show-current
git log develop..HEAD --oneline
git diff develop...HEAD --stat
git diff develop...HEAD
```

Read `.github/PULL_REQUEST_TEMPLATE.md` for the PR template structure.

## PR Title Convention

This project uses the following PR title format: `[scope] description`

**Available Scopes:**
- **Primary**: Domain names - auth, account, student, club, project, neis, client, oauth
- **Cross-cutting concerns only**: Module names - web, oauth, openapi
- **Others**: ci/cd, global

**Scope Selection Rule:**
- Use domain names by default for feature-specific changes
- Only use module names or global when changes affect multiple modules or cross-cutting concerns (e.g., config, security, common utilities)

**Recent PR Title Examples:**
- `[global] 기여자 지침 문서 추가`
- `[global] 올바르지 않은 공개 API 경로 설정 수정`
- `[student] 졸업생 전환 및 저장 기능 구현`
- `[ci/cd] CD 파이프라인 ZIP 패키징 및 빌드 최적화`
- `[client] 클라이언트 조회 시 페이지네이션 파라미터 추가`

## Your task

Based on the above information, perform the following tasks:

1. **PR Title Suggestions**:
   - Suggest 3 appropriate titles based on the convention above
   - Format: `[scope] description`
   - Description: Korean, clear and concise
   - No emojis

2. **PR Body**:
   - Follow the PR template structure
   - Analyze commits and changes between develop and current branch
   - Total length must not exceed 2500 characters
   - No emojis
   - Write in Korean
   - Be clear and specific

3. **Writing Style**:
   - Use formal Korean ending style: "~하였습니다", "~되었습니다", "~추가하였습니다" (not "~했어요", "~합니다", "~했습니다")

4. **Save to file**:
   - Save the content to `PR_BODY.md`
   - Overwrite if file already exists

5. **Output format**:

   ```
   ## 추천 PR 제목

   1. [title1]
   2. [title2]
   3. [title3]

   ## PR 본문 (PR_BODY.md에 저장됨)

   [preview of generated content]
   ```
