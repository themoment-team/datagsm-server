package team.themoment.datagsm.authorization.global.security.authentication.principal

import org.springframework.security.core.AuthenticatedPrincipal
import team.themoment.datagsm.authorization.global.security.authentication.type.AuthType

class CustomPrincipal(
    val email: String,
    val type: AuthType,
    val clientId: String?,
) : AuthenticatedPrincipal {
    override fun getName(): String = email
}
