package team.themoment.datagsm.oauth.authorization.domain.oauth.service.impl

import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import team.themoment.datagsm.common.domain.client.repository.ClientJpaRepository
import team.themoment.datagsm.common.domain.oauth.entity.IdpSessionHandoffRedisEntity
import team.themoment.datagsm.common.domain.oauth.exception.OAuthException
import team.themoment.datagsm.common.domain.oauth.repository.IdpSessionHandoffRedisRepository
import team.themoment.datagsm.common.global.data.OauthEnvironment
import team.themoment.datagsm.oauth.authorization.domain.oauth.service.CompleteIdpSessionHandoffService
import java.net.URI
import java.security.MessageDigest
import java.time.Duration

@Service
class CompleteIdpSessionHandoffServiceImpl(
    private val idpSessionHandoffRedisRepository: IdpSessionHandoffRedisRepository,
    private val clientJpaRepository: ClientJpaRepository,
    private val oauthEnvironment: OauthEnvironment,
) : CompleteIdpSessionHandoffService {
    override fun execute(
        ticket: String,
        verifier: String,
    ): ResponseEntity<Void> {
        val handoff =
            idpSessionHandoffRedisRepository.findByIdOrNull(ticket)
                ?: throw OAuthException.InvalidRequest("인증 티켓이 유효하지 않거나 만료되었습니다. 다시 시도해주세요.")

        // ticket은 URL에 노출되므로, 실제 브라우저만 가진 verifier까지 일치해야 세션을 발급한다.
        // 불일치 시에도 티켓을 즉시 폐기해 무차별 대입 시도를 차단한다.
        if (!matchesVerifier(verifier, handoff.verifierHash)) {
            idpSessionHandoffRedisRepository.deleteById(ticket)
            throw OAuthException.InvalidRequest("인증 티켓이 유효하지 않거나 만료되었습니다. 다시 시도해주세요.")
        }

        idpSessionHandoffRedisRepository.deleteById(ticket)

        verifyRedirectUrlStillAllowed(handoff)

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

    // 저장 시점에 화이트리스트를 통과했더라도, Location으로 내보내기 직전에 다시 확인해
    // 오픈 리다이렉트가 성립하지 않도록 한다.
    // redirectUrl에는 code/state 쿼리가 덧붙어 있으므로, 등록된 redirect_uri와 동일한 방식으로
    // 원본 URI만 잘라내 정확히 일치하는지 본다. 접두사 비교는 도메인 위조를 허용하므로 쓰지 않는다.
    private fun verifyRedirectUrlStillAllowed(handoff: IdpSessionHandoffRedisEntity) {
        val client =
            clientJpaRepository
                .findById(handoff.clientId)
                .orElseThrow { OAuthException.InvalidRequest("인증 티켓이 유효하지 않거나 만료되었습니다. 다시 시도해주세요.") }

        val isAllowed = client.redirectUrls.any { it == extractRegisteredRedirectUri(handoff.redirectUrl, it) }
        if (!isAllowed) {
            throw OAuthException.InvalidRequest("등록되지 않은 redirect_uri입니다.")
        }
    }

    // 발급 시 buildRedirectUrl이 등록 URI 뒤에 '?' 또는 '&'로 code/state를 이어 붙인다.
    // 후보 URI 길이만큼 잘라낸 값이 그 URI와 같고, 이어지는 문자가 구분자인 경우에만 일치로 본다.
    private fun extractRegisteredRedirectUri(
        redirectUrl: String,
        candidate: String,
    ): String? {
        if (!redirectUrl.startsWith(candidate)) return null
        val remainder = redirectUrl.substring(candidate.length)
        if (remainder.isNotEmpty() && remainder[0] != '?' && remainder[0] != '&') return null
        return candidate
    }

    private fun matchesVerifier(
        verifier: String,
        expectedHash: String,
    ): Boolean {
        val actualHash = sha256Hex(verifier)
        return MessageDigest.isEqual(
            actualHash.toByteArray(Charsets.UTF_8),
            expectedHash.toByteArray(Charsets.UTF_8),
        )
    }

    private fun sha256Hex(value: String): String =
        MessageDigest
            .getInstance("SHA-256")
            .digest(value.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
}
