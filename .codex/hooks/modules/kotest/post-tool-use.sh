#!/bin/bash
INPUT=$(cat)
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool_name')

if [[ "$TOOL_NAME" == "Edit" ]] || [[ "$TOOL_NAME" == "Write" ]] || [[ "$TOOL_NAME" == "write_file" ]]; then
    FILE_PATH=$(echo "$INPUT" | jq -r '.tool_input.file_path // .tool_input.path // empty')
    CWD=$(echo "$INPUT" | jq -r '.cwd // empty')

    if [[ "$FILE_PATH" == *.kt ]] && [[ -n "$CWD" ]] && [[ "$FILE_PATH" != */test/* ]]; then
        PROJECT_ROOT=$(git -C "$CWD" rev-parse --show-toplevel 2>/dev/null || printf '%s' "$CWD")
        FILE_NAME=$(basename "$FILE_PATH")
        if [[ "$FILE_NAME" == *ServiceImpl.kt ]]; then
            if [[ "$FILE_PATH" == /* ]]; then
                FILE_ABS="$FILE_PATH"
            else
                FILE_ABS="$CWD/$FILE_PATH"
            fi
            RELATIVE="${FILE_ABS#$PROJECT_ROOT/}"
            MODULE=$(echo "$RELATIVE" | cut -d'/' -f1)
            if [[ -n "$MODULE" ]] && [[ -d "$PROJECT_ROOT/$MODULE/src/test" ]]; then
                TEST_CLASS="${FILE_NAME%Impl.kt}Test"
                echo "[Hook] Running test $TEST_CLASS in $MODULE..." >&2
                TEST_OUTPUT=$(cd "$PROJECT_ROOT" && ./gradlew ":${MODULE}:test" --tests "$TEST_CLASS" 2>&1)
                TEST_EXIT=$?
                TAIL=$(echo "$TEST_OUTPUT" | tail -5)
                if [[ $TEST_EXIT -ne 0 ]]; then
                    echo "[Hook] Test FAILED in $MODULE. Last 5 lines:"
                    echo "$TAIL"
                    echo "Tests failed after editing $FILE_NAME. Consider running /test skill."
                else
                    echo "[Hook] Tests passed in $MODULE. Last 5 lines:"
                    echo "$TAIL"
                fi
            fi
        fi
    fi
fi

exit 0