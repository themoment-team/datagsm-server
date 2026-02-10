package team.themoment.datagsm.oauth.authorization.domain.oauth.service

import jakarta.servlet.http.HttpSession
import org.springframework.http.ResponseEntity

interface StartOauthAuthorizeFlowService {
    fun execute(
        clientId: String?,
        redirectUri: String?,
        responseType: String?,
        state: String?,
        codeChallenge: String?,
        codeChallengeMethod: String?,
        session: HttpSession,
    ): ResponseEntity<Void>
}
