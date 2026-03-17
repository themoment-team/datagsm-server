package team.themoment.datagsm.web.domain.application.service

import team.themoment.datagsm.common.domain.application.dto.request.CreateApplicationReqDto
import team.themoment.datagsm.common.domain.application.dto.response.ApplicationResDto

interface CreateApplicationService {
    fun execute(reqDto: CreateApplicationReqDto): ApplicationResDto
}
