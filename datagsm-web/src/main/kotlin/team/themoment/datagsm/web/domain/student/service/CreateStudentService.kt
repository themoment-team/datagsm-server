package team.themoment.datagsm.web.domain.student.service

import team.themoment.datagsm.web.domain.student.dto.request.CreateStudentReqDto
import team.themoment.datagsm.web.domain.student.dto.response.StudentResDto

interface CreateStudentService {
    fun execute(reqDto: CreateStudentReqDto): StudentResDto
}
