package team.themoment.datagsm.oauth.authorization.domain.account.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import team.themoment.datagsm.common.domain.account.dto.request.SendEmailReqDto
import team.themoment.datagsm.common.domain.account.entity.EmailCodeRedisEntity
import team.themoment.datagsm.common.domain.account.repository.AccountJpaRepository
import team.themoment.datagsm.common.domain.account.repository.EmailCodeRedisRepository
import team.themoment.datagsm.oauth.authorization.domain.account.service.SendSignupEmailService
import team.themoment.datagsm.oauth.authorization.global.security.annotation.PasswordResetRateLimitType
import team.themoment.datagsm.oauth.authorization.global.security.annotation.PasswordResetRateLimited
import team.themoment.datagsm.oauth.authorization.global.service.EmailSenderService
import team.themoment.datagsm.oauth.authorization.global.template.EmailTemplate
import team.themoment.datagsm.oauth.authorization.global.util.EmailCodeGenerator
import team.themoment.sdk.exception.ExpectedException

@Service
class SendSignupEmailServiceImpl(
    private val emailCodeRedisRepository: EmailCodeRedisRepository,
    private val accountJpaRepository: AccountJpaRepository,
    private val emailSenderService: EmailSenderService,
) : SendSignupEmailService {
    @PasswordResetRateLimited(type = PasswordResetRateLimitType.SIGNUP_SEND_EMAIL)
    override fun execute(reqDto: SendEmailReqDto) {
        if (accountJpaRepository.findByEmail(reqDto.email).isPresent) {
            throw ExpectedException("이미 해당 이메일을 가진 계정이 존재합니다.", HttpStatus.CONFLICT)
        }

        val code = EmailCodeGenerator.generate()
        val emailCodeRedisEntity =
            EmailCodeRedisEntity(
                email = reqDto.email,
                code = code,
                ttl = 300,
            )

        emailSenderService.sendEmail(reqDto.email, EmailTemplate.SIGNUP, code)
        emailCodeRedisRepository.save(emailCodeRedisEntity)
    }
}
