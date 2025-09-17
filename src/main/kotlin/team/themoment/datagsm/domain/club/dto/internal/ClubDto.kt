package team.themoment.datagsm.domain.club.dto.internal

import team.themoment.datagsm.domain.club.entity.constant.ClubType

data class ClubDto(
    val clubId: Long,
    val clubName: String,
    val clubDescription: String,
    val clubType: ClubType,
)
