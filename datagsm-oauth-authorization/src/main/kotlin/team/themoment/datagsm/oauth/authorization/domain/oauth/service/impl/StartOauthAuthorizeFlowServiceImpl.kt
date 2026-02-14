package team.themoment.datagsm.oauth.authorization.domain.oauth.service.impl

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import team.themoment.datagsm.common.domain.client.repository.ClientJpaRepository
import team.themoment.datagsm.common.domain.oauth.entity.OauthAuthorizeStateRedisEntity
import team.themoment.datagsm.common.domain.oauth.entity.constant.PkceChallengeMethod
import team.themoment.datagsm.common.domain.oauth.exception.OAuthException
import team.themoment.datagsm.common.domain.oauth.repository.OauthAuthorizeStateRedisRepository
import team.themoment.datagsm.common.global.data.OauthEnvironment
import team.themoment.datagsm.oauth.authorization.domain.oauth.service.StartOauthAuthorizeFlowService
import team.themoment.sdk.logging.logger.logger
import java.net.URI
import java.util.UUID

@Service
class StartOauthAuthorizeFlowServiceImpl(
    private val clientJpaRepository: ClientJpaRepository,
    private val oauthEnvironment: OauthEnvironment,
    private val oauthAuthorizeStateRedisRepository: OauthAuthorizeStateRedisRepository,
) : StartOauthAuthorizeFlowService {
    override fun execute(
        clientId: String?,
        redirectUri: String?,
        responseType: String?,
        state: String?,
        codeChallenge: String?,
        codeChallengeMethod: String?,
    ): ResponseEntity<Void> {
        if (clientId.isNullOrBlank()) {
            throw OAuthException.InvalidRequest("client_id 파라미터가 필요합니다.")
        }

        if (redirectUri.isNullOrBlank()) {
            throw OAuthException.InvalidRequest("redirect_uri 파라미터가 필요합니다.")
        }

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

        val token = UUID.randomUUID().toString()

        val stateEntity =
            OauthAuthorizeStateRedisEntity(
                token = token,
                clientId = clientId,
                redirectUri = redirectUri,
                state = state,
                codeChallenge = codeChallenge,
                codeChallengeMethod = codeChallengeMethod,
                ttl = 600,
            )

        oauthAuthorizeStateRedisRepository.save(stateEntity)

        logger()
            .info(
                "🔵 [START] OAuth state saved - Token: $token, " +
                    "ClientID: $clientId, TTL: 600s",
            )

        return ResponseEntity
            .status(HttpStatus.FOUND)
            .location(URI.create("${oauthEnvironment.frontendUrl}/oauth/authorize?token=$token"))
            .build()
    }
}
