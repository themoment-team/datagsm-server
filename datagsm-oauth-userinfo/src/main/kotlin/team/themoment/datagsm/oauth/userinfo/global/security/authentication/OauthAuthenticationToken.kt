package team.themoment.datagsm.userinfo.global.security.authentication

import org.springframework.security.authentication.AbstractAuthenticationToken
import team.themoment.datagsm.common.domain.client.entity.constant.OAuthScope
import team.themoment.datagsm.userinfo.global.security.authentication.principal.OauthUserPrincipal

class OauthAuthenticationToken : AbstractAuthenticationToken {
    private val principal: OauthUserPrincipal

    constructor(
        principal: OauthUserPrincipal,
        authorities: Set<OAuthScope>,
    ) : super(authorities) {
        this.principal = principal
        super.setAuthenticated(true)
    }

    override fun getCredentials(): Any? = null

    override fun getPrincipal(): OauthUserPrincipal = principal
}
