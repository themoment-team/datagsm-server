package team.themoment.datagsm.oauth.authorization.domain.oauth.service.impl

import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.oauth.dto.request.OauthConsentReqDto
import team.themoment.datagsm.common.domain.oauth.entity.OauthAuthorizeStateRedisEntity
import team.themoment.datagsm.common.domain.oauth.exception.OAuthException
import team.themoment.datagsm.common.domain.oauth.repository.OauthAuthorizeStateRedisRepository
import team.themoment.datagsm.common.domain.oauth.repository.OauthConsentJpaRepository
import team.themoment.datagsm.oauth.authorization.domain.oauth.component.IdpSessionResolver
import team.themoment.datagsm.oauth.authorization.domain.oauth.service.CompleteOauthConsentService
import team.themoment.datagsm.oauth.authorization.domain.oauth.service.IssueAuthorizationCodeService
import team.themoment.sdk.exception.ExpectedException
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Service
class CompleteOauthConsentServiceImpl(
    private val oauthAuthorizeStateRedisRepository: OauthAuthorizeStateRedisRepository,
    private val oauthConsentJpaRepository: OauthConsentJpaRepository,
    private val issueAuthorizationCodeService: IssueAuthorizationCodeService,
    private val idpSessionResolver: IdpSessionResolver,
) : CompleteOauthConsentService {
    companion object {
        private const val ACCESS_DENIED_ERROR = "access_denied"
        private const val ACCESS_DENIED_DESCRIPTION = "사용자가 접근 권한 요청을 거부했습니다."
    }

    @Transactional
    override fun execute(
        reqDto: OauthConsentReqDto,
        sessionId: String?,
    ): ResponseEntity<Void> {
        val stateEntity =
            oauthAuthorizeStateRedisRepository
                .findByIdOrNull(reqDto.token)
                ?: throw OAuthException.InvalidRequest("인증 토큰이 유효하지 않거나 만료되었습니다. 다시 시도해주세요.")

        // 이 엔드포인트는 비밀번호 없이 세션 쿠키만으로 코드를 발급한다.
        // 따라서 SSO 인가와 완전히 같은 자격 조건을 여기서 다시 확인해야 한다.
        val account =
            idpSessionResolver.resolveEligibleAccount(sessionId)
                ?: throw ExpectedException("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED)

        if (!reqDto.approved) {
            return denyConsent(stateEntity)
        }

        val redirectUrl =
            issueAuthorizationCodeService.execute(
                email = account.email,
                clientId = stateEntity.clientId,
                redirectUri = stateEntity.redirectUri,
                state = stateEntity.state,
                codeChallenge = stateEntity.codeChallenge,
                codeChallengeMethod = stateEntity.codeChallengeMethod,
                scopes = stateEntity.scopes,
                nonce = stateEntity.nonce,
            )

        oauthAuthorizeStateRedisRepository.deleteById(reqDto.token)

        val accountId = requireNotNull(account.id) { "Persisted account must have an id" }
        recordConsent(accountId, stateEntity.clientId, stateEntity.scopes)

        return ResponseEntity
            .status(HttpStatus.FOUND)
            .location(URI.create(redirectUrl))
            .build()
    }

    // 거부는 OAuth 2.0 표준(RFC 6749 4.1.2.1)대로 클라이언트에 error=access_denied로 돌려준다.
    // 토큰을 소비해 같은 화면을 다시 승인으로 뒤집지 못하게 한다.
    private fun denyConsent(stateEntity: OauthAuthorizeStateRedisEntity): ResponseEntity<Void> {
        oauthAuthorizeStateRedisRepository.deleteById(stateEntity.token)

        val redirectUrl =
            buildString {
                append(stateEntity.redirectUri)
                append(if (stateEntity.redirectUri.contains('?')) '&' else '?')
                append("error=").append(ACCESS_DENIED_ERROR)
                append("&error_description=").append(encodeQueryValue(ACCESS_DENIED_DESCRIPTION))
                stateEntity.state?.let { append("&state=").append(encodeQueryValue(it)) }
            }

        return ResponseEntity
            .status(HttpStatus.FOUND)
            .location(URI.create(redirectUrl))
            .build()
    }

    // 동의 기록은 DB의 원자적 upsert에 맡긴다.
    //
    // 조회 후 없으면 insert하는 방식은 같은 (account, client)로 첫 로그인이 동시에
    // 들어올 때 둘 다 "없음"을 보고 insert해 uk_oauth_consent_account_client 위반이 난다.
    // 제약 위반을 잡아 같은 트랜잭션에서 재시도하는 것도 답이 아니다. flush 중 제약 위반이
    // 나면 영속성 컨텍스트를 더 이상 신뢰할 수 없고, REPEATABLE READ에서는 재조회가 다른
    // 트랜잭션이 방금 커밋한 행을 보지 못해 같은 예외를 다시 던질 수 있다.
    //
    // 이 자리에서 실패하면 특히 곤란하다. 인가 코드는 이미 Redis에 저장돼 롤백되지 않으므로,
    // 사용자는 500을 받는데 코드만 남는 상태가 된다.
    private fun recordConsent(
        accountId: Long,
        clientId: String,
        scopes: Set<String>,
    ) {
        oauthConsentJpaRepository.upsertConsent(accountId, clientId)

        val consentId =
            oauthConsentJpaRepository.findIdByAccountIdAndClientId(accountId, clientId)
                ?: throw ExpectedException("동의 정보를 저장하지 못했습니다.", HttpStatus.INTERNAL_SERVER_ERROR)

        scopes.forEach { scope ->
            oauthConsentJpaRepository.addScopeIfAbsent(consentId, scope)
        }
    }

    private fun encodeQueryValue(value: String): String = URLEncoder.encode(value, StandardCharsets.UTF_8)
}
