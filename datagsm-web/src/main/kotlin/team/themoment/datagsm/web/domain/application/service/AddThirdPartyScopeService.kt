package team.themoment.datagsm.web.domain.application.service

import team.themoment.datagsm.common.domain.application.dto.request.AddThirdPartyScopeReqDto
import team.themoment.datagsm.common.domain.application.dto.response.ApplicationResDto

interface AddThirdPartyScopeService {
    fun execute(
        applicationId: String,
        reqDto: AddThirdPartyScopeReqDto,
    ): ApplicationResDto
}
