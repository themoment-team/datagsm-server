package team.themoment.datagsm.domain.auth.service.impl

import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.domain.account.repository.AccountJpaRepository
import team.themoment.datagsm.domain.auth.dto.TokenResDto
import team.themoment.datagsm.domain.auth.entity.constant.Role
import team.themoment.datagsm.domain.auth.service.AuthenticateGoogleOAuthService
import team.themoment.datagsm.global.security.jwt.JwtProvider
import team.themoment.datagsm.global.thirdparty.feign.oauth.GoogleOAuth2Client
import team.themoment.datagsm.global.thirdparty.feign.oauth.GoogleUserInfoClient
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Service
class AuthenticateGoogleOAuthServiceImpl(
    private val clientRegistrationRepository: ClientRegistrationRepository,
    private val googleOAuth2Client: GoogleOAuth2Client,
    private val googleUserInfoClient: GoogleUserInfoClient,
    private val accountJpaRepository: AccountJpaRepository,
    private val jwtProvider: JwtProvider,
) : AuthenticateGoogleOAuthService {
    @Transactional
    override fun execute(authorizationCode: String): TokenResDto {
        val decodedCode = URLDecoder.decode(authorizationCode, StandardCharsets.UTF_8)

        val clientRegistration =
            clientRegistrationRepository.findByRegistrationId("google")
                ?: throw IllegalArgumentException("Google OAuth 설정을 찾을 수 없습니다.")

        val tokenResponse = exchangeCodeForToken(decodedCode, clientRegistration)

        val userInfo = googleUserInfoClient.getUserInfo("Bearer ${tokenResponse.accessToken}")

        val account =
            accountJpaRepository
                .findByAccountEmail(userInfo.email)
                .orElseGet {
                    accountJpaRepository.save(AccountJpaEntity.create(userInfo.email))
                }

        val role = account.accountStudent?.studentRole ?: Role.GENERAL_STUDENT

        val accessToken = jwtProvider.generateAccessToken(userInfo.email, role)
        val refreshToken = jwtProvider.generateRefreshToken(userInfo.email)

        return TokenResDto(
            accessToken = accessToken,
            refreshToken = refreshToken,
        )
    }

    private fun exchangeCodeForToken(
        code: String,
        clientRegistration: ClientRegistration,
    ) = googleOAuth2Client.exchangeCodeForToken(
        mapOf(
            "code" to code,
            "client_id" to clientRegistration.clientId,
            "client_secret" to clientRegistration.clientSecret,
            "redirect_uri" to clientRegistration.redirectUri,
            "grant_type" to "authorization_code",
        ),
    )
}
