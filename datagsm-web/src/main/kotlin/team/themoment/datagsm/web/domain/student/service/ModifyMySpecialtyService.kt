package team.themoment.datagsm.web.domain.student.service

import team.themoment.datagsm.web.domain.student.dto.request.UpdateMySpecialtyReqDto

interface ModifyMySpecialtyService {
    fun execute(reqDto: UpdateMySpecialtyReqDto)
}
