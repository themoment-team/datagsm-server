package team.themoment.datagsm.common.domain.client.dto.response

import io.swagger.v3.oas.annotations.media.Schema

data class ClientListResDto(
    @field:Schema(description = "전체 페이지 수", example = "5")
    val totalPages: Int,
    @field:Schema(description = "전체 클라이언트 수", example = "42")
    val totalElements: Long,
    @field:Schema(description = "클라이언트 목록")
    val clients: List<ClientResDto>,
)
