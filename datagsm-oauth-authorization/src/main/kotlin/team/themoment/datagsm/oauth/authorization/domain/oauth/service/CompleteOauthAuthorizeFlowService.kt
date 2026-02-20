package team.themoment.datagsm.oauth.authorization.domain.oauth.service

import org.springframework.http.ResponseEntity
import team.themoment.datagsm.common.domain.oauth.dto.request.OauthAuthorizeSubmitReqDto

interface CompleteOauthAuthorizeFlowService {
    fun execute(reqDto: OauthAuthorizeSubmitReqDto): ResponseEntity<Void>
}
