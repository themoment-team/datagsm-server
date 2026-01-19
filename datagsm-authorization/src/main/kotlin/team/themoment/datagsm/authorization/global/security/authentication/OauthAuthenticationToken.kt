package team.themoment.datagsm.authorization.global.security.authentication

import org.springframework.security.authentication.AbstractAuthenticationToken
import team.themoment.datagsm.authorization.global.security.authentication.principal.OauthUserPrincipal
import team.themoment.datagsm.common.domain.account.entity.constant.ApiScope

class OauthAuthenticationToken : AbstractAuthenticationToken {
    private val principal: OauthUserPrincipal

    constructor(
        principal: OauthUserPrincipal,
        authorities: Set<ApiScope>,
    ) : super(authorities) {
        this.principal = principal
        super.setAuthenticated(true)
    }

    override fun getCredentials(): Any? = null

    override fun getPrincipal(): OauthUserPrincipal = principal
}
