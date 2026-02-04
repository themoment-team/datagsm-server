package team.themoment.datagsm.oauth.authorization.domain.account.password.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import team.themoment.datagsm.common.domain.account.dto.request.VerifyPasswordResetCodeReqDto
import team.themoment.datagsm.common.domain.account.repository.PasswordResetCodeRedisRepository
import team.themoment.datagsm.oauth.authorization.domain.account.password.service.CheckPasswordResetCodeService
import team.themoment.datagsm.oauth.authorization.global.security.annotation.PasswordResetRateLimitType
import team.themoment.datagsm.oauth.authorization.global.security.annotation.PasswordResetRateLimited
import team.themoment.sdk.exception.ExpectedException

@Service
class CheckPasswordResetCodeServiceImpl(
    private val passwordResetCodeRedisRepository: PasswordResetCodeRedisRepository,
) : CheckPasswordResetCodeService {
    @PasswordResetRateLimited(type = PasswordResetRateLimitType.CHECK_CODE)
    override fun execute(reqDto: VerifyPasswordResetCodeReqDto) {
        val passwordResetCode =
            passwordResetCodeRedisRepository
                .findById(reqDto.email)
                .orElseThrow { ExpectedException("인증 코드가 존재하지 않습니다.", HttpStatus.NOT_FOUND) }

        if (passwordResetCode.code != reqDto.code) {
            throw ExpectedException("인증 코드가 일치하지 않습니다.", HttpStatus.BAD_REQUEST)
        }

        val updatedCode = passwordResetCode.copy(verified = true)
        passwordResetCodeRedisRepository.save(updatedCode)
    }
}
