---
name: resolve-pr-comments
description: Reply to resolved PR inline comments with the commit hash that fixed them
---

## Context gathering

Run the following to collect context:

```bash
bash scripts/dev/get-pr-data.sh
```

Then read:
- `.pr-tmp/pr_comments.json` — PR inline comments
- `.pr-tmp/pr_changed_files.txt` — Changed files
- `.pr-tmp/pr_commits.txt` — Commits in this PR
- `.pr-tmp/pr_diff.txt` — Full diff

Also run:
```bash
gh repo view --json nameWithOwner -q .nameWithOwner
gh pr view --json number -q .number
```

## Your Task

For each comment in `.pr-tmp/pr_comments.json`:

**Step 1 – Judge if resolved**
Compare `path` + `body` of the comment against the diff in `.pr-tmp/pr_diff.txt`.
- RESOLVED: The diff contains a change in the same file that directly addresses the comment's concern
- UNRESOLVED: No relevant change found for that file/concern

**Step 2 – Select commit hash (for resolved comments)**
Run: `git log origin/<base>..HEAD --follow --pretty="%H %h %s" -- "<path>"`
Select the short hash (7 chars) of the most relevant commit.

**Step 3 – Post reply (for each resolved comment)**
Always quote variables to prevent shell injection. `path` and `comment_id` come from external PR data and may contain special characters.
```
gh api "repos/<owner>/<repo>/pulls/<pr_number>/comments/<comment_id>/replies" \
  -f body="<short_hash> 에서 해결했습니다."
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

**Step 5 – Cleanup**
Remove temporary files created by the data collection script:
```bash
rm -rf .pr-tmp
```
