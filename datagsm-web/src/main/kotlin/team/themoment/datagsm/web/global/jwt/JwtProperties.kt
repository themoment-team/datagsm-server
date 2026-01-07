package team.themoment.datagsm.web.global.jwt

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.security.jwt")
data class JwtProperties(
    val secret: String,
    val accessTokenExpiration: Long,
    val refreshTokenExpiration: Long,
    val oauthAccessTokenExpiration: Long,
    val oauthRefreshTokenExpiration: Long,
)
