package team.themoment.datagsm.domain.auth.dto.response

import io.swagger.v3.oas.annotations.media.Schema

data class ApiScopeGroupResDto(
    @param:Schema(description = "카테고리 전체 권한 스코프", example = "student:*")
    val title: String,
    @param:Schema(description = "카테고리 전체 권한 설명", example = "학생 정보 모든 권한")
    val description: String,
    @param:Schema(description = "카테고리에 속한 세부 스코프 목록")
    val scopes: List<ApiScopeResDto>,
)