package team.themoment.datagsm.global.security.data

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.security.api-key")
data class ApiKeyEnvironment(
    val expirationDays: Long,
    val renewalPeriodDays: Long,
)
