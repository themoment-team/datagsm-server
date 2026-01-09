package team.themoment.datagsm.common.global.data

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.security.jwt")
data class OauthJwtEnvironment(
    val secret: String,
    val oauthAccessTokenExpiration: Long,
    val oauthRefreshTokenExpiration: Long,
)
