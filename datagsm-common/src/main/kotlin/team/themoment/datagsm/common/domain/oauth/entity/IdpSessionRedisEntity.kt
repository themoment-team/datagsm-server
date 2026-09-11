package team.themoment.datagsm.common.domain.oauth.entity

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.TimeToLive
import org.springframework.data.redis.core.index.Indexed

@RedisHash("idp_session")
data class IdpSessionRedisEntity(
    @Id
    val sessionId: String,
    // 비밀번호 변경 시 해당 계정의 모든 세션을 끊어야 하므로 역방향 조회가 필요하다.
    @Indexed
    val email: String,
    @TimeToLive
    val ttl: Long,
)
