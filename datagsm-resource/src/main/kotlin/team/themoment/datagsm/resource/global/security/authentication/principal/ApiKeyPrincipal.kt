package team.themoment.datagsm.resource.global.security.authentication.principal

import org.springframework.security.core.AuthenticatedPrincipal
import team.themoment.datagsm.common.domain.auth.entity.ApiKey

class ApiKeyPrincipal(
    val email: String,
    val apiKey: ApiKey,
) : AuthenticatedPrincipal {
    override fun getName(): String = email
}
