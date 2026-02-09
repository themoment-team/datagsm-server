#!/bin/bash
# .claude/hooks/preToolUse.sh
# Block dangerous commands before execution

# Bash tool 사용 시 위험 명령 차단
if [[ "$TOOL_NAME" == "Bash" ]]; then
    COMMAND="$TOOL_PARAMS_COMMAND"

    # 차단 패턴
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
            echo "[Hook] ✗ Blocked dangerous command: $COMMAND"
            echo "This command is not allowed for safety reasons."
            exit 2  # exit code 2 = hook error
        fi
    done
fi