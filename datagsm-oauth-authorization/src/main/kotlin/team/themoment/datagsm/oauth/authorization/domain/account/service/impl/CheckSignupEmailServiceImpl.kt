package team.themoment.datagsm.oauth.authorization.domain.account.service.impl

import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import team.themoment.datagsm.common.domain.account.dto.request.EmailCodeReqDto
import team.themoment.datagsm.common.domain.account.repository.EmailCodeRedisRepository
import team.themoment.datagsm.oauth.authorization.domain.account.service.CheckSignupEmailService
import team.themoment.datagsm.oauth.authorization.global.security.annotation.PasswordResetRateLimitType
import team.themoment.datagsm.oauth.authorization.global.security.annotation.PasswordResetRateLimited
import team.themoment.sdk.exception.ExpectedException

@Service
class CheckSignupEmailServiceImpl(
    private val emailCodeRedisRepository: EmailCodeRedisRepository,
) : CheckSignupEmailService {
    @PasswordResetRateLimited(type = PasswordResetRateLimitType.SIGNUP_CHECK_CODE)
    override fun execute(reqDto: EmailCodeReqDto) {
        val emailCodeRedisEntity =
            emailCodeRedisRepository
                .findByIdOrNull(reqDto.email)
                ?: throw ExpectedException("해당 이메일에 인증 코드가 존재하지 않습니다.", HttpStatus.NOT_FOUND)

        if (emailCodeRedisEntity.code != reqDto.code) {
            throw ExpectedException("인증 코드가 일치하지 않습니다.", HttpStatus.BAD_REQUEST)
        }
    }
}
