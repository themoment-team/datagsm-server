package team.themoment.datagsm.domain.account.entity

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.TimeToLive

@RedisHash("emailCode")
data class EmailCodeRedisEntity(
    @Id
    val email: String,
    val code: String,
    @TimeToLive
    val ttl: Long,
)
