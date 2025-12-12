package team.themoment.datagsm.global.security.service

import team.themoment.datagsm.domain.auth.entity.ApiKey
import team.themoment.datagsm.global.security.dto.RateLimitConsumeResult

interface RateLimitService {
    fun tryConsume(apiKey: ApiKey): Boolean

    fun getRemainingTokens(apiKey: ApiKey): Long

    fun getSecondsUntilRefill(apiKey: ApiKey): Long

    fun tryConsumeAndReturnRemaining(apiKey: ApiKey): RateLimitConsumeResult
}
