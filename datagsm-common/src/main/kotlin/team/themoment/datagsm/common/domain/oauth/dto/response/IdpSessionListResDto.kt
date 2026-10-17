package team.themoment.datagsm.common.domain.oauth.dto.response

import io.swagger.v3.oas.annotations.media.Schema

data class IdpSessionListResDto(
    @field:Schema(description = "활성 세션 목록. 최근 로그인 순으로 정렬됩니다.")
    val sessions: List<IdpSessionResDto>,
)
