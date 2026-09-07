package team.themoment.datagsm.oauth.authorization.domain.oauth.service.impl

import org.springframework.stereotype.Service
import team.themoment.datagsm.common.domain.oauth.entity.OauthCodeRedisEntity
import team.themoment.datagsm.common.domain.oauth.repository.OauthCodeRedisRepository
import team.themoment.datagsm.common.global.data.OauthEnvironment
import team.themoment.datagsm.oauth.authorization.domain.oauth.service.IssueAuthorizationCodeService
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
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

    // state는 클라이언트가 임의 값을 넣는 CSRF 방어 값이라, 인코딩하지 않으면
    // '&'나 '=' 삽입으로 리다이렉트 URL의 파라미터가 조작될 수 있다.
    private fun buildRedirectUrl(
        redirectUri: String,
        code: String,
        state: String?,
    ): String =
        buildString {
            append(redirectUri)
            append(if (redirectUri.contains('?')) '&' else '?')
            append("code=").append(encodeQueryValue(code))
            state?.let { append("&state=").append(encodeQueryValue(it)) }
        }

    private fun encodeQueryValue(value: String): String = URLEncoder.encode(value, StandardCharsets.UTF_8)
}
