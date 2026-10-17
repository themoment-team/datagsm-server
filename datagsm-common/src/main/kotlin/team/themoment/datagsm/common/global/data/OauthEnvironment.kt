package team.themoment.datagsm.common.global.data

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.security.oauth")
data class OauthEnvironment(
    val codeExpirationSeconds: Long,
    val frontendUrl: String,
    val authorizeStateExpirationMs: Long,
    val issuerUrl: String,
    // UserInfo는 별도 모듈(datagsm-oauth-userinfo)의 다른 호스트라 issuerUrl에서 유도할 수 없다.
    val userinfoUrl: String,
    val idpSessionExpirationSeconds: Long,
    val idpSessionHandoffExpirationSeconds: Long,
    val idpSessionCookieName: String,
    val idpSessionCookieSecure: Boolean,
    val idpSessionHandoffRequireFetchMetadata: Boolean,
)
