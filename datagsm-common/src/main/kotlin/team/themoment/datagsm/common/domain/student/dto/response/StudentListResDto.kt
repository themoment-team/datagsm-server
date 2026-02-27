package team.themoment.datagsm.common.domain.student.dto.response

import io.swagger.v3.oas.annotations.media.Schema

data class StudentListResDto(
    @field:Schema(description = "전체 페이지 수", example = "1")
    val totalPages: Int,
    @field:Schema(description = "전체 학생 수", example = "100")
    val totalElements: Long,
    @field:Schema(description = "학생 목록")
    val students: List<StudentResDto>,
)
