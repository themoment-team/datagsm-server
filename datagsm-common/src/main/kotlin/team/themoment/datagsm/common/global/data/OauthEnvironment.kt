package team.themoment.datagsm.common.global.data

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.security.oauth")
data class OauthEnvironment(
    val codeExpirationSeconds: Long,
    val frontendUrl: String,
    val authorizeStateExpirationMs: Long,
    val issuerUrl: String,
    val idpSessionExpirationSeconds: Long,
    val idpSessionHandoffExpirationSeconds: Long,
    val idpSessionCookieName: String,
    val idpSessionCookieSecure: Boolean,
    val idpSessionHandoffRequireFetchMetadata: Boolean,
)
