package team.themoment.datagsm.domain.student.service

import team.themoment.datagsm.domain.student.dto.request.StudentCreateReqDto
import team.themoment.datagsm.domain.student.dto.response.StudentResDto

interface CreateStudentService {
    fun createStudent(reqDto: StudentCreateReqDto): StudentResDto
}
