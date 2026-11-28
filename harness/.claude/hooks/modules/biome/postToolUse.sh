#!/bin/bash
INPUT=$(cat)
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool_name')
if [[ "$TOOL_NAME" == "Edit" ]] || [[ "$TOOL_NAME" == "Write" ]]; then
    FILE_PATH=$(echo "$INPUT" | jq -r '.tool_input.file_path // empty')
    CWD=$(echo "$INPUT" | jq -r '.cwd')
    case "$FILE_PATH" in
        *.js|*.ts|*.jsx|*.tsx|*.json|*.css|*.graphql)
            if [[ ! -f "$CWD/biome.json" ]] && [[ ! -f "$CWD/biome.jsonc" ]]; then
                exit 0
            fi
            BIOME="$CWD/node_modules/.bin/biome"
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
