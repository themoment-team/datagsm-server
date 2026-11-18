package team.themoment.datagsm.common.domain.student.dto.internal

import io.swagger.v3.oas.annotations.media.Schema

data class BatchOperationFilter(
    @param:Schema(
        description = "처리할 학년 (생략 또는 null이면 3학년, 범위 검증 없음)",
        example = "3",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
    )
    val grade: Int? = null,
)
