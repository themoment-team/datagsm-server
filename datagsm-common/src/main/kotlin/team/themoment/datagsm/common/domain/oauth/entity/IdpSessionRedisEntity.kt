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
    // 사용자가 "어떤 기기에서 로그인 중인지" 구분할 근거.
    // 기존 세션에는 없는 값이라 null을 허용한다.
    val userAgent: String? = null,
    val createdAt: Long? = null,
    @TimeToLive
    val ttl: Long,
)
