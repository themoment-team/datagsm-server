package team.themoment.datagsm.common.domain.account.entity

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.TimeToLive

@RedisHash("passwordResetCode")
data class PasswordResetCodeRedisEntity(
    @Id
    val email: String,
    val code: String,
    val verified: Boolean = false,
    @TimeToLive
    val ttl: Long,
)
