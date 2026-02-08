#!/bin/bash
# .claude/hooks/postToolUse.sh
# Auto-format Kotlin files after Edit/Write tool usage

# Edit/Write tool 사용 후 자동 ktlintFormat 실행
if [[ "$TOOL_NAME" == "Edit" ]] || [[ "$TOOL_NAME" == "Write" ]]; then
    FILE_PATH="${TOOL_PARAMS_FILE_PATH:-$TOOL_RESULT_FILE_PATH}"

    # Kotlin 파일인지 확인
    if [[ "$FILE_PATH" == *.kt ]]; then
        echo "[Hook] Running ktlintFormat for $FILE_PATH"

        # 프로젝트 루트에서 실행
        cd /Users/snowykte0426/Programming/datagsm-server
        ./gradlew ktlintFormat -q

        if [ $? -eq 0 ]; then
            echo "[Hook] ✓ Format successful"
        else
            echo "[Hook] ✗ Format failed"
            exit 1
        fi
    fi
fi