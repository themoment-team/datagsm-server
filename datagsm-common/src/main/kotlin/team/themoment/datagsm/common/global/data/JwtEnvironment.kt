package team.themoment.datagsm.common.global.data

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.security.jwt")
data class JwtEnvironment(
    val secret: String,
    val accessTokenExpiration: Long?,
    val refreshTokenExpiration: Long?,
    val oauthAccessTokenExpiration: Long?,
    val oauthRefreshTokenExpiration: Long?,
)
