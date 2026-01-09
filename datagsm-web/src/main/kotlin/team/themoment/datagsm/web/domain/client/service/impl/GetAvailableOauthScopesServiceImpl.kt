package team.themoment.datagsm.web.domain.client.service.impl

import org.springframework.stereotype.Service
import team.themoment.datagsm.common.domain.account.entity.constant.ApiScope
import team.themoment.datagsm.web.domain.client.service.GetAvailableOauthScopesService

@Service
class GetAvailableOauthScopesServiceImpl : GetAvailableOauthScopesService {
    override fun execute(): Set<String> = setOf(ApiScope.SELF_READ.scope)
}
