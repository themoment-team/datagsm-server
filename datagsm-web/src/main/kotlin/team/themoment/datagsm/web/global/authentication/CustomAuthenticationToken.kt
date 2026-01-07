package team.themoment.datagsm.global.security.authentication

import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import team.themoment.datagsm.web.global.security.authentication.principal.CustomPrincipal

class CustomAuthenticationToken : AbstractAuthenticationToken {
    private val principal: CustomPrincipal

    constructor(
        principal: CustomPrincipal,
        authorities: Collection<GrantedAuthority?>,
    ) : super(authorities) {
        this.principal = principal
        super.setAuthenticated(true)
    }

    override fun getCredentials(): Any? = null

    override fun getPrincipal(): CustomPrincipal = principal
}
