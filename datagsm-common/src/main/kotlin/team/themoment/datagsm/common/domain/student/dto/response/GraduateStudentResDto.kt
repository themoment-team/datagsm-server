package team.themoment.datagsm.common.domain.student.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import team.themoment.datagsm.ksp.annotation.SdkExport

@SdkExport
data class GraduateStudentResDto(
    @field:Schema(description = "졸업 처리된 학생 수", example = "50")
    val graduatedCount: Int,
)
