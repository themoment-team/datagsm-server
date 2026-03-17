package team.themoment.datagsm.web.global.data

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.security.oauth-jwt")
data class OauthJwtVerificationEnvironment(
    val publicKey: String,
)
