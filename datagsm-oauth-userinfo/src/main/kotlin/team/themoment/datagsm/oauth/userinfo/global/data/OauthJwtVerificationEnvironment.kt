package team.themoment.datagsm.oauth.userinfo.global.data

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.security.oauth-jwt")
data class OauthJwtVerificationEnvironment(
    val publicKey: String,
    val datagsmApplicationId: String,
)
