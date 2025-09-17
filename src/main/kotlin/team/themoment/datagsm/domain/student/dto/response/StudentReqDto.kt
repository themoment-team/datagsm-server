package team.themoment.datagsm.domain.student.dto.response

import team.themoment.datagsm.domain.student.dto.internal.StudentDto

data class StudentReqDto(
    val totalPages: Int,
    val totalElements: Long,
    val students: List<StudentDto>,
)
