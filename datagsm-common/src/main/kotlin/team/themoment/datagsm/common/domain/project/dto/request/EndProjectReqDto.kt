package team.themoment.datagsm.common.domain.project.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Positive

data class EndProjectReqDto(
    @field:Positive
    @param:Schema(description = "프로젝트 종료 연도", example = "2025")
    val endYear: Int,
)
