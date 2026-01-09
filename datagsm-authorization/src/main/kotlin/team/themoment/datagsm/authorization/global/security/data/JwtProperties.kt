package team.themoment.datagsm.authorization.global.security.data

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.security.jwt")
data class JwtProperties(
    val secret: String,
    val oauthAccessTokenExpiration: Long,
    val oauthRefreshTokenExpiration: Long,
)
