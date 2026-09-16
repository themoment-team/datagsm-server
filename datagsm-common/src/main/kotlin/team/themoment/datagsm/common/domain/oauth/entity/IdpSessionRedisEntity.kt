package team.themoment.datagsm.common.domain.oauth.entity

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.TimeToLive

@RedisHash("idp_session")
data class IdpSessionRedisEntity(
    @Id
    val sessionId: String,
    val email: String,
    @TimeToLive
    val ttl: Long,
)
