package team.themoment.datagsm.oauth.authorization.domain.oauth.service.impl

import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.oauth.dto.request.OauthConsentReqDto
import team.themoment.datagsm.common.domain.oauth.entity.OauthAuthorizeStateRedisEntity
import team.themoment.datagsm.common.domain.oauth.entity.OauthConsentJpaEntity
import team.themoment.datagsm.common.domain.oauth.exception.OAuthException
import team.themoment.datagsm.common.domain.oauth.repository.OauthAuthorizeStateRedisRepository
import team.themoment.datagsm.common.domain.oauth.repository.OauthConsentJpaRepository
import team.themoment.datagsm.oauth.authorization.domain.oauth.component.IdpSessionResolver
import team.themoment.datagsm.oauth.authorization.domain.oauth.service.CompleteOauthConsentService
import team.themoment.datagsm.oauth.authorization.domain.oauth.service.IssueAuthorizationCodeService
import team.themoment.sdk.exception.ExpectedException
import team.themoment.sdk.logging.logger.logger
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

    // 동시 요청으로 같은 (account, client) 행을 함께 insert하면 제약 위반이 난다.
    // 이 시점에는 code가 이미 Redis에 저장돼 롤백되지 않으므로 재조회 후 병합으로 흡수한다.
    private fun recordConsent(
        accountId: Long,
        clientId: String,
        scopes: Set<String>,
    ) {
        try {
            saveConsent(accountId, clientId, scopes)
        } catch (e: DataIntegrityViolationException) {
            logger().warn("Retrying consent record after unique constraint violation for clientId {}", clientId, e)
            saveConsent(accountId, clientId, scopes)
        }
    }

    private fun saveConsent(
        accountId: Long,
        clientId: String,
        scopes: Set<String>,
    ) {
        val consent =
            oauthConsentJpaRepository
                .findByAccountIdAndClientId(accountId, clientId)
                .orElseGet { OauthConsentJpaEntity.create(accountId, clientId, emptySet()) }
        consent.grantedScopes.addAll(scopes)
        oauthConsentJpaRepository.saveAndFlush(consent)
    }

    private fun encodeQueryValue(value: String): String = URLEncoder.encode(value, StandardCharsets.UTF_8)
}
