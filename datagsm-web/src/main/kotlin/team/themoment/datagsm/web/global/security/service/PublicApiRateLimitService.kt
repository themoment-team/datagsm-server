package team.themoment.datagsm.web.global.security.service

import team.themoment.datagsm.common.global.dto.internal.RateLimitConsumeResult

interface PublicApiRateLimitService {
    fun tryConsumeAndReturnRemaining(clientIp: String): RateLimitConsumeResult
}
