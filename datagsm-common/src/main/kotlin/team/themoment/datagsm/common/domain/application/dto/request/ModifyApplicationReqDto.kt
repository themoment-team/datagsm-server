package team.themoment.datagsm.common.domain.application.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ModifyApplicationReqDto(
    @field:NotBlank
    @field:Size(max = 100)
    @param:Schema(description = "Application 이름", example = "My Application", maxLength = 100)
    val name: String,
)
