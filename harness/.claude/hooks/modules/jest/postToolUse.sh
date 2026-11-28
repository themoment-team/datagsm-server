#!/bin/bash
INPUT=$(cat)
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool_name')
if [[ "$TOOL_NAME" == "Edit" ]] || [[ "$TOOL_NAME" == "Write" ]]; then
    FILE_PATH=$(echo "$INPUT" | jq -r '.tool_input.file_path // empty')
    CWD=$(echo "$INPUT" | jq -r '.cwd')
    case "$FILE_PATH" in
        *.js|*.ts|*.jsx|*.tsx)
            if ! compgen -G "$CWD/jest.config.*" > /dev/null 2>&1; then
                if ! grep -q '"jest"' "$CWD/package.json" 2>/dev/null; then
                    exit 0
                fi
            fi
            JEST="$CWD/node_modules/.bin/jest"
            [[ -x "$JEST" ]] || JEST="npx --no-install jest"
            echo "[Hook] Running jest for $(basename "$FILE_PATH")" >&2
            $JEST --passWithNoTests --findRelatedTests "$FILE_PATH" 2>&1
            ;;
    esac
fi
exit 0
