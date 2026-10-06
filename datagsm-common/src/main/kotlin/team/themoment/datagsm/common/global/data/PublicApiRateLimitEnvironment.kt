package team.themoment.datagsm.common.global.data

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.security.public-api.rate-limit")
data class PublicApiRateLimitEnvironment(
    val enabled: Boolean = true,
    val capacity: Long = 60,
    val refillTokens: Long = 60,
    val refillDurationSeconds: Long = 60,
)
