package team.themoment.datagsm.common.domain.club.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import team.themoment.datagsm.common.domain.club.dto.internal.ClubSummaryDto

data class ClubSummaryListResDto(
    @field:Schema(description = "동아리 목록")
    val clubs: List<ClubSummaryDto>,
)
