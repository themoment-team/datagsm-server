package team.themoment.datagsm.common.domain.account.entity

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.TimeToLive
import org.springframework.data.redis.core.index.Indexed

@RedisHash("refreshToken")
data class RefreshTokenRedisEntity(
    @Id
    val id: String,
    @Indexed
    val email: String,
    val token: String,
    @TimeToLive
    val ttl: Long,
) {
    companion object {
        fun of(
            email: String,
            token: String,
            ttl: Long,
        ): RefreshTokenRedisEntity =
            RefreshTokenRedisEntity(
                id = email,
                email = email,
                token = token,
                ttl = ttl,
            )
    }
}
