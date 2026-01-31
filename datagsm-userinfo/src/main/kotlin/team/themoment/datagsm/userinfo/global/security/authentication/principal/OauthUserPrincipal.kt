package team.themoment.datagsm.userinfo.global.security.authentication.principal

import org.springframework.security.core.AuthenticatedPrincipal

class OauthUserPrincipal(
    val email: String,
    val clientId: String,
) : AuthenticatedPrincipal {
    override fun getName(): String = email
}
