#!/bin/bash
set -euo pipefail

PR_NUMBER=$(gh pr view --json number -q .number 2>/dev/null || true)
if [ -z "$PR_NUMBER" ]; then
  echo "ERROR: No open PR found for current branch." >&2
  exit 1
fi

REPO=$(gh repo view --json nameWithOwner -q .nameWithOwner)
BASE=$(gh pr view "$PR_NUMBER" --json baseRefName -q .baseRefName)

git fetch origin "$BASE" --quiet

mkdir -p .pr-tmp

gh api graphql --paginate \
  -F owner="${REPO%%/*}" -F name="${REPO#*/}" -F number="$PR_NUMBER" \
  -f query='
query($owner: String!, $name: String!, $number: Int!, $endCursor: String) {
  viewer { login }
  repository(owner: $owner, name: $name) {
    pullRequest(number: $number) {
      reviewThreads(first: 100, after: $endCursor) {
        pageInfo { hasNextPage endCursor }
        nodes {
          id
          isResolved
          isOutdated
          path
          line
          originalLine
          comments(first: 100) {
            nodes { databaseId body author { __typename login } }
          }
        }
      }
    }
  }
}' \
  --jq '
.data.viewer.login as $me
| .data.repository.pullRequest.reviewThreads.nodes[]
| select(.isResolved | not)
| select(any(.comments.nodes[1:][]; .author.login == $me) | not)
| .comments.nodes[0] as $root
| {
    id: $root.databaseId,
    thread_id: .id,
    path,
    line: (.line // .originalLine),
    outdated: .isOutdated,
    body: $root.body,
    user: ($root.author.login // "ghost"),
    is_bot: ($root.author.__typename == "Bot" or (($root.author.login // "") | endswith("[bot]")))
  }' \
  | jq -s . > .pr-tmp/pr_comments.json

git log "origin/$BASE..HEAD" --pretty=format:"%H %h %s" > .pr-tmp/pr_commits.txt

git diff "origin/$BASE...HEAD" --name-only > .pr-tmp/pr_changed_files.txt

git diff "origin/$BASE...HEAD" > .pr-tmp/pr_diff.txt

echo "PR #$PR_NUMBER | Repo: $REPO | Base: $BASE"
echo "Comments: $(jq length .pr-tmp/pr_comments.json) (bot: $(jq '[.[] | select(.is_bot)] | length' .pr-tmp/pr_comments.json)), Changed files: $(wc -l < .pr-tmp/pr_changed_files.txt | tr -d ' ')"
