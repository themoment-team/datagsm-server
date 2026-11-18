package team.themoment.datagsm.common.domain.student.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import team.themoment.datagsm.common.domain.student.dto.internal.BatchOperationFilter
import team.themoment.datagsm.common.domain.student.dto.internal.BatchOperationType

data class BatchOperationReqDto(
    @field:NotNull(message = "작업 타입은 필수입니다.")
    @param:Schema(
        description = "일괄 작업 종류",
        allowableValues = ["GRADUATE"],
        example = "GRADUATE",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val operation: BatchOperationType,
    @param:Schema(description = "대상 필터 (생략 또는 null이면 3학년)", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    val filter: BatchOperationFilter? = null,
)
