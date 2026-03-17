package team.themoment.datagsm.web.domain.application.service

import team.themoment.datagsm.common.domain.application.dto.request.ModifyThirdPartyScopeReqDto
import team.themoment.datagsm.common.domain.application.dto.response.ApplicationResDto

interface ModifyThirdPartyScopeService {
    fun execute(
        applicationId: String,
        scopeId: Long,
        reqDto: ModifyThirdPartyScopeReqDto,
    ): ApplicationResDto
}
