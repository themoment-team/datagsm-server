package team.themoment.datagsm.oauth.authorization.domain.account.service.impl

import org.springframework.stereotype.Service
import team.themoment.datagsm.common.domain.account.dto.request.EmailCodeReqDto
import team.themoment.datagsm.common.domain.account.repository.EmailCodeRedisRepository
import team.themoment.datagsm.oauth.authorization.domain.account.service.CheckSignupEmailService
import team.themoment.datagsm.oauth.authorization.global.security.annotation.PasswordResetRateLimitType
import team.themoment.datagsm.oauth.authorization.global.security.annotation.PasswordResetRateLimited
import team.themoment.datagsm.oauth.authorization.global.util.EmailCodeValidator

@Service
class CheckSignupEmailServiceImpl(
    private val emailCodeRedisRepository: EmailCodeRedisRepository,
) : CheckSignupEmailService {
    @PasswordResetRateLimited(type = PasswordResetRateLimitType.SIGNUP_CHECK_CODE)
    override fun execute(reqDto: EmailCodeReqDto) {
        EmailCodeValidator.validateSignupCode(
            reqDto.email,
            reqDto.code,
            emailCodeRedisRepository,
        )
    }
}
