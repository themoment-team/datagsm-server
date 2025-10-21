package team.themoment.datagsm.domain.club.dto.request

import jakarta.validation.constraints.Size
import team.themoment.datagsm.domain.club.entity.constant.ClubType

data class ClubReqDto(
    @param:Size(max = 30)
    val clubName: String,
    @param:Size(max = 500)
    val clubDescription: String,
    val clubType: ClubType,
)
