package team.themoment.datagsm.domain.auth.service.impl

import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import team.themoment.datagsm.domain.account.repository.AccountJpaRepository
import team.themoment.datagsm.domain.auth.dto.request.LoginReqDto
import team.themoment.datagsm.domain.auth.dto.response.TokenResDto
import team.themoment.datagsm.domain.auth.entity.RefreshTokenRedisEntity
import team.themoment.datagsm.domain.auth.repository.RefreshTokenRedisRepository
import team.themoment.datagsm.domain.auth.service.LoginService
import team.themoment.datagsm.global.exception.error.ExpectedException
import team.themoment.datagsm.global.security.jwt.JwtProperties
import team.themoment.datagsm.global.security.jwt.JwtProvider

@Service
class LoginServiceImpl(
    val accountRepository: AccountJpaRepository,
    val jwtProvider: JwtProvider,
    val refreshTokenRedisRepository: RefreshTokenRedisRepository,
    val jwtProperties: JwtProperties,
    val passwordEncoder: PasswordEncoder,
) : LoginService {
    override fun execute(reqDto: LoginReqDto): TokenResDto {
        val account =
            accountRepository
                .findByEmail(reqDto.email)
                .orElseThrow { ExpectedException("존재하지 않는 계정 이메일입니다.", HttpStatus.NOT_FOUND) }
        validatePassword(reqDto.password, account.password)
        val accessToken = jwtProvider.generateAccessToken(account.email, account.role)
        val refreshToken = jwtProvider.generateRefreshToken(reqDto.email)
        refreshTokenRedisRepository.deleteByEmail(reqDto.email)
        val ttlSeconds = jwtProperties.refreshTokenExpiration / 1000
        val refreshTokenEntity =
            RefreshTokenRedisEntity.of(
                email = account.email,
                token = refreshToken,
                ttl = ttlSeconds,
            )
        refreshTokenRedisRepository.save(refreshTokenEntity)
        return TokenResDto(
            accessToken = accessToken,
            refreshToken = refreshToken,
        )
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
