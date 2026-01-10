package team.themoment.datagsm.common.domain.club.dto.internal

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import team.themoment.datagsm.common.domain.club.entity.constant.ClubType

data class ClubInfoDto(
    @field:NotBlank
    @param:Schema(description = "동아리명", example = "더모먼트", maxLength = 50)
    val clubName: String,
    @field:NotBlank
    @param:Schema(description = "동아리 종류", example = "전공동아리")
    val clubType: ClubType,
    @param:Schema(description = "동아리 부장 정보", example = "2404 김태은")
    val leaderInfo: String? = null,
)
