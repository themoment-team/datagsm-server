package team.themoment.datagsm.domain.student.dto.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "학생 목록 응답 DTO")
data class StudentListResDto(
    @param:Schema(description = "전체 페이지 수", example = "1")
    val totalPages: Int,
    @param:Schema(description = "전체 학생 수", example = "100")
    val totalElements: Long,
    @param:Schema(description = "학생 목록")
    val students: List<StudentResDto>,
)
