package team.themoment.datagsm.common.domain.club.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import team.themoment.datagsm.common.domain.club.dto.internal.ClubSummaryDto
import team.themoment.datagsm.ksp.annotation.SdkExport

@SdkExport
data class PublicClubListResDto(
    @field:Schema(description = "동아리 목록")
    val clubs: List<ClubSummaryDto>,
)
