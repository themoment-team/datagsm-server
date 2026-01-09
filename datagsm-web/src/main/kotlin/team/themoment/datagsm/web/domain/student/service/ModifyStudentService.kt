package team.themoment.datagsm.web.domain.student.service

import team.themoment.datagsm.common.dto.student.request.UpdateStudentReqDto
import team.themoment.datagsm.common.dto.student.response.StudentResDto

interface ModifyStudentService {
    fun execute(
        studentId: Long,
        reqDto: UpdateStudentReqDto,
    ): StudentResDto
}
