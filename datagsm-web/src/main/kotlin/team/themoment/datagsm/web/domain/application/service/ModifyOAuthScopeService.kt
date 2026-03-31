package team.themoment.datagsm.web.domain.application.service

import team.themoment.datagsm.common.domain.application.dto.request.ModifyOAuthScopeReqDto
import team.themoment.datagsm.common.domain.application.dto.response.ApplicationResDto

interface ModifyOAuthScopeService {
    fun execute(
        applicationId: String,
        scopeId: Long,
        reqDto: ModifyOAuthScopeReqDto,
    ): ApplicationResDto
}
