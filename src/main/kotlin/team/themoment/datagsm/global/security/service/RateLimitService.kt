package team.themoment.datagsm.global.security.service

import team.themoment.datagsm.domain.auth.entity.ApiKey

interface RateLimitService {
    fun tryConsume(apiKey: ApiKey): Boolean

    fun getRemainingTokens(apiKey: ApiKey): Long

    fun getSecondsUntilRefill(apiKey: ApiKey): Long
}
