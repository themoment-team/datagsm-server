package team.themoment.datagsm.openapi.domain.student.service

import team.themoment.datagsm.common.domain.student.dto.request.UpdateStudentReqDto
import team.themoment.datagsm.common.domain.student.dto.response.StudentResDto

interface ModifyStudentService {
    fun execute(
        studentId: Long,
        reqDto: UpdateStudentReqDto,
    ): StudentResDto
}
