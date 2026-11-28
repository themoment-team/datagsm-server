#!/bin/bash
INPUT=$(cat)
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool_name')

if [[ "$TOOL_NAME" == "Edit" ]] || [[ "$TOOL_NAME" == "Write" ]] || [[ "$TOOL_NAME" == "write_file" ]]; then
    FILE_PATH=$(echo "$INPUT" | jq -r '.tool_input.file_path // .tool_input.path // empty')
    CWD=$(echo "$INPUT" | jq -r '.cwd // empty')

    if [[ "$FILE_PATH" == *.py ]]; then
        [[ -z "$CWD" ]] && exit 0
        PROJECT_ROOT=$(git -C "$CWD" rev-parse --show-toplevel 2>/dev/null || printf '%s' "$CWD")
        echo "[Hook] Running ruff format+check for $(basename "$FILE_PATH")" >&2
        if command -v ruff > /dev/null 2>&1; then
            ruff format "$FILE_PATH" 2>&1 && ruff check --fix "$FILE_PATH" 2>&1
        else
            cd "$PROJECT_ROOT" && python -m ruff format "$FILE_PATH" 2>&1 && python -m ruff check --fix "$FILE_PATH" 2>&1
        fi
        if [[ $? -eq 0 ]]; then
            echo "[Hook] ruff OK" >&2
        else
            echo "[Hook] ruff failed" >&2
        fi
    fi
fi

exit 0
