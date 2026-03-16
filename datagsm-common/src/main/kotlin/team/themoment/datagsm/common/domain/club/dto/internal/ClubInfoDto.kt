package team.themoment.datagsm.common.domain.club.dto.internal

import io.swagger.v3.oas.annotations.media.Schema
import team.themoment.datagsm.common.domain.club.entity.constant.ClubStatus
import team.themoment.datagsm.common.domain.club.entity.constant.ClubType

data class ClubInfoDto(
    @field:Schema(description = "동아리명", example = "더모먼트", maxLength = 50)
    val clubName: String,
    @field:Schema(description = "동아리 종류", example = "MAJOR_CLUB")
    val clubType: ClubType,
    @field:Schema(description = "동아리 부장 정보", example = "2404 김태은")
    val leaderInfo: String? = null,
    @field:Schema(description = "창설 학년도", example = "2022")
    val foundedYear: Int,
    @field:Schema(description = "운영 상태", example = "ACTIVE")
    val status: ClubStatus,
    @field:Schema(description = "폐지 학년도", example = "2024")
    val abolishedYear: Int? = null,
)
