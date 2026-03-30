#!/bin/bash
set -e

TITLE="${1:?Error: PR title is required. Usage: create-pr.sh <title> <body-file> [label1,label2,...]}"
BODY_FILE="${2:?Error: Body file is required. Usage: create-pr.sh <title> <body-file> [label1,label2,...]}"
LABELS="${3:-}"

if [ ! -f "$BODY_FILE" ]; then
  echo "ERROR: Body file not found: $BODY_FILE" >&2
  exit 1
fi

# Detect base branch from remote tracking info, fallback to develop
BASE=$(git for-each-ref --format='%(upstream:short)' "$(git symbolic-ref HEAD)" 2>/dev/null \
  | cut -d'/' -f2- \
  | xargs git log --format='%D' 2>/dev/null \
  | grep -oE 'origin/[^ ,]+' | head -1 | sed 's|origin/||' \
  || echo "develop")

# Build gh pr create args array
ARGS=(gh pr create --title "$TITLE" --body-file "$BODY_FILE" --base "$BASE")

if [ -n "$LABELS" ]; then
  IFS=',' read -ra LABEL_ARRAY <<< "$LABELS"
  for label in "${LABEL_ARRAY[@]}"; do
    trimmed="${label#"${label%%[![:space:]]*}"}"  # trim leading space
    trimmed="${trimmed%"${trimmed##*[![:space:]]}"}"  # trim trailing space
    [ -n "$trimmed" ] && ARGS+=(--label "$trimmed")
  done
fi

echo "Creating PR..."
echo "  Title : $TITLE"
echo "  Base  : $BASE"
[ -n "$LABELS" ] && echo "  Labels: $LABELS"
echo ""

"${ARGS[@]}"