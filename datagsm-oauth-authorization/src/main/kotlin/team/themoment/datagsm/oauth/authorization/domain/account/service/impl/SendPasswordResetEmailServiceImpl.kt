package team.themoment.datagsm.oauth.authorization.domain.account.service.impl

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.mail.MailException
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service
import team.themoment.datagsm.common.domain.account.dto.request.SendPasswordResetEmailReqDto
import team.themoment.datagsm.common.domain.account.entity.PasswordResetCodeRedisEntity
import team.themoment.datagsm.common.domain.account.repository.AccountJpaRepository
import team.themoment.datagsm.common.domain.account.repository.PasswordResetCodeRedisRepository
import team.themoment.datagsm.oauth.authorization.domain.account.service.SendPasswordResetEmailService
import team.themoment.datagsm.oauth.authorization.global.security.annotation.PasswordResetRateLimitType
import team.themoment.datagsm.oauth.authorization.global.security.annotation.PasswordResetRateLimited
import team.themoment.sdk.exception.ExpectedException
import java.security.SecureRandom
import kotlin.math.pow

@Service
class SendPasswordResetEmailServiceImpl(
    private val passwordResetCodeRedisRepository: PasswordResetCodeRedisRepository,
    private val accountJpaRepository: AccountJpaRepository,
    private val javaMailSender: JavaMailSender,
    @param:Value($$"${${spring.mail.from-address}}")
    private val fromAddress: String,
) : SendPasswordResetEmailService {
    companion object {
        private val secureRandom = SecureRandom()
        private const val EMAIL_CODE_LENGTH = 8
        private val EMAIL_RANDOM_MAX = 10.0.pow(EMAIL_CODE_LENGTH).toInt()
    }

    @PasswordResetRateLimited(type = PasswordResetRateLimitType.SEND_EMAIL)
    override fun execute(reqDto: SendPasswordResetEmailReqDto) {
        val account =
            accountJpaRepository
                .findByEmail(reqDto.email)
                .orElseThrow { ExpectedException("존재하지 않는 이메일입니다.", HttpStatus.NOT_FOUND) }

        val code = generateCode()
        val passwordResetCodeRedisEntity =
            PasswordResetCodeRedisEntity(
                email = account.email,
                code = code,
                verified = false,
                ttl = 300,
            )

        sendCode(account.email, code)
        passwordResetCodeRedisRepository.save(passwordResetCodeRedisEntity)
    }

    private fun sendCode(
        email: String,
        code: String,
    ) {
        val message =
            SimpleMailMessage().apply {
                setFrom(fromAddress)
                setTo(email)
                subject = "DataGSM 비밀번호 재설정 코드"
                text = "비밀번호 재설정 인증 코드는 $code 입니다. 5분 이내로 입력해주세요."
            }
        try {
            javaMailSender.send(message)
        } catch (e: MailException) {
            throw RuntimeException("Email 전송에 실패했습니다.", e)
        }
    }

    private fun generateCode(): String = secureRandom.nextInt(0, EMAIL_RANDOM_MAX).toString().padStart(EMAIL_CODE_LENGTH, '0')
}
