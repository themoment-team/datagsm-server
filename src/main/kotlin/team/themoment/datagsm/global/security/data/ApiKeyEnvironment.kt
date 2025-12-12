package team.themoment.datagsm.global.security.data

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.security.api-key")
data class ApiKeyEnvironment(
    val expirationDays: Long,
    val renewalPeriodDays: Long,
    val adminExpirationDays: Long,
    val adminRenewalPeriodDays: Long,
    val rateLimit: RateLimit,
) {
    data class RateLimit(
        val enabled: Boolean = true,
        val defaultCapacity: Long = 100,
        val defaultRefillTokens: Long = 100,
        val defaultRefillDurationSeconds: Long = 60,
    )
}
