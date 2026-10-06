package team.themoment.datagsm.oauth.authorization.domain.oauth.service

import org.springframework.http.ResponseEntity

interface DeleteIdpSessionService {
    fun execute(
        targetSessionId: String,
        sessionId: String?,
    ): ResponseEntity<Void>
}
