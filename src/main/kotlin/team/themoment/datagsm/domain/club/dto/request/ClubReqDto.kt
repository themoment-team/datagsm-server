package team.themoment.datagsm.domain.club.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import team.themoment.datagsm.domain.club.entity.constant.ClubType

data class ClubReqDto(
    @param:NotBlank
    @param:Size(max = 30)
    val clubName: String,
    @param:NotBlank
    @param:Size(max = 500)
    val clubDescription: String,
    val clubType: ClubType,
)
