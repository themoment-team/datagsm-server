#!/bin/bash
INPUT=$(cat)
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool_name')

if [[ "$TOOL_NAME" == "Edit" ]] || [[ "$TOOL_NAME" == "Write" ]] || [[ "$TOOL_NAME" == "write_file" ]]; then
    FILE_PATH=$(echo "$INPUT" | jq -r '.tool_input.file_path // .tool_input.path // empty')
    CWD=$(echo "$INPUT" | jq -r '.cwd // empty')

    case "$FILE_PATH" in
        *.js|*.ts|*.jsx|*.tsx|*.json|*.css|*.graphql)
            [[ -z "$CWD" ]] && exit 0
            PROJECT_ROOT=$(git -C "$CWD" rev-parse --show-toplevel 2>/dev/null || printf '%s' "$CWD")
            if [[ ! -f "$PROJECT_ROOT/biome.json" ]] && [[ ! -f "$PROJECT_ROOT/biome.jsonc" ]]; then
                exit 0
            fi
            BIOME="$PROJECT_ROOT/node_modules/.bin/biome"
            [[ -x "$BIOME" ]] || BIOME="npx --no-install biome"
            echo "[Hook] Running biome check for $(basename "$FILE_PATH")" >&2
            if $BIOME check --write "$FILE_PATH" 2>&1; then
                echo "[Hook] biome OK" >&2
            else
                echo "[Hook] biome failed" >&2
            fi
            ;;
    esac
fi

exit 0
