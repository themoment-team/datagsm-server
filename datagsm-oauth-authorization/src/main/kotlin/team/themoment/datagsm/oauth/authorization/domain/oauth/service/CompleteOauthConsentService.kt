package team.themoment.datagsm.oauth.authorization.domain.oauth.service

import org.springframework.http.ResponseEntity
import team.themoment.datagsm.common.domain.oauth.dto.request.OauthConsentReqDto

interface CompleteOauthConsentService {
    fun execute(
        reqDto: OauthConsentReqDto,
        sessionId: String?,
    ): ResponseEntity<Void>
}
