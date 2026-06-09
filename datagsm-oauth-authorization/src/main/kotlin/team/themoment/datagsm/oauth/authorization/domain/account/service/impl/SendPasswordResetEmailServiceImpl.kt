package team.themoment.datagsm.oauth.authorization.domain.account.service.impl

import org.springframework.stereotype.Service
import team.themoment.datagsm.common.domain.account.dto.request.SendPasswordResetEmailReqDto
import team.themoment.datagsm.common.domain.account.entity.PasswordResetCodeRedisEntity
import team.themoment.datagsm.common.domain.account.repository.AccountJpaRepository
import team.themoment.datagsm.common.domain.account.repository.PasswordResetCodeRedisRepository
import team.themoment.datagsm.oauth.authorization.domain.account.service.SendPasswordResetEmailService
import team.themoment.datagsm.oauth.authorization.global.security.annotation.PasswordResetRateLimitType
import team.themoment.datagsm.oauth.authorization.global.security.annotation.PasswordResetRateLimited
import team.themoment.datagsm.oauth.authorization.global.service.EmailSenderService
import team.themoment.datagsm.oauth.authorization.global.template.EmailTemplate
import team.themoment.datagsm.oauth.authorization.global.util.EmailCodeGenerator

@Service
class SendPasswordResetEmailServiceImpl(
    private val passwordResetCodeRedisRepository: PasswordResetCodeRedisRepository,
    private val accountJpaRepository: AccountJpaRepository,
    private val emailSenderService: EmailSenderService,
) : SendPasswordResetEmailService {
    @PasswordResetRateLimited(type = PasswordResetRateLimitType.SEND_EMAIL)
    override fun execute(reqDto: SendPasswordResetEmailReqDto) {
        val account = accountJpaRepository.findByEmail(reqDto.email).orElse(null) ?: return

        val code = EmailCodeGenerator.generate()
        val passwordResetCodeRedisEntity =
            PasswordResetCodeRedisEntity(
                email = account.email,
                code = code,
                verified = false,
                ttl = 300,
            )

        emailSenderService.sendEmail(account.email, EmailTemplate.PASSWORD_RESET, code)
        passwordResetCodeRedisRepository.save(passwordResetCodeRedisEntity)
    }
}
