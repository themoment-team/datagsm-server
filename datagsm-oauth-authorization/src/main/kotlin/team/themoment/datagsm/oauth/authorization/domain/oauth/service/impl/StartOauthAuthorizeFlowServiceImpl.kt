package team.themoment.datagsm.oauth.authorization.domain.oauth.service.impl

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.util.UriComponentsBuilder
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.client.entity.constant.OAuthScope
import team.themoment.datagsm.common.domain.client.repository.ClientJpaRepository
import team.themoment.datagsm.common.domain.oauth.dto.request.OauthAuthorizeReqDto
import team.themoment.datagsm.common.domain.oauth.entity.OauthAuthorizeStateRedisEntity
import team.themoment.datagsm.common.domain.oauth.entity.constant.PkceChallengeMethod
import team.themoment.datagsm.common.domain.oauth.exception.OAuthException
import team.themoment.datagsm.common.domain.oauth.repository.OauthAuthorizeStateRedisRepository
import team.themoment.datagsm.common.domain.oauth.repository.OauthConsentJpaRepository
import team.themoment.datagsm.common.global.data.OauthEnvironment
import team.themoment.datagsm.oauth.authorization.domain.oauth.component.IdpSessionResolver
import team.themoment.datagsm.oauth.authorization.domain.oauth.service.IssueAuthorizationCodeService
import team.themoment.datagsm.oauth.authorization.domain.oauth.service.StartOauthAuthorizeFlowService
import team.themoment.datagsm.oauth.authorization.global.data.OauthJwtProvisionEnvironment
import java.net.URI
import java.util.UUID

@Service
class StartOauthAuthorizeFlowServiceImpl(
    private val clientJpaRepository: ClientJpaRepository,
    private val oauthEnvironment: OauthEnvironment,
    private val oauthAuthorizeStateRedisRepository: OauthAuthorizeStateRedisRepository,
    private val jwtEnvironment: OauthJwtProvisionEnvironment,
    private val idpSessionResolver: IdpSessionResolver,
    private val oauthConsentJpaRepository: OauthConsentJpaRepository,
    private val issueAuthorizationCodeService: IssueAuthorizationCodeService,
) : StartOauthAuthorizeFlowService {
    companion object {
        private const val LOGIN_PATH = "/oauth/authorize"
        private const val CONSENT_PATH = "/oauth/consent"
    }

    @Transactional(readOnly = true)
    override fun execute(
        reqDto: OauthAuthorizeReqDto,
        sessionId: String?,
    ): ResponseEntity<Void> {
        val clientId = reqDto.client_id ?: throw OAuthException.InvalidRequest("client_id는 필수입니다.")
        val redirectUri = reqDto.redirect_uri ?: throw OAuthException.InvalidRequest("redirect_uri는 필수입니다.")
        val responseType = reqDto.response_type
        val state = reqDto.state
        val codeChallenge = reqDto.code_challenge
        val codeChallengeMethod = reqDto.code_challenge_method

        if (responseType != "code") {
            throw OAuthException.InvalidRequest("response_type은 'code'여야 합니다.")
        }

        val client =
            clientJpaRepository
                .findById(clientId)
                .orElseThrow { OAuthException.InvalidClient("존재하지 않는 클라이언트입니다.") }

        if (!client.redirectUrls.contains(redirectUri)) {
            throw OAuthException.InvalidRequest("등록되지 않은 redirect_uri입니다.")
        }

        if (codeChallenge != null) {
            PkceChallengeMethod.fromOrNull(codeChallengeMethod)
                ?: throw OAuthException.InvalidRequest("지원하지 않는 code_challenge_method입니다.")
        }

        val requestedScopes =
            reqDto.scope
                ?.split(" ")
                ?.filter { it.isNotBlank() }
                ?.toSet()
        val resolvedScopes = resolveScopes(requestedScopes, client.scopes)

        val ssoAccount = idpSessionResolver.resolveEligibleAccount(sessionId)
        if (ssoAccount != null && hasConsentFor(ssoAccount, clientId, resolvedScopes)) {
            val redirectUrl =
                issueAuthorizationCodeService.execute(
                    email = ssoAccount.email,
                    clientId = clientId,
                    redirectUri = redirectUri,
                    state = state,
                    codeChallenge = codeChallenge,
                    codeChallengeMethod = codeChallengeMethod,
                    scopes = resolvedScopes,
                )
            return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(redirectUrl))
                .build()
        }

        val token = UUID.randomUUID().toString()

        val stateEntity =
            OauthAuthorizeStateRedisEntity(
                token = token,
                clientId = clientId,
                redirectUri = redirectUri,
                state = state,
                codeChallenge = codeChallenge,
                codeChallengeMethod = codeChallengeMethod,
                scopes = resolvedScopes,
                ttl = oauthEnvironment.authorizeStateExpirationMs / 1000,
            )

        oauthAuthorizeStateRedisRepository.save(stateEntity)

        // 세션은 유효한데 동의만 없는 경우, 비밀번호를 다시 받을 이유가 없으므로 동의 화면으로 보낸다.
        // 세션 자체가 없거나 자격을 잃은 경우에만 로그인 폼으로 되돌린다.
        val frontendPath = if (ssoAccount != null) CONSENT_PATH else LOGIN_PATH

        val location =
            UriComponentsBuilder
                .fromUriString(oauthEnvironment.frontendUrl)
                .path(frontendPath)
                .queryParam("token", token)
                .build()
                .toUri()

        return ResponseEntity
            .status(HttpStatus.FOUND)
            .location(location)
            .build()
    }

    // 요청된 scope가 이미 동의 기록에 전부 포함되어 있는지 확인한다.
    // 하나라도 새 scope가 섞여 있으면 그 scope에 대한 동의를 다시 받아야 한다.
    private fun hasConsentFor(
        account: AccountJpaEntity,
        clientId: String,
        resolvedScopes: Set<String>,
    ): Boolean {
        val accountId = account.id ?: return false
        val consent =
            oauthConsentJpaRepository
                .findByAccountIdAndClientId(accountId, clientId)
                .orElse(null) ?: return false

        return consent.grantedScopes.containsAll(resolvedScopes)
    }

    private fun resolveScopes(
        requestedScopes: Set<String>?,
        clientScopes: Set<String>,
    ): Set<String> {
        if (requestedScopes == null) return defaultScopes(clientScopes)
        val invalid = requestedScopes - clientScopes
        if (invalid.isNotEmpty()) {
            throw OAuthException.InvalidScope("클라이언트에 허용되지 않은 권한 범위가 포함되어 있습니다.")
        }
        return requestedScopes
    }

    // scope 파라미터 미입력 시 client의 전체 허용 scope가 아닌 기본 UserInfo scope만 요청된 것으로 처리한다.
    // account_read/student_read를 아직 부여받지 못한 레거시 client는 deprecated self_read로 대체한다.
    // 둘 다 없는 client는 스코프 없는 토큰이 조용히 발급되는 것을 막기 위해 InvalidScope로 실패시킨다.
    private fun defaultScopes(clientScopes: Set<String>): Set<String> {
        val applicationId = jwtEnvironment.datagsmApplicationId
        val defaults =
            setOf("$applicationId:${OAuthScope.ACCOUNT_READ}", "$applicationId:${OAuthScope.STUDENT_READ}")
                .intersect(clientScopes)
        if (defaults.isNotEmpty()) return defaults

        val legacySelfRead = "$applicationId:${OAuthScope.SELF_READ}"
        if (legacySelfRead in clientScopes) return setOf(legacySelfRead)

        throw OAuthException.InvalidScope("클라이언트에 기본으로 부여할 수 있는 권한 범위가 없습니다.")
    }
}
