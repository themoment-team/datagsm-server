package team.themoment.datagsm.oauth.authorization.domain.oauth.service

import team.themoment.datagsm.common.domain.student.dto.request.QueryStudentDataEditRequestReqDto
import team.themoment.datagsm.common.domain.student.dto.response.StudentDataEditRequestResDto

interface QueryStudentDataEditRequestService {
    fun execute(reqDto: QueryStudentDataEditRequestReqDto): StudentDataEditRequestResDto
}
