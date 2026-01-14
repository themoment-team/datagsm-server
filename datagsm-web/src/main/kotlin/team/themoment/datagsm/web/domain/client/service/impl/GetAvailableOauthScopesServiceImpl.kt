package team.themoment.datagsm.web.domain.client.service.impl

import org.springframework.stereotype.Service
import team.themoment.datagsm.web.domain.client.service.GetAvailableOauthScopesService
import team.themoment.datagsm.web.domain.client.util.ClientUtil

@Service
class GetAvailableOauthScopesServiceImpl : GetAvailableOauthScopesService {
    override fun execute(): Set<String> = ClientUtil.getAvailableOauthScopes()
}
