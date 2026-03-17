#!/bin/bash
# .claude/hooks/preCommit.sh
# Validate commit message format before committing

if [[ "$TOOL_NAME" != "Bash" ]]; then
    exit 0
fi
COMMAND="$TOOL_PARAMS_COMMAND"
if [[ ! "$COMMAND" =~ git[[:space:]]+commit ]]; then
    exit 0
fi
if [[ "$COMMAND" =~ \$\( || "$COMMAND" =~ "<<" ]]; then
    exit 0
fi
COMMIT_MSG=$(echo "$COMMAND" | sed -n -e 's/.*-m[[:space:]]*"\([^"]*\)".*/\1/p' -e "s/.*-m[[:space:]]*'\([^']*\)'.*/\1/p" | head -n 1)
if [[ -z "$COMMIT_MSG" ]]; then
    exit 0
fi
PATTERN="^(add|update|fix|refactor|test|docs|merge)\\(([^)]+)\\): .+"
if [[ ! "$COMMIT_MSG" =~ $PATTERN ]]; then
    echo "[Hook] ✗ Invalid commit message format"
    echo "Expected: type(scope): description"
    echo "Example: fix(auth): API 키 로테이션 시 트랜잭션 롤백 방지"
    echo ""
    echo "Types: add/update/fix/refactor/test/docs/merge"
    echo "Scopes: auth, account, student, club, project, neis, client, oauth, utility, ci/cd, global"
    exit 2
fi
SCOPE=${BASH_REMATCH[2]}
MODULE_NAMES=("web" "oauth" "openapi" "common")
if [[ " ${MODULE_NAMES[@]} " =~ " ${SCOPE} " ]]; then
    echo "[Hook] ⚠ Warning: Using module name '$SCOPE' as scope"
    echo "Consider using domain name instead (auth, student, club, etc.)"
    echo "Module names should only be used for cross-cutting concerns."
fi
echo "[Hook] ✓ Commit message format valid"
