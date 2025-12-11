package team.themoment.datagsm.domain.club.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import team.themoment.datagsm.domain.club.entity.constant.ClubType

data class ClubResDto(
    @param:Schema(description = "동아리 ID", example = "1")
    val id: Long,
    @param:Schema(description = "동아리 이름", example = "SW개발동아리")
    val name: String,
    @param:Schema(description = "동아리 종류", example = "MAJOR_CLUB")
    val type: ClubType,
)
