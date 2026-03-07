---
allowed-tools: Bash(bash scripts/get-pr-data.sh:*), Bash(git log:*), Bash(gh api:*), Bash(gh repo:*), Read
description: Reply to resolved PR inline comments with commit hash
---

## Context

- Run data collection: !`bash scripts/get-pr-data.sh`
- PR inline comments: Read `/tmp/pr_comments.json`
- Changed files: Read `/tmp/pr_changed_files.txt`
- Commits in this PR: !`cat /tmp/pr_commits.txt`
- Full diff: Read `/tmp/pr_diff.txt`
- Repo name: !`gh repo view --json nameWithOwner -q .nameWithOwner`

## Your Task

For each comment in `/tmp/pr_comments.json`:

**Step 1 – Judge if resolved**
Compare `path` + `body` of the comment against the diff in `/tmp/pr_diff.txt`.
- RESOLVED: The diff contains a change in the same file that directly addresses the comment's concern
- UNRESOLVED: No relevant change found for that file/concern

**Step 2 – Select commit hash (for resolved comments)**
Run: `git log origin/<base>..HEAD --follow --pretty="%H %h %s" -- <path>`
Select the short hash (7 chars) of the most relevant commit.

**Step 3 – Post reply (for each resolved comment)**
```
gh api repos/<owner>/<repo>/pulls/comments/<comment_id>/replies \
  -f body="<short_hash> 해결했습니다."
```

**Step 4 – Final report**
```
## 답변 완료 코멘트
- [file:line] "comment excerpt" → {hash} 해결했습니다.
...

## 미해결 코멘트 (reply 없음)
- [file:line] "comment excerpt" → 사유: ...
...
```