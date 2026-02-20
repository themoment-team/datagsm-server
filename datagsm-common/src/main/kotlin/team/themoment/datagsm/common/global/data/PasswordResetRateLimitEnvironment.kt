package team.themoment.datagsm.common.global.data

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.security.password-reset-rate-limit")
data class PasswordResetRateLimitEnvironment(
    val enabled: Boolean,
    val send: RateLimitConfig,
    val verify: RateLimitConfig,
    val change: RateLimitConfig,
    val signupSend: RateLimitConfig,
    val signupCheck: RateLimitConfig,
) {
    data class RateLimitConfig(
        val capacity: Long,
        val refillTokens: Long,
        val refillDurationMinutes: Long,
    )
}
