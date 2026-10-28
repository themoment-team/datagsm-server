package team.themoment.datagsm.oauth.authorization.domain.oauth.service

import org.springframework.http.ResponseEntity

interface DeleteIdpSessionService {
    fun execute(
        targetSessionHandle: String,
        sessionId: String?,
    ): ResponseEntity<Void>
}
