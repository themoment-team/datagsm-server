package team.themoment.datagsm.web.domain.application.service

import team.themoment.datagsm.common.domain.application.dto.request.ModifyApplicationReqDto
import team.themoment.datagsm.common.domain.application.dto.response.ApplicationResDto

interface ModifyApplicationService {
    fun execute(
        id: String,
        reqDto: ModifyApplicationReqDto,
    ): ApplicationResDto
}
