package team.themoment.datagsm.web.domain.account.service.impl

import org.springframework.http.HttpStatus
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service
import team.themoment.datagsm.common.domain.account.dto.request.SendEmailReqDto
import team.themoment.datagsm.common.domain.account.entity.EmailCodeRedisEntity
import team.themoment.datagsm.common.domain.account.repository.AccountJpaRepository
import team.themoment.datagsm.common.domain.account.repository.EmailCodeRedisRepository
import team.themoment.datagsm.web.domain.account.service.SendEmailService
import team.themoment.datagsm.web.global.security.annotation.EmailRateLimitType
import team.themoment.datagsm.web.global.security.annotation.EmailRateLimited
import team.themoment.sdk.exception.ExpectedException
import java.security.SecureRandom

@Service
class SendEmailServiceImpl(
    private val emailCodeRedisRepository: EmailCodeRedisRepository,
    private val accountJpaRepository: AccountJpaRepository,
    private val javaMailSender: JavaMailSender,
) : SendEmailService {
    companion object {
        private val secureRandom = SecureRandom()
    }

    @EmailRateLimited(type = EmailRateLimitType.SEND_EMAIL)
    override fun execute(reqDto: SendEmailReqDto) {
        if (accountJpaRepository.findByEmail(reqDto.email).isPresent) {
            throw ExpectedException("이미 해당 이메일을 가진 계정이 존재합니다.", HttpStatus.CONFLICT)
        }

        val emailCodeRedisEntity =
            EmailCodeRedisEntity(
                email = reqDto.email,
                code = generateCode(),
                ttl = 300,
            )

        sendCode(reqDto.email, emailCodeRedisEntity.code)
        emailCodeRedisRepository.save(emailCodeRedisEntity)
    }

    private fun sendCode(
        email: String,
        code: String,
    ) {
        val message =
            SimpleMailMessage().apply {
                setTo(email)
                subject = "DataGSM 인증 코드"
                text = "인증 코드는 $code 입니다. 5분 이내로 입력해주세요."
            }
        try {
            javaMailSender.send(message)
        } catch (e: Exception) {
            throw RuntimeException("Email 전송에 실패했습니다.", e)
        }
    }

    private fun generateCode(): String = secureRandom.nextInt(0, 100000000).toString().padStart(6, '0')
}
