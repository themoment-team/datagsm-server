package team.themoment.datagsm.web.domain.application.service

import team.themoment.datagsm.common.domain.application.dto.request.AddOAuthScopeReqDto
import team.themoment.datagsm.common.domain.application.dto.response.ApplicationResDto

interface AddOAuthScopeService {
    fun execute(
        applicationId: String,
        reqDto: AddOAuthScopeReqDto,
    ): ApplicationResDto
}
