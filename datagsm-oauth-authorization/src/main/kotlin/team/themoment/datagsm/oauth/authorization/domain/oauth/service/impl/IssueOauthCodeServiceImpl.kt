package team.themoment.datagsm.oauth.authorization.domain.oauth.service.impl

import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.account.repository.AccountJpaRepository
import team.themoment.datagsm.common.domain.client.entity.ClientJpaEntity
import team.themoment.datagsm.common.domain.client.repository.ClientJpaRepository
import team.themoment.datagsm.common.domain.oauth.dto.request.OauthCodeReqDto
import team.themoment.datagsm.common.domain.oauth.dto.response.OauthCodeResDto
import team.themoment.datagsm.common.domain.oauth.entity.OauthCodeRedisEntity
import team.themoment.datagsm.common.domain.oauth.repository.OauthCodeRedisRepository
import team.themoment.datagsm.common.global.data.OauthEnvironment
import team.themoment.datagsm.oauth.authorization.domain.oauth.service.IssueOauthCodeService
import team.themoment.sdk.exception.ExpectedException
import java.security.SecureRandom
import java.util.Base64

@Service
class IssueOauthCodeServiceImpl(
    private val accountJpaRepository: AccountJpaRepository,
    private val clientJpaRepository: ClientJpaRepository,
    private val passwordEncoder: PasswordEncoder,
    private val oauthCodeRedisRepository: OauthCodeRedisRepository,
    private val oauthEnvironment: OauthEnvironment,
) : IssueOauthCodeService {
    companion object {
        private val secureRandom = SecureRandom()
    }

    @Transactional(readOnly = true)
    override fun execute(reqDto: OauthCodeReqDto): OauthCodeResDto {
        val client =
            clientJpaRepository
                .findByIdOrNull(reqDto.clientId)
                ?: throw ExpectedException("존재하지 않는 Client Id 입니다.", HttpStatus.NOT_FOUND)
        validateRedirectUrl(reqDto.redirectUrl, client)

        if (reqDto.codeChallenge != null) {
            val method = reqDto.codeChallengeMethod ?: "plain"
            if (method !in setOf("S256", "plain")) {
                throw ExpectedException("지원하지 않는 code_challenge_method입니다.", HttpStatus.BAD_REQUEST)
            }
        }

        val account =
            accountJpaRepository
                .findByEmail(reqDto.email)
                .orElseThrow { ExpectedException("존재하지 않는 회원의 이메일입니다.", HttpStatus.NOT_FOUND) }
        validatePassword(reqDto.password, account.password)
        val oauthCode =
            generateOauthCodeForAccount(account, reqDto.clientId, reqDto.redirectUrl, reqDto.codeChallenge, reqDto.codeChallengeMethod)
        return OauthCodeResDto(oauthCode)
    }

    private fun validateRedirectUrl(
        redirectUrl: String,
        client: ClientJpaEntity,
    ) {
        if (!client.redirectUrls.contains(redirectUrl)) {
            throw ExpectedException("등록되지 않은 Redirect URL 입니다.", HttpStatus.BAD_REQUEST)
        }
    }

    private fun generateOauthCodeForAccount(
        account: AccountJpaEntity,
        clientId: String,
        redirectUri: String,
        codeChallenge: String?,
        codeChallengeMethod: String?,
    ): String {
        val code =
            Base64
                .getUrlEncoder()
                .withoutPadding()
                .encodeToString(ByteArray(22).also { secureRandom.nextBytes(it) })
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
        return code
    }

    private fun validatePassword(
        password: String,
        encodedPassword: String,
    ) {
        if (!passwordEncoder.matches(password, encodedPassword)) {
            throw ExpectedException("비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED)
        }
    }
}
