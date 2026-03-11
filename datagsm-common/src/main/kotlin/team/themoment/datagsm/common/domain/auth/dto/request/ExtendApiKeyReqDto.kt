package team.themoment.datagsm.common.domain.auth.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

data class ExtendApiKeyReqDto(
    @field:Min(1, message = "연장 일수는 1일 이상이어야 합니다.")
    @field:Max(365, message = "연장 일수는 365일 이하이어야 합니다.")
    @param:Schema(description = "연장할 일수", example = "30", minimum = "1", maximum = "365")
    val days: Long,
)
