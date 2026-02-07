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
    @Id
    val code: String,
    @TimeToLive
    val ttl: Long,
)
