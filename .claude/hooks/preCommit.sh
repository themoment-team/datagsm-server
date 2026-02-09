#!/bin/bash
# .claude/hooks/preCommit.sh
# Validate commit message format before committing

# 커밋 메시지 형식 검증
COMMIT_MSG="$TOOL_PARAMS_MESSAGE"

# 정규식: type(scope): description
PATTERN="^(add|update|fix|refactor|test|docs|merge)\([a-z/-]+\): .+"

if [[ ! "$COMMIT_MSG" =~ $PATTERN ]]; then
    echo "[Hook] ✗ Invalid commit message format"
    echo "Expected: type(scope): description"
    echo "Example: fix(auth): API 키 로테이션 시 트랜잭션 롤백 방지"
    echo ""
    echo "Types: add/update/fix/refactor/test/docs/merge"
    echo "Scopes: auth, student, club, neis, oauth, ci/cd, global"
    exit 1
fi

# 도메인명 우선 경고
SCOPE=$(echo "$COMMIT_MSG" | sed -n 's/.*(\([^)]*\)).*/\1/p')
MODULE_NAMES=("web" "oauth" "openapi" "common")

if [[ " ${MODULE_NAMES[@]} " =~ " ${SCOPE} " ]]; then
    echo "[Hook] ⚠ Warning: Using module name '$SCOPE' as scope"
    echo "Consider using domain name instead (auth, student, club, etc.)"
    echo "Module names should only be used for cross-cutting concerns."
    echo ""
    # Warning only, not blocking
fi

echo "[Hook] ✓ Commit message format valid"