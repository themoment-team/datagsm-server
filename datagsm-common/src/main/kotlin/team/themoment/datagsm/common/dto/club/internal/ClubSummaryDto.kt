package team.themoment.datagsm.common.dto.club.internal

import io.swagger.v3.oas.annotations.media.Schema
import team.themoment.datagsm.common.domain.club.entity.constant.ClubType

data class ClubSummaryDto(
    @param:Schema(description = "동아리 ID", example = "1")
    val id: Long,
    @param:Schema(description = "동아리 이름", example = "SW개발동아리")
    val name: String,
    @param:Schema(description = "동아리 종류", example = "MAJOR_CLUB")
    val type: ClubType,
)
