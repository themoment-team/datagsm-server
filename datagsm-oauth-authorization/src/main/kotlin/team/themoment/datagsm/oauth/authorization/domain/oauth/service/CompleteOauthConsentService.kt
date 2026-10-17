package team.themoment.datagsm.oauth.authorization.domain.oauth.service

import team.themoment.datagsm.common.domain.oauth.dto.request.OauthConsentReqDto
import team.themoment.datagsm.common.domain.oauth.dto.response.OauthConsentResDto

interface CompleteOauthConsentService {
    fun execute(
        reqDto: OauthConsentReqDto,
        sessionId: String?,
    ): OauthConsentResDto
}
