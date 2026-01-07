package team.themoment.datagsm.authorization.domain.client.service.impl

import org.springframework.stereotype.Service
import team.themoment.datagsm.common.domain.account.ApiScope
import team.themoment.datagsm.authorization.domain.client.service.GetAvailableOauthScopesService

@Service
class GetAvailableOauthScopesServiceImpl : GetAvailableOauthScopesService {
    override fun execute(): Set<String> = setOf(ApiScope.SELF_READ.scope)
}
