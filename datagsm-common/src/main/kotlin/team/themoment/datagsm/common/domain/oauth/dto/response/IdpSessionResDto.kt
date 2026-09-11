package team.themoment.datagsm.common.domain.oauth.dto.response

import io.swagger.v3.oas.annotations.media.Schema

data class IdpSessionResDto(
    @field:Schema(description = "세션 식별자. 개별 종료 요청에 사용합니다.")
    val sessionId: String,
    @field:Schema(description = "로그인한 기기의 User-Agent. 기록되지 않은 세션은 null입니다.")
    val userAgent: String?,
    @field:Schema(description = "로그인 시각(epoch milliseconds). 기록되지 않은 세션은 null입니다.")
    val createdAt: Long?,
    @field:Schema(description = "현재 요청에 사용된 세션인지 여부")
    val current: Boolean,
)
