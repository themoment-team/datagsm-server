#!/bin/bash

INPUT=$(cat)
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool_name')

if [[ "$TOOL_NAME" == "Edit" ]] || [[ "$TOOL_NAME" == "Write" ]]; then
    FILE_PATH=$(echo "$INPUT" | jq -r '.tool_input.file_path // empty')
    CWD=$(echo "$INPUT" | jq -r '.cwd')

    if [[ "$FILE_PATH" == *.kt ]]; then
        echo "[Hook] Running ktlintFormat for $(basename "$FILE_PATH")" >&2
        cd "$CWD"
        if ./gradlew ktlintFormat -q 2>&1; then
            echo "[Hook] Format OK" >&2
        else
            echo "[Hook] Format failed" >&2
        fi
        FILE_NAME=$(basename "$FILE_PATH")
        if [[ "$FILE_NAME" == *ServiceImpl.kt ]] && [[ "$FILE_PATH" != */test/* ]]; then
            RELATIVE="${FILE_PATH#$CWD/}"
            MODULE=$(echo "$RELATIVE" | cut -d'/' -f1)
            if [[ -n "$MODULE" ]] && [[ -d "$CWD/$MODULE/src/test" ]]; then
                echo "[Hook] Running tests for $MODULE after $FILE_NAME edit..." >&2
                TEST_OUTPUT=$(./gradlew ":${MODULE}:test" 2>&1)
                TEST_EXIT=$?
                TAIL=$(echo "$TEST_OUTPUT" | tail -5)
                if [[ $TEST_EXIT -ne 0 ]]; then
                    echo "[Hook] Test FAILED in $MODULE. Last 5 lines:"
                    echo "$TAIL"
                    echo "Tests failed after editing $FILE_NAME. Consider running test-fixer agent."
                else
                    echo "[Hook] Tests passed in $MODULE. Last 5 lines:"
                    echo "$TAIL"
                fi
            fi
        fi
    fi
fi

exit 0
