package team.themoment.datagsm.resource.global.security.authentication

import org.springframework.security.authentication.AbstractAuthenticationToken
import team.themoment.datagsm.common.domain.auth.entity.constant.ApiKeyScope
import team.themoment.datagsm.resource.global.security.authentication.principal.ApiKeyPrincipal

class ApiKeyAuthenticationToken : AbstractAuthenticationToken {
    private val principal: ApiKeyPrincipal

    constructor(
        principal: ApiKeyPrincipal,
        authorities: Set<ApiKeyScope>,
    ) : super(authorities) {
        this.principal = principal
        super.setAuthenticated(true)
    }

    override fun getCredentials(): Any? = null

    override fun getPrincipal(): ApiKeyPrincipal = principal
}
