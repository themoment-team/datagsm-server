package team.themoment.datagsm.common.dto.auth.response

import io.swagger.v3.oas.annotations.media.Schema

data class ApiKeySearchResDto(
    @param:Schema(description = "전체 페이지 수", example = "1")
    val totalPages: Int,
    @param:Schema(description = "전체 API 키 수", example = "20")
    val totalElements: Long,
    @param:Schema(description = "API 키 목록")
    val apiKeys: List<ApiKeyResDto>,
)
