package team.themoment.datagsm.web.domain.client.util

import team.themoment.datagsm.common.domain.account.entity.constant.ApiScope

object ClientUtil {
    fun getAvailableOauthScopes(): Set<String> = setOf(ApiScope.SELF_READ.scope)
}
