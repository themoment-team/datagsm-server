package team.themoment.datagsm.web.domain.student.service

import team.themoment.datagsm.common.domain.student.dto.request.QueryStudentReqDto
import team.themoment.datagsm.common.domain.student.dto.response.StudentListResDto

interface QueryStudentService {
    fun execute(queryReq: QueryStudentReqDto): StudentListResDto
}
