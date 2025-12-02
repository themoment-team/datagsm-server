package team.themoment.datagsm.domain.student.service

import team.themoment.datagsm.domain.student.dto.request.UpdateStudentReqDto
import team.themoment.datagsm.domain.student.dto.response.StudentResDto

interface ModifyStudentService {
    fun execute(
        studentId: Long,
        reqDto: UpdateStudentReqDto,
    ): StudentResDto
}
