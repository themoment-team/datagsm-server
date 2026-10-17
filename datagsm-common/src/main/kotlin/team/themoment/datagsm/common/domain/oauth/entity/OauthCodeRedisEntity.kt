package team.themoment.datagsm.common.domain.oauth.entity

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.TimeToLive

@RedisHash("oauthCode")
data class OauthCodeRedisEntity(
    val email: String,
    val clientId: String,
    val redirectUri: String?,
    val codeChallenge: String?,
    val codeChallengeMethod: String?,
    val scopes: Set<String>,
    // OIDC replay 방지 값. authorize에서 받아 id_token 클레임으로 그대로 되돌려준다.
    val nonce: String? = null,
    @Id
    val code: String,
    @TimeToLive
    val ttl: Long,
)
