package team.themoment.datagsm.common.domain.student.dto.request

import jakarta.validation.constraints.NotNull
import team.themoment.datagsm.common.domain.student.entity.constant.StudentRole

data class UpdateStudentStatusReqDto(
    @field:NotNull(message = "상태는 필수입니다.")
    val status: StudentRole,
)
