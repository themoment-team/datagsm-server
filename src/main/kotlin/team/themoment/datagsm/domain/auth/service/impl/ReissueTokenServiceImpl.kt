package team.themoment.datagsm.domain.auth.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.domain.account.repository.AccountJpaRepository
import team.themoment.datagsm.domain.auth.dto.TokenResDto
import team.themoment.datagsm.domain.auth.entity.RefreshTokenRedisEntity
import team.themoment.datagsm.domain.auth.entity.constant.Role
import team.themoment.datagsm.domain.auth.repository.RefreshTokenRedisRepository
import team.themoment.datagsm.domain.auth.service.ReissueTokenService
import team.themoment.datagsm.global.exception.error.ExpectedException
import team.themoment.datagsm.global.security.jwt.JwtProperties
import team.themoment.datagsm.global.security.jwt.JwtProvider

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

        if (storedToken.token != refreshToken) {
            refreshTokenRedisRepository.deleteByEmail(email)
            throw ExpectedException("Refresh token이 일치하지 않습니다. 재로그인이 필요합니다.", HttpStatus.UNAUTHORIZED)
        }

        val account =
            accountJpaRepository
                .findByAccountEmail(email)
                .orElseThrow {
                    ExpectedException("계정을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
                }

        val role = account.accountStudent?.studentRole ?: Role.GENERAL_STUDENT

        val newAccessToken = jwtProvider.generateAccessToken(email, role)
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