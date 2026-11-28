#!/bin/bash
INPUT=$(cat)
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool_name')

if [[ "$TOOL_NAME" == "Edit" ]] || [[ "$TOOL_NAME" == "Write" ]]; then
    FILE_PATH=$(echo "$INPUT" | jq -r '.tool_input.file_path // empty')
    CWD=$(echo "$INPUT" | jq -r '.cwd')

    if [[ "$FILE_PATH" == *.py ]]; then
        echo "[Hook] Running ruff format+check for $(basename "$FILE_PATH")" >&2
        if command -v ruff > /dev/null 2>&1; then
            ruff format "$FILE_PATH" 2>&1 && ruff check --fix "$FILE_PATH" 2>&1
        else
            cd "$CWD" && python -m ruff format "$FILE_PATH" 2>&1 && python -m ruff check --fix "$FILE_PATH" 2>&1
        fi
        if [[ $? -eq 0 ]]; then
            echo "[Hook] ruff OK" >&2
        else
            echo "[Hook] ruff failed" >&2
        fi
    fi
fi

exit 0