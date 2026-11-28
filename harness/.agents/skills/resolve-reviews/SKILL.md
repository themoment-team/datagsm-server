---
name: resolve-reviews
description: Collect unresolved PR review threads, critically assess each one against project conventions, auto-apply valid ones, post refutation replies for invalid ones, prompt for partial ones, and resolve threads opened by bot reviewers.
compatibility: Requires git, gh (GitHub CLI), and jq
allowed-tools: Bash(bash *get-pr-data.sh:*), Bash(gh api:*), Bash(git add:*), Bash(git commit:*), Bash(git log:*), Bash(git push:*), Bash(git rev-parse:*), Bash(rm -rf .pr-tmp), Edit, Read
---

## Step 1 — Collect PR Data

```bash
bash .agents/skills/resolve-reviews/scripts/get-pr-data.sh
```

Output files:
- `.pr-tmp/pr_comments.json` — the first comment of each unresolved review thread (`id`, `thread_id`, `path`, `line`, `outdated`, `body`, `user`, `is_bot`)
- `.pr-tmp/pr_changed_files.txt` — changed files
- `.pr-tmp/pr_commits.txt` — commits in this PR
- `.pr-tmp/pr_diff.txt` — full diff

The script collects every page of review threads and excludes threads that are already resolved or that the current user has already replied to, so rerunning the skill does not post duplicate replies. Use the `PR #<number> | Repo: <owner>/<repo> | Base: <base>` line it prints for the API calls below.

## Step 2 — Load Rules and Assess Each Comment

Read existing `AGENTS.md` files that apply to each target path, from the repository root down to the target file's parent directory. More specific instructions override broader ones only within their subtree; do not apply a sibling directory's rules globally. Honor applicable instructions already supplied by the runtime.

`AGENTS.md` is optional. Read the other convention files listed below when they exist, and skip missing files or directories. Do not create a missing instruction file unless the user requests it. Cite only files and sections actually read. If no document covers a topic, use existing code and tool configuration as evidence and label inferred conventions separately from documented rules.

**Rule conflicts**: Follow the runtime instructions, path scopes, and any precedence explicitly defined by the project. Other convention documents are supporting references; do not invent a ranking between tool-specific guides and project documentation. If no applicable instruction resolves a conflict, cite both sources and leave the affected change unapplied while continuing independent work. Intentional scoped overrides are not contradictions.

For Codex, respect the runtime-selected file in each directory: `AGENTS.override.md`, then `AGENTS.md`, then configured fallback filenames. Do not reapply a file superseded by that selection. A document audit may still review a superseded file as content, without treating it as active authority.

Discover applicable `.claude/rules/**`, `.gemini/styleguide.md`, `CONTRIBUTING.md`, and `.github/copilot-instructions.md` files if present, and read them before judging comments. Existing code patterns can provide context but are not grounds to reject a review as a documented-rule violation.

For each comment in `pr_comments.json`, apply the following **layered judgment criteria**:

### Judgment criteria (priority order)

1. **Project conventions** (primary): apply the existing, scoped rules discovered above
   - naming, layering, logging style, error handling, commit scope, etc.
2. **Language/framework best practices** (secondary): official style guides and documentation for the language and framework the target file actually uses (detect them from the file extension and project build or package configuration)
   - Apply only when no matching project rule exists
   - Do not apply guidance from a language or framework the target file does not use

### Verdicts

- **VALID**: reviewer is correct → attempt auto code fix
- **INVALID**: reviewer is wrong with a clear refutation → skip, post refutation reply
- **PARTIAL**: intent is correct but application method or scope is ambiguous → confirm with AskUserQuestion

Always cite an actual source in the rationale (e.g. `AGENTS.md §Logging Style`, `Kotlin coding conventions: prefer val over var`, `React docs: Rules of Hooks`).

## Step 3 — Act on Each Verdict

### VALID → Auto fix

1. Read the target file with the Read tool
2. Apply the reviewer's concern with the Edit tool
3. If the changes have not been committed yet, commit them
4. Record the short commit hash for use in Step 5:
   ```bash
   git rev-parse --short=7 HEAD
   ```

On failure: record the reason and fall back to PARTIAL.

### INVALID → Skip

Do not modify any code. Record the refutation rationale for Step 5.

### PARTIAL → Confirm with AskUserQuestion

Use the AskUserQuestion tool to ask:

```
⚠️ PARTIAL: [file:line] (reviewer)
Review: "..."
Rationale: ...
Accept? (y / n / s = skip for now)
```

- `y`: treat as VALID, attempt code fix
- `n`: treat as INVALID, skip
- `s` / other: record as PENDING

## Step 4 — Print Report

```
## resolve-reviews Results

| # | Reviewer | File | Verdict | Rationale | Action |
|---|----------|------|---------|-----------|--------|
| 1 | gemini-code-assist (bot) | Foo.kt:12 | ✅ VALID | AGENTS.md §Logging Style | Auto-fixed (abc1234), resolve |
| 2 | bob | Bar.kt:34 | ❌ INVALID | AGENTS.md §Exception Message | Skipped |
| 3 | alice | Baz.kt:56 | ⚠️ PARTIAL | - | PENDING |
```

## Step 5 — Push Commits

If any VALID fixes were committed in Step 3, push them before posting replies so the referenced commit hashes are visible on GitHub:

```bash
git push
```

## Step 6 — Post GitHub Replies

Post an inline reply for each comment. Always quote `path` and `comment_id` to prevent shell injection.

```bash
gh api "repos/<owner>/<repo>/pulls/<pr_number>/comments/<comment_id>/replies" \
  -f body="<reply_body>"
```

For reply body templates, read `.agents/skills/resolve-reviews/references/reply-formats.md`.

### Resolve bot threads

After replying, resolve the thread only when **both** conditions hold:

- `is_bot` is `true` for the comment
- the outcome is final: VALID fixed, INVALID, PARTIAL accepted and fixed, or PARTIAL rejected

```bash
gh api graphql -f query='mutation($id: ID!) { resolveReviewThread(input: {threadId: $id}) { thread { isResolved } } }' \
  -f id="<thread_id>"
```

Never resolve a thread opened by a human reviewer, even after the fix is applied; leave resolution to that reviewer. Do not resolve PENDING or fix-failed threads either. If resolving fails, record the error in the report and continue.

## Step 7 — Cleanup

```bash
rm -rf .pr-tmp
```
