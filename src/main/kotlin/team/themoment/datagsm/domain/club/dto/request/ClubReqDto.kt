package team.themoment.datagsm.domain.club.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import team.themoment.datagsm.domain.club.entity.constant.ClubType

data class ClubReqDto(
    @field:NotBlank
    @field:Size(max = 30)
    @param:Schema(description = "동아리 이름", example = "SW개발동아리", maxLength = 30)
    val clubName: String,
    @field:NotBlank
    @field:Size(max = 500)
    @param:Schema(description = "동아리 설명", example = "소프트웨어 개발을 공부하는 동아리입니다.", maxLength = 500)
    val clubDescription: String,
    @param:Schema(description = "동아리 종류", example = "MAJOR")
    val clubType: ClubType,
)
