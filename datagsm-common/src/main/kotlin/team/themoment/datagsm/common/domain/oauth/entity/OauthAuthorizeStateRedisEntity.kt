package team.themoment.datagsm.common.domain.oauth.entity

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.TimeToLive

@RedisHash("oauth_authorize_state")
data class OauthAuthorizeStateRedisEntity(
    @Id
    val token: String,
    val clientId: String,
    val redirectUri: String,
    val state: String?,
    val codeChallenge: String?,
    val codeChallengeMethod: String?,
    @TimeToLive
    val ttl: Long = 600,
)
