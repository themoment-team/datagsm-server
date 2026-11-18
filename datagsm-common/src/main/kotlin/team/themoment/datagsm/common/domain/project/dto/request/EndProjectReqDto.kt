package team.themoment.datagsm.common.domain.project.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Positive

data class EndProjectReqDto(
    @field:Positive
    @param:Schema(
        description = "프로젝트 종료 연도 (시작 연도 이상)",
        example = "2025",
        minimum = "1",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val endYear: Int,
)
