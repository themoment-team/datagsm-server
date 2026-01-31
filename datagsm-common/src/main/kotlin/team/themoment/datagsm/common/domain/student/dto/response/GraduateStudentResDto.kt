package team.themoment.datagsm.common.domain.student.dto.response

import io.swagger.v3.oas.annotations.media.Schema

data class GraduateStudentResDto(
    @param:Schema(description = "졸업 처리된 학생 수", example = "50")
    val graduatedCount: Int,
)
