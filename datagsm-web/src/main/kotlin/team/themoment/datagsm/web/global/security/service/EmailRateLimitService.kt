package team.themoment.datagsm.web.global.security.service

import team.themoment.datagsm.common.global.dto.internal.RateLimitConsumeResult
import team.themoment.datagsm.web.global.security.annotation.EmailRateLimitType

interface EmailRateLimitService {
    /**
     * 이메일 관련 rate limit을 체크하고 토큰을 소비합니다.
     * @param email 이메일 주소
     * @param type Rate Limit 타입 (SEND_EMAIL, CHECK_EMAIL)
     * @return RateLimitConsumeResult 소비 결과
     */
    fun tryConsume(
        email: String,
        type: EmailRateLimitType,
    ): RateLimitConsumeResult
}
