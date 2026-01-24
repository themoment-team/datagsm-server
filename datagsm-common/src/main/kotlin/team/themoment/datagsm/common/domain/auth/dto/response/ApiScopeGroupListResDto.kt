package team.themoment.datagsm.common.domain.auth.dto.response

import io.swagger.v3.oas.annotations.media.Schema

data class ApiScopeGroupListResDto(
    val list: List<ApiScopeGroupResDto>,
) {
    data class ApiScopeGroupResDto(
        @param:Schema(description = "카테고리 이름", example = "학생")
        val title: String,
        @param:Schema(description = "카테고리에 속한 모든 권한 범위 목록 (와일드카드 포함)")
        val scopes: List<ApiScopeResDto>,
    )
}
