package team.themoment.datagsm.domain.club.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import team.themoment.datagsm.domain.club.entity.constant.ClubType

data class ClubResDto(
    @param:Schema(description = "동아리 ID", example = "1")
    val clubId: Long,
    @param:Schema(description = "동아리 이름", example = "SW개발동아리")
    val clubName: String,
    @param:Schema(description = "동아리 설명", example = "소프트웨어 개발을 공부하는 동아리입니다.")
    val clubDescription: String,
    @param:Schema(description = "동아리 종류", example = "MAJOR")
    val clubType: ClubType,
)
