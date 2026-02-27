package team.themoment.datagsm.common.domain.client.dto.response

import io.swagger.v3.oas.annotations.media.Schema

data class OAuthScopeGroupListResDto(
    val list: List<OAuthScopeGroupResDto>,
) {
    data class OAuthScopeGroupResDto(
        @field:Schema(description = "카테고리 이름", example = "사용자")
        val title: String,
        @field:Schema(description = "카테고리에 속한 OAuth 권한 범위 목록")
        val scopes: List<OAuthScopeResDto>,
    )
}
