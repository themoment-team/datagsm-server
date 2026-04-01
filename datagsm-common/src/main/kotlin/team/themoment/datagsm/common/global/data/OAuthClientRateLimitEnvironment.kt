package team.themoment.datagsm.common.global.data

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.security.oauth-client.rate-limit")
data class OAuthClientRateLimitEnvironment(
    val enabled: Boolean,
    val capacity: Long,
    val refillTokens: Long,
    val refillDurationSeconds: Long,
)
