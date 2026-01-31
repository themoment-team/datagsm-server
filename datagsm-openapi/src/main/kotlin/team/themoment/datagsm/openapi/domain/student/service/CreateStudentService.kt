package team.themoment.datagsm.resource.domain.student.service

import team.themoment.datagsm.common.domain.student.dto.request.CreateStudentReqDto
import team.themoment.datagsm.common.domain.student.dto.response.StudentResDto

interface CreateStudentService {
    fun execute(reqDto: CreateStudentReqDto): StudentResDto
}
