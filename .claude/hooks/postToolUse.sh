#!/bin/bash
if [[ "$TOOL_NAME" == "Edit" ]] || [[ "$TOOL_NAME" == "Write" ]]; then
    FILE_PATH="${TOOL_PARAMS_FILE_PATH:-$TOOL_RESULT_FILE_PATH}"
    if [[ "$FILE_PATH" == *.kt ]]; then
        echo "[Hook] Running ktlintFormat for $FILE_PATH"
        SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
        PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
        cd "$PROJECT_ROOT"
        ./gradlew ktlintFormat -q
        if [ $? -eq 0 ]; then
            echo "[Hook] ✓ Format successful"
        else
            echo "[Hook] ✗ Format failed"
            exit 1
        fi
    fi
fi
