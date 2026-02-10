package team.themoment.datagsm.oauth.authorization.domain.oauth.service

import jakarta.servlet.http.HttpSession
import org.springframework.http.ResponseEntity
import team.themoment.datagsm.common.domain.oauth.dto.request.OauthAuthorizeSubmitReqDto

interface CompleteOauthAuthorizeFlowService {
    fun execute(
        reqDto: OauthAuthorizeSubmitReqDto,
        session: HttpSession,
    ): ResponseEntity<Void>
}
