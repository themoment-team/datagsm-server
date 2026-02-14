package team.themoment.datagsm.oauth.authorization.domain.oauth.service

import org.springframework.http.ResponseEntity

interface StartOauthAuthorizeFlowService {
    fun execute(
        clientId: String?,
        redirectUri: String?,
        responseType: String?,
        state: String?,
        codeChallenge: String?,
        codeChallengeMethod: String?,
    ): ResponseEntity<Void>
}
