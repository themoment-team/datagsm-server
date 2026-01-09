package team.themoment.datagsm.web.domain.client.dto.response

import io.swagger.v3.oas.annotations.media.Schema

data class ClientListResDto(
    @param:Schema(description = "전체 페이지 수", example = "5")
    val totalPages: Int,
    @param:Schema(description = "전체 클라이언트 수", example = "42")
    val totalElements: Long,
    @param:Schema(description = "클라이언트 목록")
    val clients: List<ClientResDto>,
)
