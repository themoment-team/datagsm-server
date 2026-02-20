package team.themoment.datagsm.oauth.authorization.domain.account.service.impl

import org.springframework.stereotype.Service
import team.themoment.datagsm.common.domain.account.dto.request.VerifyPasswordResetCodeReqDto
import team.themoment.datagsm.common.domain.account.repository.PasswordResetCodeRedisRepository
import team.themoment.datagsm.oauth.authorization.domain.account.service.CheckPasswordResetCodeService
import team.themoment.datagsm.oauth.authorization.global.security.annotation.PasswordResetRateLimitType
import team.themoment.datagsm.oauth.authorization.global.security.annotation.PasswordResetRateLimited
import team.themoment.datagsm.oauth.authorization.global.util.EmailCodeValidator

@Service
class CheckPasswordResetCodeServiceImpl(
    private val passwordResetCodeRedisRepository: PasswordResetCodeRedisRepository,
) : CheckPasswordResetCodeService {
    @PasswordResetRateLimited(type = PasswordResetRateLimitType.CHECK_CODE)
    override fun execute(reqDto: VerifyPasswordResetCodeReqDto) {
        val passwordResetCode =
            EmailCodeValidator.validatePasswordResetCode(
                reqDto.email,
                reqDto.code,
                passwordResetCodeRedisRepository,
            )

        val updatedCode = passwordResetCode.copy(verified = true)
        passwordResetCodeRedisRepository.save(updatedCode)
    }
}
