package team.themoment.datagsm.common.domain.project.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RejectProjectRequestReqDto(
    @field:NotBlank
    @field:Size(max = 500)
    @param:Schema(description = "거절 사유", example = "리포지토리 링크가 유효하지 않습니다.", maxLength = 500)
    val reason: String,
)
