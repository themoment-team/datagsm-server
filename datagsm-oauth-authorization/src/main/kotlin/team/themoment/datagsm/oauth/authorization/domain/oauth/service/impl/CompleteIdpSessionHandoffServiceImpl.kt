package team.themoment.datagsm.oauth.authorization.domain.oauth.service.impl

import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.client.repository.ClientJpaRepository
import team.themoment.datagsm.common.domain.oauth.entity.IdpSessionHandoffRedisEntity
import team.themoment.datagsm.common.domain.oauth.exception.OAuthException
import team.themoment.datagsm.common.domain.oauth.repository.IdpSessionHandoffRedisRepository
import team.themoment.datagsm.common.domain.oauth.repository.IdpSessionRedisRepository
import team.themoment.datagsm.common.global.data.OauthEnvironment
import team.themoment.datagsm.common.global.security.util.IdpSessionCookieFactory
import team.themoment.datagsm.common.global.security.util.OpaqueTokenHashUtil
import team.themoment.datagsm.oauth.authorization.domain.oauth.service.CompleteIdpSessionHandoffService
import java.net.URI

@Service
class CompleteIdpSessionHandoffServiceImpl(
    private val idpSessionHandoffRedisRepository: IdpSessionHandoffRedisRepository,
    private val idpSessionRedisRepository: IdpSessionRedisRepository,
    private val clientJpaRepository: ClientJpaRepository,
    private val oauthEnvironment: OauthEnvironment,
) : CompleteIdpSessionHandoffService {
    companion object {
        private const val SEC_FETCH_MODE_NAVIGATE = "navigate"
        private val ALLOWED_SEC_FETCH_SITES = setOf("same-origin", "same-site", "none")
        private const val INVALID_TICKET_MESSAGE = "인증 티켓이 유효하지 않거나 만료되었습니다. 다시 시도해주세요."
    }

    @Transactional(readOnly = true)
    override fun execute(
        ticket: String,
        verifier: String?,
        secFetchSite: String?,
        secFetchMode: String?,
    ): ResponseEntity<Void> {
        verifyTopLevelNavigation(secFetchSite, secFetchMode)

        val handoff =
            idpSessionHandoffRedisRepository.findByIdOrNull(ticket)
                ?: throw OAuthException.InvalidRequest(INVALID_TICKET_MESSAGE)

        // ticket과 verifier는 같은 URL로 전달되므로 verifier만으로 유출을 막지는 못한다.
        // 로그 등에서 티켓 일부만 새어나간 경우를 위한 보조 방어이며,
        // 실질적인 재사용 차단은 위의 Sec-Fetch 검사와 일회용 소비가 담당한다.
        // 불일치해도 티켓을 삭제하지 않는다. 삭제하면 ticket만 아는 공격자가
        // 요청 한 번으로 정상 사용자의 로그인을 무효화할 수 있다.
        if (verifier == null || !OpaqueTokenHashUtil.matches(verifier, handoff.verifierHash)) {
            throw OAuthException.InvalidRequest(INVALID_TICKET_MESSAGE)
        }

        // 티켓을 소비하기 전에 목적지를 먼저 검증한다.
        // 순서가 반대면 클라이언트의 redirect_uri가 바뀐 순간 티켓만 날아가고,
        // 이미 만들어둔 IdP 세션은 브라우저에 전달되지 못한 채 Redis에 남는다.
        verifyRedirectUrlStillAllowed(handoff)

        idpSessionHandoffRedisRepository.deleteById(ticket)

        return ResponseEntity
            .status(HttpStatus.FOUND)
            .header(
                HttpHeaders.SET_COOKIE,
                IdpSessionCookieFactory.issued(oauthEnvironment, handoff.sessionId).toString(),
            ).location(URI.create(handoff.redirectUrl))
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
                throw OAuthException.InvalidRequest(INVALID_TICKET_MESSAGE)
            }
            return
        }

        if (secFetchMode != null && secFetchMode != SEC_FETCH_MODE_NAVIGATE) {
            throw OAuthException.InvalidRequest(INVALID_TICKET_MESSAGE)
        }

        if (secFetchSite != null && secFetchSite !in ALLOWED_SEC_FETCH_SITES) {
            throw OAuthException.InvalidRequest(INVALID_TICKET_MESSAGE)
        }
    }

    // 저장 시점에 화이트리스트를 통과했더라도, Location으로 내보내기 직전에 다시 확인해
    // 오픈 리다이렉트가 성립하지 않도록 한다.
    // 검증에 실패하면 쓰이지 못할 세션이 남지 않도록 티켓과 세션을 함께 정리한다.
    private fun verifyRedirectUrlStillAllowed(handoff: IdpSessionHandoffRedisEntity) {
        val client =
            clientJpaRepository
                .findById(handoff.clientId)
                .orElseGet { null }
                ?: throw discardHandoff(handoff, INVALID_TICKET_MESSAGE)

        if (client.redirectUrls.none { matchesRegisteredRedirectUri(handoff.redirectUrl, it) }) {
            throw discardHandoff(handoff, "등록되지 않은 redirect_uri입니다.")
        }
    }

    private fun discardHandoff(
        handoff: IdpSessionHandoffRedisEntity,
        message: String,
    ): OAuthException.InvalidRequest {
        idpSessionHandoffRedisRepository.deleteById(handoff.ticket)
        idpSessionRedisRepository.deleteById(handoff.sessionId)
        return OAuthException.InvalidRequest(message)
    }

    // 발급 시 buildRedirectUrl이 등록 URI 뒤에 '?' 또는 '&'로 code/state를 이어 붙인다.
    // 등록 URI로 시작하고, 이어지는 문자가 그 구분자인 경우에만 일치로 본다.
    // 접두사만 비교하면 https://example.com.attacker.io 같은 도메인 위조가 통과한다.
    private fun matchesRegisteredRedirectUri(
        redirectUrl: String,
        candidate: String,
    ): Boolean {
        if (!redirectUrl.startsWith(candidate)) return false
        val remainder = redirectUrl.substring(candidate.length)
        return remainder.isEmpty() || remainder[0] == '?' || remainder[0] == '&'
    }
}
