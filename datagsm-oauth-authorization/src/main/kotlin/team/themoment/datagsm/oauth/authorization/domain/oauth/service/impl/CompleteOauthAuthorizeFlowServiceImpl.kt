package team.themoment.datagsm.oauth.authorization.domain.oauth.service.impl

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import team.themoment.datagsm.common.domain.account.repository.AccountJpaRepository
import team.themoment.datagsm.common.domain.oauth.dto.request.OauthAuthorizeSubmitReqDto
import team.themoment.datagsm.common.domain.oauth.entity.OauthCodeRedisEntity
import team.themoment.datagsm.common.domain.oauth.exception.OAuthException
import team.themoment.datagsm.common.domain.oauth.repository.OauthAuthorizeStateRedisRepository
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
    private val oauthAuthorizeStateRedisRepository: OauthAuthorizeStateRedisRepository,
    private val passwordEncoder: PasswordEncoder,
    private val oauthEnvironment: OauthEnvironment,
) : CompleteOauthAuthorizeFlowService {
    companion object {
        private val secureRandom = SecureRandom()
    }

    override fun execute(reqDto: OauthAuthorizeSubmitReqDto): ResponseEntity<Void> {
        logger()
            .info(
                "🟢 [COMPLETE] OAuth authorize request - Token: ${reqDto.token}",
            )

        val stateEntity =
            oauthAuthorizeStateRedisRepository
                .findById(reqDto.token)
                .orElseThrow {
                    logger()
                        .error(
                            "🔴 [COMPLETE] Invalid or expired token - Token: ${reqDto.token}",
                        )
                    OAuthException.InvalidRequest("인증 토큰이 유효하지 않거나 만료되었습니다. 다시 시도해주세요.")
                }

        logger()
            .info(
                "🟢 [COMPLETE] OAuth state retrieved: " +
                    "ClientID: ${stateEntity.clientId}, RedirectURI: ${stateEntity.redirectUri}",
            )

        val clientId = stateEntity.clientId
        val redirectUri = stateEntity.redirectUri
        val state = stateEntity.state
        val codeChallenge = stateEntity.codeChallenge
        val codeChallengeMethod = stateEntity.codeChallengeMethod

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

        oauthAuthorizeStateRedisRepository.deleteById(reqDto.token)

        logger()
            .info(
                "🟢 [COMPLETE] Authorization code issued - Code: ${code.take(10)}..., " +
                    "Token deleted: ${reqDto.token}",
            )

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
