package team.themoment.datagsm.resource.global.security.service

import team.themoment.datagsm.common.domain.auth.entity.ApiKey
import team.themoment.datagsm.common.dto.global.internal.RateLimitConsumeResult

interface RateLimitService {
    fun tryConsume(apiKey: ApiKey): Boolean

    fun getRemainingTokens(apiKey: ApiKey): Long

    fun getSecondsUntilRefill(apiKey: ApiKey): Long

    fun tryConsumeAndReturnRemaining(apiKey: ApiKey): RateLimitConsumeResult
}
