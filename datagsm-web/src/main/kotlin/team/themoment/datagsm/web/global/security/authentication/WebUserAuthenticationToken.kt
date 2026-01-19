package team.themoment.datagsm.web.global.security.authentication

import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import team.themoment.datagsm.web.global.security.authentication.principal.WebUserPrincipal

class WebUserAuthenticationToken : AbstractAuthenticationToken {
    private val principal: WebUserPrincipal

    constructor(
        principal: WebUserPrincipal,
        authorities: Collection<GrantedAuthority>,
    ) : super(authorities) {
        this.principal = principal
        super.setAuthenticated(true)
    }

    override fun getCredentials(): Any? = null

    override fun getPrincipal(): WebUserPrincipal = principal
}
