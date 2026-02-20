package team.themoment.datagsm.oauth.authorization.domain.oauth.service

import org.springframework.http.ResponseEntity
import team.themoment.datagsm.common.domain.oauth.dto.request.OauthAuthorizeReqDto

interface StartOauthAuthorizeFlowService {
    fun execute(reqDto: OauthAuthorizeReqDto): ResponseEntity<Void>
}
