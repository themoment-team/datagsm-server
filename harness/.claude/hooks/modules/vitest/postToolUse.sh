#!/bin/bash
INPUT=$(cat)
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool_name')

if [[ "$TOOL_NAME" == "Edit" ]] || [[ "$TOOL_NAME" == "Write" ]]; then
    FILE_PATH=$(echo "$INPUT" | jq -r '.tool_input.file_path // empty')
    CWD=$(echo "$INPUT" | jq -r '.cwd')

    case "$FILE_PATH" in
        *.js|*.ts|*.jsx|*.tsx)
            if ! compgen -G "$CWD/vitest.config.*" > /dev/null 2>&1; then
                if ! grep -q '"vitest"' "$CWD/package.json" 2>/dev/null; then
                    exit 0
                fi
            fi
            VITEST="$CWD/node_modules/.bin/vitest"
            [[ -x "$VITEST" ]] || VITEST="npx --no-install vitest"
            echo "[Hook] Running vitest for $(basename "$FILE_PATH")" >&2
            $VITEST run --reporter=verbose "$FILE_PATH" 2>&1
            ;;
    esac
fi

exit 0