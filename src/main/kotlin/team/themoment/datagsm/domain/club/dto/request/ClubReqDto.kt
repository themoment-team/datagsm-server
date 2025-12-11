package team.themoment.datagsm.domain.club.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import team.themoment.datagsm.domain.club.entity.constant.ClubType

data class ClubReqDto(
    @field:NotBlank
    @field:Size(max = 30)
    @param:Schema(description = "동아리 이름", example = "SW개발동아리", maxLength = 30)
    val name: String,
    @field:NotBlank
    @param:Schema(description = "동아리 종류", example = "MAJOR_CLUB")
    val type: ClubType,
)
