package team.themoment.datagsm.oauth.authorization.domain.oauth.service.impl

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder
import team.themoment.datagsm.common.domain.client.repository.ClientJpaRepository
import team.themoment.datagsm.common.domain.oauth.dto.request.OauthAuthorizeReqDto
import team.themoment.datagsm.common.domain.oauth.entity.OauthAuthorizeStateRedisEntity
import team.themoment.datagsm.common.domain.oauth.entity.constant.PkceChallengeMethod
import team.themoment.datagsm.common.domain.oauth.exception.OAuthException
import team.themoment.datagsm.common.domain.oauth.repository.OauthAuthorizeStateRedisRepository
import team.themoment.datagsm.common.global.data.OauthEnvironment
import team.themoment.datagsm.oauth.authorization.domain.oauth.service.StartOauthAuthorizeFlowService
import team.themoment.datagsm.oauth.authorization.global.data.OauthJwtProvisionEnvironment
import java.util.UUID

@Service
class StartOauthAuthorizeFlowServiceImpl(
    private val clientJpaRepository: ClientJpaRepository,
    private val oauthEnvironment: OauthEnvironment,
    private val oauthAuthorizeStateRedisRepository: OauthAuthorizeStateRedisRepository,
    private val jwtEnvironment: OauthJwtProvisionEnvironment,
) : StartOauthAuthorizeFlowService {
    override fun execute(reqDto: OauthAuthorizeReqDto): ResponseEntity<Void> {
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

        val location =
            UriComponentsBuilder
                .fromUriString(oauthEnvironment.frontendUrl)
                .path("/oauth/authorize")
                .queryParam("token", token)
                .build()
                .toUri()

        return ResponseEntity
            .status(HttpStatus.FOUND)
            .location(location)
            .build()
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
    private fun defaultScopes(clientScopes: Set<String>): Set<String> {
        val applicationId = jwtEnvironment.datagsmApplicationId
        val defaults = setOf("$applicationId:account_read", "$applicationId:student_read").intersect(clientScopes)
        if (defaults.isNotEmpty()) return defaults

        val legacySelfRead = "$applicationId:self_read"
        return if (legacySelfRead in clientScopes) setOf(legacySelfRead) else emptySet()
    }
}
