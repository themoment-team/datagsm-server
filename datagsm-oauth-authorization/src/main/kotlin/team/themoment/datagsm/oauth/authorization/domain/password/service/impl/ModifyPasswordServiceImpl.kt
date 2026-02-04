package team.themoment.datagsm.oauth.authorization.domain.password.service.impl

import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import team.themoment.datagsm.common.domain.account.dto.request.ChangePasswordReqDto
import team.themoment.datagsm.common.domain.account.repository.AccountJpaRepository
import team.themoment.datagsm.common.domain.account.repository.PasswordResetCodeRedisRepository
import team.themoment.datagsm.common.domain.oauth.repository.OauthRefreshTokenRedisRepository
import team.themoment.datagsm.oauth.authorization.domain.password.service.ModifyPasswordService
import team.themoment.datagsm.oauth.authorization.global.security.annotation.PasswordResetRateLimitType
import team.themoment.datagsm.oauth.authorization.global.security.annotation.PasswordResetRateLimited
import team.themoment.sdk.exception.ExpectedException

@Service
class ModifyPasswordServiceImpl(
    private val passwordResetCodeRedisRepository: PasswordResetCodeRedisRepository,
    private val accountJpaRepository: AccountJpaRepository,
    private val passwordEncoder: PasswordEncoder,
    private val oauthRefreshTokenRedisRepository: OauthRefreshTokenRedisRepository,
) : ModifyPasswordService {
    @PasswordResetRateLimited(type = PasswordResetRateLimitType.MODIFY_PASSWORD)
    override fun execute(reqDto: ChangePasswordReqDto) {
        val passwordResetCode =
            passwordResetCodeRedisRepository
                .findById(reqDto.email)
                .orElseThrow { ExpectedException("인증 코드가 존재하지 않습니다.", HttpStatus.NOT_FOUND) }

        if (!passwordResetCode.verified) {
            throw ExpectedException("인증 코드 검증이 필요합니다.", HttpStatus.BAD_REQUEST)
        }

        if (passwordResetCode.code != reqDto.code) {
            throw ExpectedException("인증 코드가 일치하지 않습니다.", HttpStatus.BAD_REQUEST)
        }

        val account =
            accountJpaRepository
                .findByEmail(reqDto.email)
                .orElseThrow { ExpectedException("존재하지 않는 이메일입니다.", HttpStatus.NOT_FOUND) }

        if (passwordEncoder.matches(reqDto.newPassword, account.password)) {
            throw ExpectedException("이전 비밀번호와 동일한 비밀번호는 사용할 수 없습니다.", HttpStatus.BAD_REQUEST)
        }

        account.password = passwordEncoder.encode(reqDto.newPassword)!!
        accountJpaRepository.save(account)

        passwordResetCodeRedisRepository.deleteById(reqDto.email)

        val tokens = oauthRefreshTokenRedisRepository.findAllByEmail(reqDto.email)
        tokens.forEach { oauthRefreshTokenRedisRepository.delete(it) }
    }
}
