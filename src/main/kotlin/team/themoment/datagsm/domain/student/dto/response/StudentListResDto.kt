package team.themoment.datagsm.domain.student.dto.response

import team.themoment.datagsm.domain.student.dto.response.StudentResDto

data class StudentListResDto(
    val totalPages: Int,
    val totalElements: Long,
    val students: List<StudentResDto>,
)
