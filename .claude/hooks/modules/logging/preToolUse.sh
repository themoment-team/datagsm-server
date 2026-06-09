#!/bin/bash
INPUT=$(cat)
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool_name')
if [[ "$TOOL_NAME" == "Bash" ]]; then
    COMMAND=$(echo "$INPUT" | jq -r '.tool_input.command')
    CWD=$(echo "$INPUT" | jq -r '.cwd')
    LOG_FILE="$CWD/.claude/command.log"
    mkdir -p "$(dirname "$LOG_FILE")"
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $COMMAND" >> "$LOG_FILE"
fi
exit 0
