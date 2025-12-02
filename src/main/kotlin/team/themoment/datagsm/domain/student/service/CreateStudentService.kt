package team.themoment.datagsm.domain.student.service

import team.themoment.datagsm.domain.student.dto.request.CreateStudentReqDto
import team.themoment.datagsm.domain.student.dto.response.StudentResDto

interface CreateStudentService {
    fun execute(reqDto: CreateStudentReqDto): StudentResDto
}
