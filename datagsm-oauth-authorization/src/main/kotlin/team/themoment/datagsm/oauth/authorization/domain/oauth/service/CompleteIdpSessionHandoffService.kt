package team.themoment.datagsm.oauth.authorization.domain.oauth.service

import org.springframework.http.ResponseEntity

interface CompleteIdpSessionHandoffService {
    fun execute(
        ticket: String,
        verifier: String?,
        secFetchSite: String?,
        secFetchMode: String?,
        userAgent: String? = null,
    ): ResponseEntity<Void>
}
