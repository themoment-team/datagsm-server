#!/bin/bash
INPUT=$(cat)
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool_name')

if [[ "$TOOL_NAME" == "Edit" ]] || [[ "$TOOL_NAME" == "Write" ]] || [[ "$TOOL_NAME" == "write_file" ]]; then
    FILE_PATH=$(echo "$INPUT" | jq -r '.tool_input.file_path // .tool_input.path // empty')
    CWD=$(echo "$INPUT" | jq -r '.cwd // empty')

    case "$FILE_PATH" in
        *.ts|*.tsx)
            [[ -z "$CWD" ]] && exit 0
            PROJECT_ROOT=$(git -C "$CWD" rev-parse --show-toplevel 2>/dev/null || printf '%s' "$CWD")
            if [[ ! -f "$PROJECT_ROOT/tsconfig.json" ]]; then
                exit 0
            fi
            TSC="$PROJECT_ROOT/node_modules/.bin/tsc"
            [[ -x "$TSC" ]] || TSC="npx --no-install tsc"
            echo "[Hook] Running tsc --noEmit for $(basename "$FILE_PATH")" >&2
            cd "$PROJECT_ROOT"
            if $TSC --noEmit 2>&1; then
                echo "[Hook] Type check OK" >&2
            else
                echo "[Hook] Type check failed" >&2
            fi
            ;;
    esac
fi

exit 0
