package team.themoment.datagsm.common.domain.student.dto.request

import jakarta.validation.constraints.NotNull
import team.themoment.datagsm.common.domain.student.dto.internal.BatchOperationFilter
import team.themoment.datagsm.common.domain.student.dto.internal.BatchOperationType

data class BatchOperationReqDto(
    @field:NotNull(message = "작업 타입은 필수입니다.")
    val operation: BatchOperationType,
    val filter: BatchOperationFilter? = null,
)
