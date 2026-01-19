package team.themoment.datagsm.web.global.security.authentication.principal

import org.springframework.security.core.AuthenticatedPrincipal

class WebUserPrincipal(
    val email: String,
) : AuthenticatedPrincipal {
    override fun getName(): String = email
}
