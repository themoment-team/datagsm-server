#!/bin/bash
set -e

TITLE="${1:?Error: PR title is required. Usage: create-pr.sh <title> <body-file> [label]}"
BODY_FILE="${2:?Error: Body file is required. Usage: create-pr.sh <title> <body-file> [label]}"
LABEL_INPUT="${3:-}"

if [ ! -f "$BODY_FILE" ]; then
  echo "ERROR: Body file not found: $BODY_FILE" >&2
  exit 1
fi

LABEL=$(echo "${LABEL_INPUT%%,*}" | xargs)

CURRENT=$(git branch --show-current)
case "$CURRENT" in
  feature/*)  BASE="develop" ;;
  develop)    BASE="master" ;;
  *)          BASE=$(gh pr view --json baseRefName -q .baseRefName 2>/dev/null || echo "develop") ;;
esac

if ! git ls-remote --exit-code --heads origin "$BASE" >/dev/null 2>&1; then
  DEFAULT_BASE=$(gh repo view --json defaultBranchRef -q .defaultBranchRef.name 2>/dev/null || true)
  if [ -n "$DEFAULT_BASE" ]; then
    echo "WARNING: Base branch '$BASE' does not exist on origin. Using default branch '$DEFAULT_BASE'." >&2
    BASE="$DEFAULT_BASE"
  fi
fi

if [ -n "$LABEL" ]; then
  EXISTING=$(gh label list --limit 200 --json name -q '.[].name' 2>/dev/null || true)
  if [ -n "$EXISTING" ] && ! printf '%s\n' "$EXISTING" | grep -Fxq "$LABEL"; then
    echo "WARNING: Label '$LABEL' does not exist in this repository. Creating the PR without a label." >&2
    LABEL=""
  fi
fi

ARGS=(gh pr create --title "$TITLE" --body-file "$BODY_FILE" --base "$BASE")
[ -n "$LABEL" ] && ARGS+=(--label "$LABEL")

echo "Creating PR..."
echo "  Title : $TITLE"
echo "  Base  : $BASE"
echo "  Label : ${LABEL:-(none)}"
echo ""

if ! "${ARGS[@]}"; then
  if [ -n "$LABEL" ]; then
    echo "" >&2
    echo "WARNING: PR creation failed with label '$LABEL'. Retrying without the label." >&2
    gh pr create --title "$TITLE" --body-file "$BODY_FILE" --base "$BASE"
  else
    exit 1
  fi
fi
