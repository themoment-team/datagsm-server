package team.themoment.datagsm.common.global.data

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.security.oauth")
data class OauthProperties(
    val codeExpirationSeconds: Long,
)
