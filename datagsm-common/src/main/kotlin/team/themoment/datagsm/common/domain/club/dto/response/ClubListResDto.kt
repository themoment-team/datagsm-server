package team.themoment.datagsm.common.domain.club.dto.response

import io.swagger.v3.oas.annotations.media.Schema

data class ClubListResDto(
    @field:Schema(description = "전체 페이지 수", example = "1")
    val totalPages: Int,
    @field:Schema(description = "전체 동아리 수", example = "20")
    val totalElements: Long,
    @field:Schema(description = "동아리 목록")
    val clubs: List<ClubResDto>,
)
