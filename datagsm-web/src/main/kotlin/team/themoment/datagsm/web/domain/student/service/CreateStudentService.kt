package team.themoment.datagsm.web.domain.student.service

import team.themoment.datagsm.common.dto.student.request.CreateStudentReqDto
import team.themoment.datagsm.common.dto.student.response.StudentResDto

interface CreateStudentService {
    fun execute(reqDto: CreateStudentReqDto): StudentResDto
}
