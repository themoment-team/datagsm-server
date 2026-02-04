package team.themoment.datagsm.oauth.authorization.global.security.service

import team.themoment.datagsm.common.global.dto.internal.RateLimitConsumeResult
import team.themoment.datagsm.oauth.authorization.global.security.annotation.PasswordResetRateLimitType

interface PasswordResetRateLimitService {
    fun tryConsume(
        email: String,
        type: PasswordResetRateLimitType,
    ): RateLimitConsumeResult
}
