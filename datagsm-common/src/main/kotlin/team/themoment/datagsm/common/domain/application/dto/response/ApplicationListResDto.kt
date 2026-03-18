package team.themoment.datagsm.common.domain.application.dto.response

import io.swagger.v3.oas.annotations.media.Schema

data class ApplicationListResDto(
    @field:Schema(description = "전체 페이지 수")
    val totalPages: Int,
    @field:Schema(description = "전체 요소 수")
    val totalElements: Long,
    @field:Schema(description = "Application 목록")
    val applications: List<ApplicationResDto>,
)
