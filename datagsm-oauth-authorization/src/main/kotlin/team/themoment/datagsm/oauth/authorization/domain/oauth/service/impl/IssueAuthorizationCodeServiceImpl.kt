package team.themoment.datagsm.oauth.authorization.domain.oauth.service.impl

import org.springframework.stereotype.Service
import team.themoment.datagsm.common.domain.oauth.entity.OauthCodeRedisEntity
import team.themoment.datagsm.common.domain.oauth.repository.OauthCodeRedisRepository
import team.themoment.datagsm.common.global.data.OauthEnvironment
import team.themoment.datagsm.oauth.authorization.domain.oauth.service.IssueAuthorizationCodeService
import java.security.SecureRandom
import java.util.Base64

@Service
class IssueAuthorizationCodeServiceImpl(
    private val oauthCodeRedisRepository: OauthCodeRedisRepository,
    private val oauthEnvironment: OauthEnvironment,
) : IssueAuthorizationCodeService {
    companion object {
        private val secureRandom = SecureRandom()
    }

    override fun execute(
        email: String,
        clientId: String,
        redirectUri: String,
        state: String?,
        codeChallenge: String?,
        codeChallengeMethod: String?,
        scopes: Set<String>,
    ): String {
        val code = generateAuthorizationCode()

        oauthCodeRedisRepository.save(
            OauthCodeRedisEntity(
                email = email,
                clientId = clientId,
                redirectUri = redirectUri,
                codeChallenge = codeChallenge,
                codeChallengeMethod = codeChallengeMethod,
                scopes = scopes,
                code = code,
                ttl = oauthEnvironment.codeExpirationSeconds,
            ),
        )

        return buildRedirectUrl(redirectUri, code, state)
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
