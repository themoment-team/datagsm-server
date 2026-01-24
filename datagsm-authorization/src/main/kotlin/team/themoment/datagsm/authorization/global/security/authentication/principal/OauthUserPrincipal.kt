package team.themoment.datagsm.authorization.global.security.authentication.principal

import org.springframework.security.core.AuthenticatedPrincipal

/**
 * authorization 모듈의 OAuth2 사용자 Principal
 * 사용자 이메일, OAuth 클라이언트 ID를 포함하며 아래 위치에서 사용됩니다.
 * @see team.themoment.datagsm.authorization.global.security.authentication.OauthAuthenticationToken
 * @see team.themoment.datagsm.authorization.global.security.jwt.filter.JwtAuthenticationFilter
 */
class OauthUserPrincipal(
    val email: String,
    val clientId: String,
) : AuthenticatedPrincipal {
    override fun getName(): String = email
}
