#!/bin/bash
INPUT=$(cat)
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool_name')

if [[ "$TOOL_NAME" == "Bash" ]] || [[ "$TOOL_NAME" == "shell" ]]; then
    COMMAND=$(echo "$INPUT" | jq -r '.tool_input.command // .tool_input.cmd // empty')
    CWD=$(echo "$INPUT" | jq -r '.cwd // empty')
    if [[ -n "$CWD" ]]; then
        # 명령을 실행한 폴더마다 로그가 생기면 소스 트리에 섞여 커밋되므로, 저장소 루트 한 곳에만 남긴다.
        ROOT=$(git -C "$CWD" rev-parse --show-toplevel 2>/dev/null || echo "$CWD")
        LOG_FILE="$ROOT/.codex/command.log"
        mkdir -p "$(dirname "$LOG_FILE")"
        echo "[$(date '+%Y-%m-%d %H:%M:%S')] $COMMAND" >> "$LOG_FILE"
    fi
fi

exit 0