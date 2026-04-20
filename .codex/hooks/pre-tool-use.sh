#!/bin/bash

INPUT=$(cat)
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool_name')

if [[ "$TOOL_NAME" == "Bash" ]] || [[ "$TOOL_NAME" == "shell" ]]; then
    COMMAND=$(echo "$INPUT" | jq -r '.tool_input.command // .tool_input.cmd // empty')
    CWD=$(echo "$INPUT" | jq -r '.cwd // empty')
    TIMESTAMP=$(date '+%Y-%m-%d %H:%M:%S')

    if [[ -n "$CWD" ]]; then
        LOG_FILE="$CWD/.codex/command.log"
        mkdir -p "$(dirname "$LOG_FILE")"
        echo "[$TIMESTAMP] $COMMAND" >> "$LOG_FILE"
    fi

    BLOCKED_PATTERNS=(
        "rm -rf[[:space:]]*/"
        "sudo rm"
        "> /dev/"
        "dd if="
        "mkfs"
        "curl.*\| sh"
        "wget.*\| sh"
    )
    for pattern in "${BLOCKED_PATTERNS[@]}"; do
        if [[ "$COMMAND" =~ $pattern ]]; then
            echo '{"hookSpecificOutput":{"hookEventName":"PreToolUse","permissionDecision":"deny","permissionDecisionReason":"Dangerous command blocked by project hook"}}'
            exit 0
        fi
    done
fi

exit 0
