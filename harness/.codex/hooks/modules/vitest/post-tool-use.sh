#!/bin/bash
INPUT=$(cat)
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool_name')

if [[ "$TOOL_NAME" == "Edit" ]] || [[ "$TOOL_NAME" == "Write" ]] || [[ "$TOOL_NAME" == "write_file" ]]; then
    FILE_PATH=$(echo "$INPUT" | jq -r '.tool_input.file_path // .tool_input.path // empty')
    CWD=$(echo "$INPUT" | jq -r '.cwd // empty')

    case "$FILE_PATH" in
        *.js|*.ts|*.jsx|*.tsx)
            [[ -z "$CWD" ]] && exit 0
            PROJECT_ROOT=$(git -C "$CWD" rev-parse --show-toplevel 2>/dev/null || printf '%s' "$CWD")
            if ! compgen -G "$PROJECT_ROOT/vitest.config.*" > /dev/null 2>&1; then
                if ! grep -q '"vitest"' "$PROJECT_ROOT/package.json" 2>/dev/null; then
                    exit 0
                fi
            fi
            VITEST="$PROJECT_ROOT/node_modules/.bin/vitest"
            [[ -x "$VITEST" ]] || VITEST="npx --no-install vitest"
            echo "[Hook] Running vitest for $(basename "$FILE_PATH")" >&2
            $VITEST run --reporter=verbose "$FILE_PATH" 2>&1
            ;;
    esac
fi

exit 0
