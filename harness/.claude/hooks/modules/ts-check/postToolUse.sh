#!/bin/bash
INPUT=$(cat)
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool_name')
if [[ "$TOOL_NAME" == "Edit" ]] || [[ "$TOOL_NAME" == "Write" ]]; then
    FILE_PATH=$(echo "$INPUT" | jq -r '.tool_input.file_path // empty')
    CWD=$(echo "$INPUT" | jq -r '.cwd')
    case "$FILE_PATH" in
        *.ts|*.tsx)
            if [[ ! -f "$CWD/tsconfig.json" ]]; then
                exit 0
            fi
            TSC="$CWD/node_modules/.bin/tsc"
            [[ -x "$TSC" ]] || TSC="npx --no-install tsc"
            echo "[Hook] Running tsc --noEmit for $(basename "$FILE_PATH")" >&2
            if $TSC --noEmit 2>&1; then
                echo "[Hook] Type check OK" >&2
            else
                echo "[Hook] Type check failed" >&2
            fi
            ;;
    esac
fi
exit 0
