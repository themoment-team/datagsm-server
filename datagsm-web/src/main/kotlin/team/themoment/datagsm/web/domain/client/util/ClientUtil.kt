package team.themoment.datagsm.web.domain.client.util

import org.springframework.stereotype.Component
import team.themoment.datagsm.common.domain.account.entity.constant.ApiScope

@Component
class ClientUtil {
    fun getAvailableOauthScopes(): Set<String> = setOf(ApiScope.SELF_READ.scope)
}
