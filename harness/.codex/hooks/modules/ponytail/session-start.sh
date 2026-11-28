#!/bin/bash
HOOK_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../../.." && pwd)" || exit 0
SKILL="$HOOK_ROOT/.agents/skills/ponytail/SKILL.md"
[[ -f "$SKILL" ]] || exit 0

echo "PONYTAIL MODE ACTIVE"
# frontmatter를 제외한 규칙 본문만 주입한다
awk 'NR == 1 && /^---$/ { fm = 1; next } fm && /^---$/ { fm = 0; next } !fm' "$SKILL"
exit 0
