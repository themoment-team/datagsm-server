package team.themoment.datagsm.web.domain.student.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Size

data class UpdateMySpecialtyReqDto(
    @field:Size(max = 50)
    @param:Schema(description = "전공", example = "백엔드", maxLength = 50)
    val specialty: String?,
)
