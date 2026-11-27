#!/bin/bash
INPUT=$(cat)
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool_name')

if [[ "$TOOL_NAME" == "Bash" ]] || [[ "$TOOL_NAME" == "shell" ]]; then
    COMMAND=$(echo "$INPUT" | jq -r '.tool_input.command // .tool_input.cmd // empty')
    CWD=$(echo "$INPUT" | jq -r '.cwd // empty')
    HOOK_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../../.." && pwd)" || exit 0
    PROJECT_ROOT=$(git -C "${CWD:-$HOOK_ROOT}" rev-parse --show-toplevel 2>/dev/null) || PROJECT_ROOT="$HOOK_ROOT"
    LOG_FILE="$PROJECT_ROOT/.codex/command.log"
    mkdir -p "$(dirname "$LOG_FILE")"
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $COMMAND" >> "$LOG_FILE"
fi

exit 0
