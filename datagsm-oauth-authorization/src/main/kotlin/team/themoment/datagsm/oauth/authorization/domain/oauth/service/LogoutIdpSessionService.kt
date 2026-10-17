package team.themoment.datagsm.oauth.authorization.domain.oauth.service

import org.springframework.http.ResponseEntity

interface LogoutIdpSessionService {
    fun execute(sessionId: String?): ResponseEntity<Void>
}
