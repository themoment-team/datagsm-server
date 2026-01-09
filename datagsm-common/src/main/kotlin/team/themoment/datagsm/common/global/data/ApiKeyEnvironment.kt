package team.themoment.datagsm.common.global.data

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.security.api-key")
data class ApiKeyEnvironment(
    val expirationDays: Long?,
    val renewalPeriodDays: Long?,
    val adminExpirationDays: Long?,
    val rateLimit: RateLimit?,
) {
    data class RateLimit(
        val enabled: Boolean?,
        val defaultCapacity: Long?,
        val defaultRefillTokens: Long?,
        val defaultRefillDurationSeconds: Long?,
    )
}
