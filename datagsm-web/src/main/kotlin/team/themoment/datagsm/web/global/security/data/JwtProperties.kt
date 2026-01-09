package team.themoment.datagsm.web.global.security.data

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.security.jwt")
data class JwtProperties(
    val secret: String,
    val accessTokenExpiration: Long,
    val refreshTokenExpiration: Long,
)
