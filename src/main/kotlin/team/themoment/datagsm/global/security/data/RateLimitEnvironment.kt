package team.themoment.datagsm.global.security.data

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.security.rate-limit")
data class RateLimitEnvironment(
    val enabled: Boolean = true,
    val defaultCapacity: Long = 100,
    val defaultRefillTokens: Long = 100,
    val defaultRefillDurationSeconds: Long = 60,
)
