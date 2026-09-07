package team.themoment.datagsm.common.domain.oauth.entity

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.TimeToLive

/**
 * BFF가 서버-투-서버로 호출하는 POST /authorize 응답에는 브라우저 쿠키를 심을 수 없다.
 * 세션 쿠키를 백엔드 도메인에 심기 위해 브라우저를 한 번 경유시키는 일회용 티켓이다.
 *
 * ticket은 URL에 노출되어 로그·Referer·브라우저 히스토리에 남으므로 그것만으로는 세션을 발급하지 않는다.
 * 정상 브라우저만 가진 verifier의 해시를 함께 저장해, 소비 시점에 대조한다.
 */
@RedisHash("idp_session_handoff")
data class IdpSessionHandoffRedisEntity(
    @Id
    val ticket: String,
    val verifierHash: String,
    val sessionId: String,
    val clientId: String,
    val redirectUrl: String,
    @TimeToLive
    val ttl: Long,
)
