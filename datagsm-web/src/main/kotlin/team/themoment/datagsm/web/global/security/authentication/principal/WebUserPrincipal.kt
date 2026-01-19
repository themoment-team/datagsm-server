package team.themoment.datagsm.web.global.security.authentication.principal

import org.springframework.security.core.AuthenticatedPrincipal

/**
 * web 모듈의 사용자 Principal
 * 사용자 식별 정보(email)을 포함하며, 아래 위치에서 사용됩니다.
 * @see team.themoment.datagsm.web.global.security.authentication.WebUserAuthenticationToken
 * @see team.themoment.datagsm.web.global.security.jwt.filter.JwtAuthenticationFilter
 */
class WebUserPrincipal(
    val email: String,
) : AuthenticatedPrincipal {
    override fun getName(): String = email
}
