package team.themoment.datagsm.common.domain.auth.dto.response

import io.swagger.v3.oas.annotations.media.Schema

data class ApiScopeGroupListResDto(
    @field:Schema(description = "카테고리별 권한 범위 목록 (ROOT 역할 조회 시 빈 목록)")
    val list: List<ApiScopeGroupResDto>,
) {
    data class ApiScopeGroupResDto(
        @field:Schema(description = "카테고리 이름", example = "학생")
        val title: String,
        @field:Schema(description = "카테고리에 속한 모든 권한 범위 목록 (와일드카드 포함)")
        val scopes: List<ApiScopeResDto>,
    )
}
