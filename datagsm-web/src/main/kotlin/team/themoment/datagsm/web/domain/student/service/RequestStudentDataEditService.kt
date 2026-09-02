package team.themoment.datagsm.web.domain.student.service

import team.themoment.datagsm.common.domain.student.dto.request.RequestStudentDataEditReqDto

interface RequestStudentDataEditService {
    fun execute(reqDto: RequestStudentDataEditReqDto)
}
