package team.themoment.datagsm.common.global.data

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.security.email-rate-limit")
data class EmailRateLimitEnvironment(
    val enabled: Boolean,
    val send: RateLimitConfig,
    val check: RateLimitConfig,
) {
    data class RateLimitConfig(
        val capacity: Long,
        val refillTokens: Long,
        val refillDurationMinutes: Long,
    )
}
