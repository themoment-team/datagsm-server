package team.themoment.datagsm.authorization.domain.oauth.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.authorization.domain.oauth.service.ReissueOauthTokenService
import team.themoment.datagsm.authorization.global.security.jwt.JwtProvider
import team.themoment.datagsm.common.domain.account.repository.AccountJpaRepository
import team.themoment.datagsm.common.domain.client.entity.constant.OAuthScope
import team.themoment.datagsm.common.domain.client.repository.ClientJpaRepository
import team.themoment.datagsm.common.domain.oauth.dto.response.OauthTokenResDto
import team.themoment.datagsm.common.domain.oauth.entity.OauthRefreshTokenRedisEntity
import team.themoment.datagsm.common.domain.oauth.repository.OauthRefreshTokenRedisRepository
import team.themoment.datagsm.common.global.data.OauthJwtEnvironment
import team.themoment.sdk.exception.ExpectedException
import java.security.MessageDigest

@Service
class ReissueOauthTokenServiceImpl(
    private val jwtProvider: JwtProvider,
    private val jwtEnvironment: OauthJwtEnvironment,
    private val oauthRefreshTokenRedisRepository: OauthRefreshTokenRedisRepository,
    private val accountJpaRepository: AccountJpaRepository,
    private val clientJpaRepository: ClientJpaRepository,
) : ReissueOauthTokenService {
    @Transactional(readOnly = true)
    override fun execute(refreshToken: String): OauthTokenResDto {
        if (!jwtProvider.validateToken(refreshToken)) {
            throw ExpectedException("유효하지 않은 refresh token입니다.", HttpStatus.UNAUTHORIZED)
        }

        val email = jwtProvider.getEmailFromToken(refreshToken)
        val clientId = jwtProvider.getClientIdFromToken(refreshToken)

        val storedToken =
            oauthRefreshTokenRedisRepository
                .findByEmailAndClientId(email, clientId)
                .orElseThrow {
                    ExpectedException("저장된 refresh token을 찾을 수 없습니다.", HttpStatus.UNAUTHORIZED)
                }

        if (!MessageDigest.isEqual(storedToken.token.toByteArray(), refreshToken.toByteArray())) {
            oauthRefreshTokenRedisRepository.deleteByEmailAndClientId(email, clientId)
            throw ExpectedException("Refresh token이 일치하지 않습니다. 재로그인이 필요합니다.", HttpStatus.UNAUTHORIZED)
        }

        val account =
            accountJpaRepository
                .findByEmail(email)
                .orElseThrow {
                    ExpectedException("계정을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
                }
        val client =
            clientJpaRepository
                .findById(clientId)
                .orElseThrow {
                    ExpectedException("Oauth 클라이언트를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
                }

        val scopes = client.scopes.map { scopeString ->
            OAuthScope.fromString(scopeString)
                ?: throw ExpectedException("Client에 유효하지 않은 scope가 포함되어 있습니다: $scopeString", HttpStatus.INTERNAL_SERVER_ERROR)
        }.toSet()
        val newAccessToken = jwtProvider.generateOauthAccessToken(email, account.role, clientId, scopes)
        val newRefreshToken = jwtProvider.generateOauthRefreshToken(email, clientId)

        oauthRefreshTokenRedisRepository.deleteByEmailAndClientId(email, clientId)

        val ttlSeconds = jwtEnvironment.refreshTokenExpiration.div(1000)
        val newRefreshTokenEntity =
            OauthRefreshTokenRedisEntity.of(
                email = email,
                clientId = clientId,
                token = newRefreshToken,
                ttl = ttlSeconds,
            )
        oauthRefreshTokenRedisRepository.save(newRefreshTokenEntity)

        return OauthTokenResDto(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken,
        )
    }
}
