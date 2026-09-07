package team.themoment.datagsm.common.domain.oauth.entity

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.TimeToLive

/**
 * BFF가 서버-투-서버로 호출하는 POST /authorize 응답에는 브라우저 쿠키를 심을 수 없다.
 * 세션 쿠키를 백엔드 도메인에 심기 위해 브라우저를 한 번 경유시키는 일회용 티켓이다.
 */
@RedisHash("idp_session_handoff")
data class IdpSessionHandoffRedisEntity(
    @Id
    val ticket: String,
    val sessionId: String,
    val redirectUrl: String,
    @TimeToLive
    val ttl: Long,
)
