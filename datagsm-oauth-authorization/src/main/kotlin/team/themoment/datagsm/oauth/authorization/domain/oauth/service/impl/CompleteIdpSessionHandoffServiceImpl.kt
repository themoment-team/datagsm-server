package team.themoment.datagsm.oauth.authorization.domain.oauth.service.impl

import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import team.themoment.datagsm.common.domain.oauth.exception.OAuthException
import team.themoment.datagsm.common.domain.oauth.repository.IdpSessionHandoffRedisRepository
import team.themoment.datagsm.common.global.data.OauthEnvironment
import team.themoment.datagsm.oauth.authorization.domain.oauth.service.CompleteIdpSessionHandoffService
import java.net.URI
import java.time.Duration

@Service
class CompleteIdpSessionHandoffServiceImpl(
    private val idpSessionHandoffRedisRepository: IdpSessionHandoffRedisRepository,
    private val oauthEnvironment: OauthEnvironment,
) : CompleteIdpSessionHandoffService {
    override fun execute(ticket: String): ResponseEntity<Void> {
        val handoff =
            idpSessionHandoffRedisRepository.findByIdOrNull(ticket)
                ?: throw OAuthException.InvalidRequest("인증 티켓이 유효하지 않거나 만료되었습니다. 다시 시도해주세요.")

        idpSessionHandoffRedisRepository.deleteById(ticket)

        val cookie =
            ResponseCookie
                .from(oauthEnvironment.idpSessionCookieName, handoff.sessionId)
                .httpOnly(true)
                .secure(oauthEnvironment.idpSessionCookieSecure)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofSeconds(oauthEnvironment.idpSessionExpirationSeconds))
                .also { builder ->
                    oauthEnvironment.idpSessionCookieDomain
                        ?.takeIf { it.isNotBlank() }
                        ?.let { builder.domain(it) }
                }.build()

        return ResponseEntity
            .status(HttpStatus.FOUND)
            .header(HttpHeaders.SET_COOKIE, cookie.toString())
            .location(URI.create(handoff.redirectUrl))
            .build()
    }
}
