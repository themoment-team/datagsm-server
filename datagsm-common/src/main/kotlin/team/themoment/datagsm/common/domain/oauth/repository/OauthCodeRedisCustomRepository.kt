package team.themoment.datagsm.common.domain.oauth.repository

import java.time.Duration

interface OauthCodeRedisCustomRepository {
    /**
     * 인가 코드의 사용권을 원자적으로 선점한다. 같은 코드에 대해 처음 호출한 쪽만 true를 받는다.
     */
    fun claimUsage(
        code: String,
        clientId: String,
        ttl: Duration,
    ): Boolean

    fun releaseUsage(code: String)
}
