package team.themoment.datagsm.oauth.authorization.domain.oauth.service.impl

import jakarta.servlet.http.HttpSession
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import team.themoment.datagsm.common.domain.client.repository.ClientJpaRepository
import team.themoment.datagsm.common.domain.oauth.entity.constant.PkceChallengeMethod
import team.themoment.datagsm.common.domain.oauth.exception.OAuthException
import team.themoment.datagsm.common.global.data.OauthEnvironment
import team.themoment.datagsm.oauth.authorization.domain.oauth.service.StartOauthAuthorizeFlowService
import java.net.URI

@Service
class StartOauthAuthorizeFlowServiceImpl(
    private val clientJpaRepository: ClientJpaRepository,
    private val oauthEnvironment: OauthEnvironment,
) : StartOauthAuthorizeFlowService {
    override fun execute(
        clientId: String?,
        redirectUri: String?,
        responseType: String?,
        state: String?,
        codeChallenge: String?,
        codeChallengeMethod: String?,
        session: HttpSession,
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

        session.setAttribute("oauth_client_id", clientId)
        session.setAttribute("oauth_redirect_uri", redirectUri)
        session.setAttribute("oauth_state", state)
        session.setAttribute("oauth_code_challenge", codeChallenge)
        session.setAttribute("oauth_code_challenge_method", codeChallengeMethod)

        return ResponseEntity
            .status(HttpStatus.FOUND)
            .location(URI.create("${oauthEnvironment.frontendUrl}/oauth/authorize"))
            .build()
    }
}
