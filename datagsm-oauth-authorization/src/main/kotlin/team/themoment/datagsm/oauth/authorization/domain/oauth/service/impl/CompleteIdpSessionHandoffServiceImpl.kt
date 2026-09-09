package team.themoment.datagsm.oauth.authorization.domain.oauth.service.impl

import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
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
    companion object {
        private const val SEC_FETCH_MODE_NAVIGATE = "navigate"
        private val ALLOWED_SEC_FETCH_SITES = setOf("same-origin", "same-site", "none")
    }

    @Transactional(readOnly = true)
    override fun execute(
        ticket: String,
        verifier: String,
        secFetchSite: String?,
        secFetchMode: String?,
    ): ResponseEntity<Void> {
        verifyTopLevelNavigation(secFetchSite, secFetchMode)

        val handoff =
            idpSessionHandoffRedisRepository.findByIdOrNull(ticket)
                ?: throw OAuthException.InvalidRequest("인증 티켓이 유효하지 않거나 만료되었습니다. 다시 시도해주세요.")

        // ticket과 verifier는 같은 URL로 전달되므로 verifier만으로 유출을 막지는 못한다.
        // 로그 등에서 티켓 일부만 새어나간 경우를 위한 추가 방어이며,
        // 실질적인 재사용 차단은 위의 Sec-Fetch 검사와 일회용 소비가 담당한다.
        // 불일치해도 티켓을 삭제하지 않는다. 삭제하면 ticket만 아는 공격자가
        // 요청 한 번으로 정상 사용자의 로그인을 무효화할 수 있다.
        if (!matchesVerifier(verifier, handoff.verifierHash)) {
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

    // 핸드오프 URL은 로그·Referer·브라우저 히스토리에 남기 때문에, 나중에 그 URL을 입수한
    // 공격자가 다시 열어보는 것을 막아야 한다. 정상 흐름은 프론트에서 백엔드로 넘어오는
    // 최상위 내비게이션이므로, 브라우저가 붙여주는 Sec-Fetch 힌트로 그 형태만 통과시킨다.
    // 헤더를 보내지 않는 구형 브라우저는 정상 로그인을 막지 않도록 설정으로 통과시킬 수 있다.
    private fun verifyTopLevelNavigation(
        secFetchSite: String?,
        secFetchMode: String?,
    ) {
        if (secFetchSite == null && secFetchMode == null) {
            if (oauthEnvironment.idpSessionHandoffRequireFetchMetadata) {
                throw OAuthException.InvalidRequest("인증 티켓이 유효하지 않거나 만료되었습니다. 다시 시도해주세요.")
            }
            return
        }

        if (secFetchMode != null && secFetchMode != SEC_FETCH_MODE_NAVIGATE) {
            throw OAuthException.InvalidRequest("인증 티켓이 유효하지 않거나 만료되었습니다. 다시 시도해주세요.")
        }

        if (secFetchSite != null && secFetchSite !in ALLOWED_SEC_FETCH_SITES) {
            throw OAuthException.InvalidRequest("인증 티켓이 유효하지 않거나 만료되었습니다. 다시 시도해주세요.")
        }
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
