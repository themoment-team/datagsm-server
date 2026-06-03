#!/bin/bash
INPUT=$(cat)
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool_name')
if [[ "$TOOL_NAME" == "Edit" ]] || [[ "$TOOL_NAME" == "Write" ]]; then
    FILE_PATH=$(echo "$INPUT" | jq -r '.tool_input.file_path // empty')
    CWD=$(echo "$INPUT" | jq -r '.cwd')
    if [[ "$FILE_PATH" == *.kt ]] && [[ "$FILE_PATH" != */test/* ]]; then
        FILE_NAME=$(basename "$FILE_PATH")
        if [[ "$FILE_NAME" == *ServiceImpl.kt ]]; then
            RELATIVE="${FILE_PATH#$CWD/}"
            MODULE=$(echo "$RELATIVE" | cut -d'/' -f1)
            if [[ -n "$MODULE" ]] && [[ -d "$CWD/$MODULE/src/test" ]]; then
                TEST_CLASS="${FILE_NAME%Impl.kt}Test"
                echo "[Hook] Running test $TEST_CLASS in $MODULE..." >&2
                TEST_OUTPUT=$(cd "$CWD" && ./gradlew ":${MODULE}:test" --tests "$TEST_CLASS" 2>&1)
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
