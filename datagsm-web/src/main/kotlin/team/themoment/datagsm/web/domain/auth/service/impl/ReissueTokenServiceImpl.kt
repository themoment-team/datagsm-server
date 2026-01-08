package team.themoment.datagsm.web.domain.auth.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.account.RefreshTokenRedisEntity
import team.themoment.datagsm.web.domain.account.repository.AccountJpaRepository
import team.themoment.datagsm.web.domain.auth.dto.response.TokenResDto
import team.themoment.datagsm.web.domain.auth.repository.RefreshTokenRedisRepository
import team.themoment.datagsm.web.domain.auth.service.ReissueTokenService
import team.themoment.datagsm.web.global.security.jwt.JwtProperties
import team.themoment.datagsm.web.global.security.jwt.JwtProvider
import team.themoment.sdk.exception.ExpectedException
import java.security.MessageDigest

@Service
class ReissueTokenServiceImpl(
    private val jwtProvider: JwtProvider,
    private val jwtProperties: JwtProperties,
    private val refreshTokenRedisRepository: RefreshTokenRedisRepository,
    private val accountJpaRepository: AccountJpaRepository,
) : ReissueTokenService {
    @Transactional
    override fun execute(refreshToken: String): TokenResDto {
        if (!jwtProvider.validateToken(refreshToken)) {
            throw ExpectedException("유효하지 않은 refresh token입니다.", HttpStatus.UNAUTHORIZED)
        }

        val email = jwtProvider.getEmailFromToken(refreshToken)

        val storedToken =
            refreshTokenRedisRepository
                .findByEmail(email)
                .orElseThrow {
                    ExpectedException("저장된 refresh token을 찾을 수 없습니다.", HttpStatus.UNAUTHORIZED)
                }

        if (!MessageDigest.isEqual(storedToken.token.toByteArray(), refreshToken.toByteArray())) {
            refreshTokenRedisRepository.deleteByEmail(email)
            throw ExpectedException("Refresh token이 일치하지 않습니다. 재로그인이 필요합니다.", HttpStatus.UNAUTHORIZED)
        }

        val account =
            accountJpaRepository
                .findByEmail(email)
                .orElseThrow {
                    ExpectedException("계정을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
                }

        val newAccessToken = jwtProvider.generateAccessToken(email, account.role)
        val newRefreshToken = jwtProvider.generateRefreshToken(email)

        refreshTokenRedisRepository.deleteByEmail(email)

        val ttlSeconds = jwtProperties.refreshTokenExpiration / 1000
        val newRefreshTokenEntity =
            RefreshTokenRedisEntity.of(
                email = email,
                token = newRefreshToken,
                ttl = ttlSeconds,
            )
        refreshTokenRedisRepository.save(newRefreshTokenEntity)

        return TokenResDto(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken,
        )
    }
}
