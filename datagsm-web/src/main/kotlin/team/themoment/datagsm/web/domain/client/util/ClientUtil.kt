package team.themoment.datagsm.web.domain.client.util

import team.themoment.datagsm.common.domain.client.entity.constant.OAuthScope

object ClientUtil {
    fun getAvailableOauthScopes(): Set<String> = OAuthScope.getAllScopes()
}
