package team.themoment.datagsm.domain.oauth.property

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.security.oauth")
data class OauthProperties(
    val codeExpirationSeconds: Long,
)
