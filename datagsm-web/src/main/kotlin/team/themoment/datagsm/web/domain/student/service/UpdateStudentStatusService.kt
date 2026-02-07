package team.themoment.datagsm.web.domain.student.service

import team.themoment.datagsm.common.domain.student.dto.request.UpdateStudentStatusReqDto

interface UpdateStudentStatusService {
    fun execute(
        studentId: Long,
        reqDto: UpdateStudentStatusReqDto,
    )
}
