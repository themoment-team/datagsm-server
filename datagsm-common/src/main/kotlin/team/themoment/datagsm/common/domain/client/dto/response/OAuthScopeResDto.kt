package team.themoment.datagsm.common.domain.client.dto.response

import io.swagger.v3.oas.annotations.media.Schema

data class OAuthScopeResDto(
    @field:Schema(description = "OAuth 권한 범위 이름", example = "self:read")
    val scope: String,
    @field:Schema(description = "OAuth 권한 범위 설명", example = "내 정보 조회")
    val description: String,
)
