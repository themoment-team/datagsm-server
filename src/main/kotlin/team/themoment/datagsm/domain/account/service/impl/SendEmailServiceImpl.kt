package team.themoment.datagsm.domain.account.service.impl

import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service
import team.themoment.datagsm.domain.account.dto.request.SendEmailReqDto
import team.themoment.datagsm.domain.account.entity.EmailCodeRedisEntity
import team.themoment.datagsm.domain.account.repository.EmailCodeRedisRepository
import team.themoment.datagsm.domain.account.service.SendEmailService
import java.security.SecureRandom

@Service
class SendEmailServiceImpl(
    val emailCodeRedisRepository: EmailCodeRedisRepository,
    val secureRandom: SecureRandom = SecureRandom(),
    val javaMailSender: JavaMailSender,
) : SendEmailService {
    override fun execute(reqDto: SendEmailReqDto) {
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
            e.printStackTrace()
            throw RuntimeException()
        }
    }

    private fun generateCode(): String = secureRandom.nextInt(0, 1000000).toString().padStart(6, '0')
}
