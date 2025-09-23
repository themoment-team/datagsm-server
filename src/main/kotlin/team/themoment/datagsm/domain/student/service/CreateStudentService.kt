package team.themoment.datagsm.domain.student.service

import team.themoment.datagsm.domain.student.dto.request.StudentReqDto
import team.themoment.datagsm.domain.student.dto.response.StudentResDto

interface CreateStudentService {
    fun createStudent(reqDto: StudentReqDto): StudentResDto
}
