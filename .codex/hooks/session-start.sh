#!/bin/bash
INPUT=$(cat)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MODULES_DIR="$SCRIPT_DIR/modules"
[[ -d "$MODULES_DIR" ]] || MODULES_DIR="${SCRIPT_DIR%/*}/modules"

[[ -d "$MODULES_DIR" ]] || exit 0

CONTEXT=""
for hook in "$MODULES_DIR"/*/session-start.sh; do
    [[ -f "$hook" ]] || continue
    OUTPUT=$(echo "$INPUT" | bash "$hook")
    [[ -n "$OUTPUT" ]] && CONTEXT+="$OUTPUT"$'\n\n'
done

[[ -n "$CONTEXT" ]] || exit 0
jq -n --arg context "$CONTEXT" \
    '{hookSpecificOutput: {hookEventName: "SessionStart", additionalContext: $context}}'
exit 0
