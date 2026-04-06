#!/bin/bash

INPUT=$(cat)
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool_name')

if [[ "$TOOL_NAME" == "Bash" ]]; then
    COMMAND=$(echo "$INPUT" | jq -r '.tool_input.command')
    CWD=$(echo "$INPUT" | jq -r '.cwd')
    TIMESTAMP=$(date '+%Y-%m-%d %H:%M:%S')
    LOG_FILE="$CWD/.claude/command.log"
    mkdir -p "$(dirname "$LOG_FILE")"
    echo "[$TIMESTAMP] $COMMAND" >> "$LOG_FILE"
    BLOCKED_PATTERNS=(
        "rm -rf /"
        "sudo rm"
        "> /dev/"
        "dd if="
        "mkfs"
        "curl.*\| sh"
        "wget.*\| sh"
    )
    for pattern in "${BLOCKED_PATTERNS[@]}"; do
        if [[ "$COMMAND" =~ $pattern ]]; then
            echo "[Hook] Blocked dangerous command: $COMMAND" >&2
            exit 2
        fi
    done
fi

exit 0
