package team.themoment.datagsm.common.domain.auth.dto.response

import io.swagger.v3.oas.annotations.media.Schema

data class ApiKeySearchResDto(
    @field:Schema(description = "전체 페이지 수", example = "1")
    val totalPages: Int,
    @field:Schema(description = "전체 API 키 수", example = "20")
    val totalElements: Long,
    @field:Schema(description = "API 키 목록")
    val apiKeys: List<ApiKeyResDto>,
)
