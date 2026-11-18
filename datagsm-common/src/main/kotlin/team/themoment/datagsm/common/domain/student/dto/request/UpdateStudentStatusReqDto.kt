package team.themoment.datagsm.common.domain.student.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import team.themoment.datagsm.common.domain.student.entity.constant.StudentRole

data class UpdateStudentStatusReqDto(
    @field:NotNull(message = "상태는 필수입니다.")
    @param:Schema(
        description = "변경할 학생 역할 (졸업·자퇴 시 학적·동아리는 초기화하지만 전공은 유지)",
        example = "GRADUATE",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val status: StudentRole,
)
