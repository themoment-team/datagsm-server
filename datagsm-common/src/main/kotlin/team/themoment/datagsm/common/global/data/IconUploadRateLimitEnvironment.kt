package team.themoment.datagsm.common.global.data

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.security.icon-upload.rate-limit")
data class IconUploadRateLimitEnvironment(
    val enabled: Boolean = true,
    val capacity: Long = 10,
    val refillTokens: Long = 10,
    val refillDurationSeconds: Long = 60,
)
