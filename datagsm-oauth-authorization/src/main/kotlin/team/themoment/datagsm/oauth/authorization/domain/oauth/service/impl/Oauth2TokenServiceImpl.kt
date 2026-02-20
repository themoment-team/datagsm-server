package team.themoment.datagsm.oauth.authorization.domain.oauth.service.impl

import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.account.repository.AccountJpaRepository
import team.themoment.datagsm.common.domain.client.entity.ClientJpaEntity
import team.themoment.datagsm.common.domain.client.entity.constant.OAuthScope
import team.themoment.datagsm.common.domain.client.repository.ClientJpaRepository
import team.themoment.datagsm.common.domain.oauth.dto.request.Oauth2TokenReqDto
import team.themoment.datagsm.common.domain.oauth.dto.response.Oauth2TokenResDto
import team.themoment.datagsm.common.domain.oauth.entity.OauthRefreshTokenRedisEntity
import team.themoment.datagsm.common.domain.oauth.entity.constant.GrantType
import team.themoment.datagsm.common.domain.oauth.entity.constant.PkceChallengeMethod
import team.themoment.datagsm.common.domain.oauth.exception.OAuthException
import team.themoment.datagsm.common.domain.oauth.repository.OauthCodeRedisRepository
import team.themoment.datagsm.common.domain.oauth.repository.OauthRefreshTokenRedisRepository
import team.themoment.datagsm.common.global.data.OauthJwtEnvironment
import team.themoment.datagsm.oauth.authorization.domain.oauth.service.Oauth2TokenService
import team.themoment.datagsm.oauth.authorization.global.security.jwt.JwtProvider
import team.themoment.datagsm.oauth.authorization.global.util.PkceVerifier
import team.themoment.sdk.exception.ExpectedException
import java.security.MessageDigest

@Service
class Oauth2TokenServiceImpl(
    private val oauthCodeRedisRepository: OauthCodeRedisRepository,
    private val oauthRefreshTokenRedisRepository: OauthRefreshTokenRedisRepository,
    private val clientJpaRepository: ClientJpaRepository,
    private val accountJpaRepository: AccountJpaRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtProvider: JwtProvider,
    private val jwtEnvironment: OauthJwtEnvironment,
) : Oauth2TokenService {
    @Transactional(readOnly = true)
    override fun execute(reqDto: Oauth2TokenReqDto): Oauth2TokenResDto {
        val grantType = GrantType.from(reqDto.grantType)

        return when (grantType) {
            GrantType.AUTHORIZATION_CODE -> handleAuthorizationCode(reqDto)
            GrantType.REFRESH_TOKEN -> handleRefreshToken(reqDto)
            GrantType.CLIENT_CREDENTIALS -> handleClientCredentials(reqDto)
        }
    }

    private fun handleAuthorizationCode(reqDto: Oauth2TokenReqDto): Oauth2TokenResDto {
        if (reqDto.code.isNullOrBlank()) {
            throw OAuthException.InvalidRequest("code 파라미터가 필요합니다.")
        }
        if (reqDto.clientId.isNullOrBlank()) {
            throw OAuthException.InvalidRequest("client_id 파라미터가 필요합니다.")
        }

        val oauthCode =
            oauthCodeRedisRepository.findByIdOrNull(reqDto.code!!)
                ?: throw OAuthException.InvalidGrant("Authorization Code가 유효하지 않거나 만료되었습니다.")

        val hasPkce = oauthCode.codeChallenge != null
        val client =
            if (hasPkce && reqDto.clientSecret.isNullOrBlank()) {
                validateClientWithoutSecret(reqDto.clientId!!)
            } else {
                if (reqDto.clientSecret.isNullOrBlank()) {
                    throw OAuthException.InvalidRequest("client_secret 파라미터가 필요합니다.")
                }
                validateClient(reqDto.clientId!!, reqDto.clientSecret!!)
            }

        if (oauthCode.clientId != reqDto.clientId) {
            throw OAuthException.InvalidGrant("코드가 해당 클라이언트에게 발급되지 않았습니다.")
        }

        if (oauthCode.redirectUri != null) {
            if (reqDto.redirectUri == null) {
                throw OAuthException.InvalidRequest("redirect_uri가 필요합니다.")
            }
            if (oauthCode.redirectUri != reqDto.redirectUri) {
                throw OAuthException.InvalidGrant("redirect_uri가 일치하지 않습니다.")
            }
        }

        if (reqDto.redirectUri != null && !client.redirectUrls.contains(reqDto.redirectUri)) {
            throw OAuthException.InvalidRequest("등록되지 않은 redirect_uri입니다.")
        }

        val codeChallenge = oauthCode.codeChallenge
        if (codeChallenge != null) {
            val codeVerifier =
                reqDto.codeVerifier
                    ?: throw OAuthException.InvalidRequest("code_verifier가 필요합니다.")

            val challengeMethod = PkceChallengeMethod.from(oauthCode.codeChallengeMethod)

            if (!PkceVerifier.verify(
                    codeChallenge,
                    challengeMethod,
                    codeVerifier,
                )
            ) {
                throw OAuthException.InvalidGrant("PKCE 검증에 실패했습니다.")
            }
        }

        val account =
            accountJpaRepository
                .findByEmail(oauthCode.email)
                .orElseThrow { ExpectedException("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND) }

        val requestedScopes = parseScopes(reqDto.scope)
        val grantedScopes = calculateGrantedScopes(client.scopes, requestedScopes)

        val accessToken = jwtProvider.generateOauthAccessToken(account.email, account.role, client.id, grantedScopes)
        val refreshToken = jwtProvider.generateOauthRefreshToken(account.email, client.id)

        saveRefreshToken(account.email, client.id, refreshToken)
        oauthCodeRedisRepository.delete(oauthCode)

        return Oauth2TokenResDto(
            accessToken = accessToken,
            tokenType = "Bearer",
            expiresIn = jwtEnvironment.accessTokenExpiration / 1000,
            refreshToken = refreshToken,
            scope = grantedScopes.joinToString(" ") { it.scope },
        )
    }

    private fun handleRefreshToken(reqDto: Oauth2TokenReqDto): Oauth2TokenResDto {
        validateRefreshTokenParams(reqDto)

        val refreshToken = reqDto.refreshToken!!
        if (!jwtProvider.validateToken(refreshToken)) {
            throw OAuthException.InvalidGrant("Refresh Token이 유효하지 않거나 만료되었습니다.")
        }

        val email = jwtProvider.getEmailFromToken(refreshToken)
        val clientIdFromToken = jwtProvider.getClientIdFromToken(refreshToken)

        val client = validateClient(reqDto.clientId!!, reqDto.clientSecret!!)

        if (clientIdFromToken != reqDto.clientId) {
            throw OAuthException.InvalidGrant("토큰이 해당 클라이언트에게 발급되지 않았습니다.")
        }

        val storedToken =
            oauthRefreshTokenRedisRepository
                .findByEmailAndClientId(email, clientIdFromToken)
                .orElseThrow {
                    OAuthException.InvalidGrant("Refresh Token이 유효하지 않거나 만료되었습니다.")
                }

        if (!MessageDigest.isEqual(
                storedToken.token.toByteArray(),
                refreshToken.toByteArray(),
            )
        ) {
            oauthRefreshTokenRedisRepository.deleteByEmailAndClientId(email, clientIdFromToken)
            throw OAuthException.InvalidGrant("Refresh Token이 유효하지 않거나 만료되었습니다.")
        }

        val account =
            accountJpaRepository
                .findByEmail(email)
                .orElseThrow { ExpectedException("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND) }

        val requestedScopes = parseScopes(reqDto.scope)
        val grantedScopes = calculateGrantedScopes(client.scopes, requestedScopes)

        val newAccessToken = jwtProvider.generateOauthAccessToken(email, account.role, clientIdFromToken, grantedScopes)
        val newRefreshToken = jwtProvider.generateOauthRefreshToken(email, clientIdFromToken)

        saveRefreshToken(email, clientIdFromToken, newRefreshToken)

        return Oauth2TokenResDto(
            accessToken = newAccessToken,
            tokenType = "Bearer",
            expiresIn = jwtEnvironment.accessTokenExpiration / 1000,
            refreshToken = newRefreshToken,
            scope = grantedScopes.joinToString(" ") { it.scope },
        )
    }

    private fun handleClientCredentials(reqDto: Oauth2TokenReqDto): Oauth2TokenResDto {
        validateClientCredentialsParams(reqDto)

        val client = validateClient(reqDto.clientId!!, reqDto.clientSecret!!)

        val requestedScopes = parseScopes(reqDto.scope)
        val grantedScopes = calculateGrantedScopes(client.scopes, requestedScopes)

        val accessToken = jwtProvider.generateClientCredentialsAccessToken(client.id, grantedScopes)

        return Oauth2TokenResDto(
            accessToken = accessToken,
            tokenType = "Bearer",
            expiresIn = jwtEnvironment.accessTokenExpiration / 1000,
            refreshToken = null,
            scope = grantedScopes.joinToString(" ") { it.scope },
        )
    }

    private fun validateClient(
        clientId: String,
        clientSecret: String,
    ): ClientJpaEntity {
        val client =
            clientJpaRepository.findByIdOrNull(clientId)
                ?: throw OAuthException.InvalidClient("존재하지 않는 클라이언트입니다.")

        if (!passwordEncoder.matches(clientSecret, client.secret)) {
            throw OAuthException.InvalidClient("클라이언트 인증에 실패했습니다.")
        }

        return client
    }

    private fun validateClientWithoutSecret(clientId: String): ClientJpaEntity =
        clientJpaRepository.findByIdOrNull(clientId)
            ?: throw OAuthException.InvalidClient("존재하지 않는 클라이언트입니다.")

    private fun parseScopes(scopeString: String?): Set<String> = scopeString?.split(" ")?.filter { it.isNotBlank() }?.toSet() ?: emptySet()

    private fun calculateGrantedScopes(
        clientScopes: Set<String>,
        requestedScopes: Set<String>,
    ): Set<OAuthScope> {
        val scopesToGrant =
            if (requestedScopes.isEmpty()) {
                clientScopes
            } else {
                requestedScopes.intersect(clientScopes)
            }

        return scopesToGrant
            .map { scopeString ->
                OAuthScope.fromString(scopeString)
                    ?: throw ExpectedException("Client에 유효하지 않은 권한범위가 포함되어 있습니다: $scopeString", HttpStatus.INTERNAL_SERVER_ERROR)
            }.toSet()
    }

    private fun saveRefreshToken(
        email: String,
        clientId: String,
        token: String,
    ) {
        oauthRefreshTokenRedisRepository.deleteByEmailAndClientId(email, clientId)
        val ttlSeconds = jwtEnvironment.refreshTokenExpiration / 1000
        val entity =
            OauthRefreshTokenRedisEntity.of(
                email = email,
                clientId = clientId,
                token = token,
                ttl = ttlSeconds,
            )
        oauthRefreshTokenRedisRepository.save(entity)
    }

    private fun validateClientCredentials(reqDto: Oauth2TokenReqDto) {
        if (reqDto.clientId.isNullOrBlank()) {
            throw OAuthException.InvalidRequest("client_id 파라미터가 필요합니다.")
        }
        if (reqDto.clientSecret.isNullOrBlank()) {
            throw OAuthException.InvalidRequest("client_secret 파라미터가 필요합니다.")
        }
    }

    private fun validateRefreshTokenParams(reqDto: Oauth2TokenReqDto) {
        if (reqDto.refreshToken.isNullOrBlank()) {
            throw OAuthException.InvalidRequest("refresh_token 파라미터가 필요합니다.")
        }
        validateClientCredentials(reqDto)
    }

    private fun validateClientCredentialsParams(reqDto: Oauth2TokenReqDto) {
        validateClientCredentials(reqDto)
    }
}
