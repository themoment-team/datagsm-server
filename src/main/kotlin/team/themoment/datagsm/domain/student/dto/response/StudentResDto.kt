package team.themoment.datagsm.domain.student.dto.response

import team.themoment.datagsm.domain.student.dto.internal.StudentDto

data class StudentResDto(
    val totalPages: Int,
    val totalElements: Long,
    val students: List<StudentDto>,
)
