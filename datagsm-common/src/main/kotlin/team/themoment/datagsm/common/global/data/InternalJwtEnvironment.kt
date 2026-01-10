package team.themoment.datagsm.common.global.data

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.security.internal-jwt")
data class InternalJwtEnvironment(
    val secret: String,
    val accessTokenExpiration: Long,
    val refreshTokenExpiration: Long,
)
