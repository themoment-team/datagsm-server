package team.themoment.datagsm.oauth.authorization.domain.oauth.service.impl

import jakarta.servlet.http.HttpSession
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import team.themoment.datagsm.common.domain.account.repository.AccountJpaRepository
import team.themoment.datagsm.common.domain.oauth.dto.request.OauthAuthorizeSubmitReqDto
import team.themoment.datagsm.common.domain.oauth.entity.OauthCodeRedisEntity
import team.themoment.datagsm.common.domain.oauth.exception.OAuthException
import team.themoment.datagsm.common.domain.oauth.repository.OauthCodeRedisRepository
import team.themoment.datagsm.common.global.data.OauthEnvironment
import team.themoment.datagsm.oauth.authorization.domain.oauth.service.CompleteOauthAuthorizeFlowService
import team.themoment.sdk.exception.ExpectedException
import team.themoment.sdk.logging.logger.logger
import java.net.URI
import java.security.SecureRandom
import java.util.Base64

@Service
class CompleteOauthAuthorizeFlowServiceImpl(
    private val accountJpaRepository: AccountJpaRepository,
    private val oauthCodeRedisRepository: OauthCodeRedisRepository,
    private val passwordEncoder: PasswordEncoder,
    private val oauthEnvironment: OauthEnvironment,
) : CompleteOauthAuthorizeFlowService {
    companion object {
        private val secureRandom = SecureRandom()
    }

    override fun execute(
        reqDto: OauthAuthorizeSubmitReqDto,
        session: HttpSession,
    ): ResponseEntity<Void> {
        logger()
            .info(
                "🟢 [COMPLETE] Session received - ID: ${session.id}, " +
                    "IsNew: ${session.isNew}, MaxInactiveInterval: ${session.maxInactiveInterval}s",
            )

        val clientId = session.getAttribute("oauth_client_id") as? String
        val redirectUri = session.getAttribute("oauth_redirect_uri") as? String

        logger()
            .info(
                "🟢 [COMPLETE] Session attributes retrieved: " +
                    "oauth_client_id=$clientId, oauth_redirect_uri=$redirectUri",
            )

        if (clientId == null) {
            logger()
                .error(
                    "🔴 [COMPLETE] Session missing oauth_client_id - " +
                        "Session may have expired or was not created",
                )
            throw OAuthException.InvalidRequest("세션이 만료되었습니다. 다시 시도해주세요.")
        }

        if (redirectUri == null) {
            logger()
                .error(
                    "🔴 [COMPLETE] Session missing oauth_redirect_uri - " +
                        "Session may have expired or was not created",
                )
            throw OAuthException.InvalidRequest("세션이 만료되었습니다. 다시 시도해주세요.")
        }

        val state = session.getAttribute("oauth_state") as? String
        val codeChallenge = session.getAttribute("oauth_code_challenge") as? String
        val codeChallengeMethod = session.getAttribute("oauth_code_challenge_method") as? String

        val account =
            accountJpaRepository
                .findByEmail(reqDto.email)
                .orElseThrow { ExpectedException("존재하지 않는 이메일입니다.", HttpStatus.UNAUTHORIZED) }

        if (!passwordEncoder.matches(reqDto.password, account.password)) {
            throw ExpectedException("비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED)
        }

        val code = generateAuthorizationCode()

        val oauthCodeEntity =
            OauthCodeRedisEntity(
                email = account.email,
                clientId = clientId,
                redirectUri = redirectUri,
                codeChallenge = codeChallenge,
                codeChallengeMethod = codeChallengeMethod,
                code = code,
                ttl = oauthEnvironment.codeExpirationSeconds,
            )
        oauthCodeRedisRepository.save(oauthCodeEntity)

        session.invalidate()

        val redirectUrl = buildRedirectUrl(redirectUri, code, state)

        return ResponseEntity
            .status(HttpStatus.FOUND)
            .location(URI.create(redirectUrl))
            .build()
    }

    private fun generateAuthorizationCode(): String =
        Base64
            .getUrlEncoder()
            .withoutPadding()
            .encodeToString(ByteArray(22).also { secureRandom.nextBytes(it) })

    private fun buildRedirectUrl(
        redirectUri: String,
        code: String,
        state: String?,
    ): String =
        buildString {
            append(redirectUri)
            append(if (redirectUri.contains('?')) '&' else '?')
            append("code=").append(code)
            state?.let { append("&state=").append(it) }
        }
}
