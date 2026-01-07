package team.themoment.datagsm.web.global.security.checker

import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import team.themoment.datagsm.web.global.security.authentication.principal.CustomPrincipal

@Component
class ScopeChecker {
    fun hasScope(
        authentication: Authentication,
        requiredScope: String,
    ): Boolean {
        val principal = authentication.principal as? CustomPrincipal ?: return false
        // JWT 사용자는 관리자 여부를 별도로 체크해야 함
        // 여기서는 간단히 email 기반으로 체크 (실제로는 Account의 role 확인 필요)
        return principal.email.endsWith("@admin.com") // 임시 로직
    }
}
