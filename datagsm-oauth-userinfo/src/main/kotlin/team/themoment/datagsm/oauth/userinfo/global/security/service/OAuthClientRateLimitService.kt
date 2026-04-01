package team.themoment.datagsm.oauth.userinfo.global.security.service

import team.themoment.datagsm.common.global.dto.internal.RateLimitConsumeResult

interface OAuthClientRateLimitService {
    fun tryConsumeAndReturnRemaining(clientId: String): RateLimitConsumeResult
}
