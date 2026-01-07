package team.themoment.datagsm.global.security.validator

import team.themoment.datagsm.common.domain.account.ApiScope

object ScopeValidator {
    /**
     * 요청된 scope를 가지고 있는지 확인 (계층적 체크)
     *
     * 예시:
     * - student:* 가 있으면 student:read도 통과
     * - student:read 요청 시 student:read 또는 student:* 확인
     *
     * @param userScopes 사용자가 가진 scope 목록
     * @param requiredScope 요청된 scope
     * @return scope를 가지고 있으면 true
     */
    fun hasScope(
        userScopes: Set<String>,
        requiredScope: String,
    ): Boolean {
        if (ApiScope.ALL_SCOPE in userScopes) return true

        if (requiredScope in userScopes) return true
        val resource = requiredScope.substringBefore(':')
        return "$resource:*" in userScopes
    }

    /**
     * 여러 scope 중 하나라도 있는지 확인
     *
     * @param userScopes 사용자가 가진 scope 목록
     * @param requiredScopes 요청된 scope 목록
     * @return 하나라도 있으면 true
     */
    fun hasAnyScope(
        userScopes: Set<String>,
        requiredScopes: Set<String>,
    ): Boolean = requiredScopes.any { hasScope(userScopes, it) }

    /**
     * 모든 scope를 가지고 있는지 확인
     *
     * @param userScopes 사용자가 가진 scope 목록
     * @param requiredScopes 요청된 scope 목록
     * @return 모두 있으면 true
     */
    fun hasAllScopes(
        userScopes: Set<String>,
        requiredScopes: Set<String>,
    ): Boolean = requiredScopes.all { hasScope(userScopes, it) }
}
