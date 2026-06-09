#!/bin/bash
INPUT=$(cat)
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool_name')

if [[ "$TOOL_NAME" == "Bash" ]] || [[ "$TOOL_NAME" == "shell" ]]; then
    COMMAND=$(echo "$INPUT" | jq -r '.tool_input.command // .tool_input.cmd // empty')
    CWD=$(echo "$INPUT" | jq -r '.cwd // empty')
    if [[ -n "$CWD" ]]; then
        LOG_FILE="$CWD/.codex/command.log"
        mkdir -p "$(dirname "$LOG_FILE")"
        echo "[$(date '+%Y-%m-%d %H:%M:%S')] $COMMAND" >> "$LOG_FILE"
    fi
fi

exit 0