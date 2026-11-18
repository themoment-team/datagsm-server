package team.themoment.datagsm.common.domain.client.dto.response

import io.swagger.v3.oas.annotations.media.Schema

data class OAuthScopeListResDto(
    @field:Schema(description = "애플리케이션 이름을 포함한 사용 가능 OAuth 권한 범위의 평면 목록")
    val list: List<OAuthScopeResDto>,
)
