#!/bin/bash
INPUT=$(cat)
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool_name')

if [[ "$TOOL_NAME" == "Edit" ]] || [[ "$TOOL_NAME" == "Write" ]] || [[ "$TOOL_NAME" == "write_file" ]]; then
    FILE_PATH=$(echo "$INPUT" | jq -r '.tool_input.file_path // .tool_input.path // empty')
    CWD=$(echo "$INPUT" | jq -r '.cwd // empty')

    case "$FILE_PATH" in
        *.js|*.ts|*.jsx|*.tsx|*.css|*.scss|*.html|*.json|*.md|*.yaml|*.yml)
            [[ -z "$CWD" ]] && exit 0
            PROJECT_ROOT=$(git -C "$CWD" rev-parse --show-toplevel 2>/dev/null || printf '%s' "$CWD")
            if ! compgen -G "$PROJECT_ROOT/.prettierrc*" > /dev/null 2>&1 && \
               ! compgen -G "$PROJECT_ROOT/prettier.config.*" > /dev/null 2>&1; then
                exit 0
            fi
            PRETTIER="$PROJECT_ROOT/node_modules/.bin/prettier"
            [[ -x "$PRETTIER" ]] || PRETTIER="npx --no-install prettier"
            echo "[Hook] Running prettier --write for $(basename "$FILE_PATH")" >&2
            if $PRETTIER --write "$FILE_PATH" 2>&1; then
                echo "[Hook] prettier OK" >&2
            else
                echo "[Hook] prettier failed" >&2
            fi
            ;;
    esac
fi

exit 0
