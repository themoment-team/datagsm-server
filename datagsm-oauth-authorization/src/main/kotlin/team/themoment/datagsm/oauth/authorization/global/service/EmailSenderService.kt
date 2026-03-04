package team.themoment.datagsm.oauth.authorization.global.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service
import team.themoment.datagsm.oauth.authorization.global.template.EmailTemplate

/**
 * 이메일 발송 Infrastructure Service
 *
 * Spring의 JavaMailSender를 wrapping하여 이메일 발송 기능을 제공합니다.
 *
 * **Architecture Note**:
 * - 이 클래스는 기술적 infrastructure service로서 비즈니스 로직 Service와 구분됩니다.
 * - 도메인 Service에서 이 클래스를 의존하는 것은 클린 아키텍처 원칙에 부합합니다.
 * - Service → Infrastructure 의존은 정상적인 의존 방향입니다.
 *
 * @see team.themoment.datagsm.oauth.authorization.domain.account.service.impl.SendSignupEmailServiceImpl
 */
@Service
class EmailSenderService(
    private val javaMailSender: JavaMailSender,
    @param:Value("\${spring.mail.from-address}")
    private val fromAddress: String,
) {
    fun sendEmail(
        to: String,
        template: EmailTemplate,
        code: String,
    ) {
        val message =
            SimpleMailMessage().apply {
                from = fromAddress
                setTo(to)
                subject = template.subject
                text = template.formatBody(code)
            }
        javaMailSender.send(message)
    }
}
