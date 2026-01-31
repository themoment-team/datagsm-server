package team.themoment.datagsm.resource.global.security.authentication.principal

import org.springframework.security.core.AuthenticatedPrincipal
import team.themoment.datagsm.common.domain.auth.entity.ApiKey

/**
 * resource 모듈의 API Key 기반 인증 Principal
 * ApiKey 소유자 이메일, ApiKey 객체를 포함하며 아래 위치에서 사용됩니다.
 * @see team.themoment.datagsm.resource.global.security.authentication.ApiKeyAuthenticationToken
 * @see team.themoment.datagsm.resource.global.security.filter.ApiKeyAuthenticationFilter
 */
class ApiKeyPrincipal(
    val email: String,
    val apiKey: ApiKey,
) : AuthenticatedPrincipal {
    override fun getName(): String = email
}
