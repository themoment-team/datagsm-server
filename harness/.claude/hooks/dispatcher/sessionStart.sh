#!/bin/bash
INPUT=$(cat)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MODULES_DIR="$SCRIPT_DIR/modules"

[[ -d "$MODULES_DIR" ]] || exit 0

# 모듈의 stdout은 세션 컨텍스트에 추가된다
for hook in "$MODULES_DIR"/*/sessionStart.sh; do
    [[ -f "$hook" ]] || continue
    echo "$INPUT" | bash "$hook"
done

exit 0
