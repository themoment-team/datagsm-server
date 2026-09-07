package team.themoment.datagsm.oauth.authorization.domain.oauth.service

import org.springframework.http.ResponseEntity

interface CompleteIdpSessionHandoffService {
    fun execute(
        ticket: String,
        verifier: String,
    ): ResponseEntity<Void>
}
