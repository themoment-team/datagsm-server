package team.themoment.datagsm.domain.club.dto.response

import team.themoment.datagsm.domain.club.entity.constant.ClubType

data class ClubResDto(
    val clubId: Long,
    val clubName: String,
    val clubDescription: String,
    val clubType: ClubType,
)
