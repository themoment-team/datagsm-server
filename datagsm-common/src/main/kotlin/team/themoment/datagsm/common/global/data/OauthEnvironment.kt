package team.themoment.datagsm.common.global.data

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.security.oauth")
data class OauthEnvironment(
    val codeExpirationSeconds: Long,
)
