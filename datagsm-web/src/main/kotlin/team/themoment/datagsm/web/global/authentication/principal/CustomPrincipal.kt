package team.themoment.datagsm.web.global.authentication.principal

import org.springframework.security.core.AuthenticatedPrincipal
import team.themoment.datagsm.common.domain.auth.entity.ApiKey
import team.themoment.datagsm.web.global.security.authentication.type.AuthType

class CustomPrincipal(
    val email: String,
    val type: AuthType,
    val clientId: String?,
    val apiKey: ApiKey?,
) : AuthenticatedPrincipal {
    override fun getName(): String = email
}
