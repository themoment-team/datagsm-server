package team.themoment.datagsm.web.global.data

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.security.oauth-jwt")
data class OauthJwtVerificationEnvironment(
    val publicKey: String,
    // 같은 인가 서버가 서드파티 클라이언트에도 토큰을 발급하므로, web을 위해 발급된 토큰만 받는다.
    val allowedClientIds: List<String>,
) {
    init {
        require(allowedClientIds.isNotEmpty() && allowedClientIds.all { it.isNotBlank() }) {
            "spring.security.oauth-jwt.allowed-client-ids must be a non-empty list of non-blank client ids"
        }
    }

    val allowedClientIdSet: Set<String>
        get() = allowedClientIds.map { it.trim() }.toSet()
}
