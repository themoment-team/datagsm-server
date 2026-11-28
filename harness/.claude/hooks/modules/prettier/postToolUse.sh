#!/bin/bash
INPUT=$(cat)
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool_name')
if [[ "$TOOL_NAME" == "Edit" ]] || [[ "$TOOL_NAME" == "Write" ]]; then
    FILE_PATH=$(echo "$INPUT" | jq -r '.tool_input.file_path // empty')
    CWD=$(echo "$INPUT" | jq -r '.cwd')
    case "$FILE_PATH" in
        *.js|*.ts|*.jsx|*.tsx|*.css|*.scss|*.html|*.json|*.md|*.yaml|*.yml)
            if ! compgen -G "$CWD/.prettierrc*" > /dev/null 2>&1 && \
               ! compgen -G "$CWD/prettier.config.*" > /dev/null 2>&1; then
                exit 0
            fi
            PRETTIER="$CWD/node_modules/.bin/prettier"
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
