package team.themoment.datagsm.domain.oauth.service.impl

import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.domain.account.repository.AccountJpaRepository
import team.themoment.datagsm.domain.client.entity.ClientJpaEntity
import team.themoment.datagsm.domain.client.repository.ClientJpaRepository
import team.themoment.datagsm.domain.oauth.dto.request.OauthTokenReqDto
import team.themoment.datagsm.domain.oauth.dto.response.OauthTokenResDto
import team.themoment.datagsm.domain.oauth.entity.OauthRefreshTokenRedisEntity
import team.themoment.datagsm.domain.oauth.repository.OauthCodeRedisRepository
import team.themoment.datagsm.domain.oauth.repository.OauthRefreshTokenRedisRepository
import team.themoment.datagsm.domain.oauth.service.ExchangeTokenService
import team.themoment.datagsm.global.exception.error.ExpectedException
import team.themoment.datagsm.global.security.jwt.JwtProperties
import team.themoment.datagsm.global.security.jwt.JwtProvider

@Service
class ExchangeTokenServiceImpl(
    private val accountJpaRepository: AccountJpaRepository,
    private val clientJpaRepository: ClientJpaRepository,
    private val passwordEncoder: PasswordEncoder,
    private val oauthCodeRedisRepository: OauthCodeRedisRepository,
    private val jwtProvider: JwtProvider,
    private val jwtProperties: JwtProperties,
    private val oauthRefreshTokenRedisRepository: OauthRefreshTokenRedisRepository,
) : ExchangeTokenService {
    @Transactional(readOnly = true)
    override fun execute(reqDto: OauthTokenReqDto): OauthTokenResDto {
        val oauthCodeRedisEntity =
            oauthCodeRedisRepository
                .findById(reqDto.code)
                .orElseThrow { ExpectedException("존재하지 않거나 만료된 코드입니다.", HttpStatus.NOT_FOUND) }
        val client =
            clientJpaRepository
                .findById(oauthCodeRedisEntity.clientId)
                .orElseThrow { ExpectedException("인증하려는 Client가 존재하지 않습니다.", HttpStatus.NOT_FOUND) }

        validateClientSecret(reqDto, client)

        val account =
            accountJpaRepository
                .findByEmail(oauthCodeRedisEntity.email)
                .orElseThrow { ExpectedException("코드에 해당하는 사용자가 존재하지 않습니다.", HttpStatus.NOT_FOUND) }

        oauthCodeRedisRepository.delete(oauthCodeRedisEntity)

        val scopes = client.scopes.map { it.scope }.toSet()
        val accessToken = jwtProvider.generateOauthAccessToken(account.email, account.role, client.id, scopes)
        val refreshToken = jwtProvider.generateOauthRefreshToken(account.email, client.id)

        oauthRefreshTokenRedisRepository.deleteByEmailAndClientId(account.email, client.id)
        val ttlSeconds = jwtProperties.oauthRefreshTokenExpiration / 1000
        val refreshTokenEntity =
            OauthRefreshTokenRedisEntity.of(
                email = account.email,
                clientId = client.id,
                token = refreshToken,
                ttl = ttlSeconds,
            )
        oauthRefreshTokenRedisRepository.save(refreshTokenEntity)

        return OauthTokenResDto(
            accessToken = accessToken,
            refreshToken = refreshToken,
        )
    }

    private fun validateClientSecret(
        reqDto: OauthTokenReqDto,
        client: ClientJpaEntity,
    ) {
        if (!passwordEncoder.matches(reqDto.clientSecret, client.secret)) {
            throw ExpectedException("Client Secret이 일치하지 않습니다.", HttpStatus.BAD_REQUEST)
        }
    }
}
