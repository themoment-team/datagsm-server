package team.themoment.datagsm.common.domain.student.dto.request

import jakarta.validation.constraints.NotNull

data class BatchOperationReqDto(
    @field:NotNull(message = "작업 타입은 필수입니다.")
    var operation: BatchOperationType,
    val filter: BatchOperationFilter? = null,
)
