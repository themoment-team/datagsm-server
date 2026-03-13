package team.themoment.datagsm.common.global.data

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.security.oauth-jwt")
data class OauthJwtEnvironment(
    val privateKey: String,
    val publicKey: String,
    val keyId: String,
    val accessTokenExpiration: Long,
    val refreshTokenExpiration: Long,
)
