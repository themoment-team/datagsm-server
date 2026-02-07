package team.themoment.datagsm.oauth.authorization.global.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.MailException
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service
import team.themoment.datagsm.oauth.authorization.global.template.EmailTemplate

@Service
class EmailSenderService(
    private val javaMailSender: JavaMailSender,
    @Value("\${spring.mail.from-address}")
    private val fromAddress: String,
) {
    fun sendEmail(
        to: String,
        template: EmailTemplate,
        code: String,
    ) {
        val message =
            SimpleMailMessage().apply {
                setFrom(fromAddress)
                setTo(to)
                subject = template.subject
                text = template.formatBody(code)
            }
        try {
            javaMailSender.send(message)
        } catch (e: MailException) {
            throw RuntimeException("Email 전송에 실패했습니다.", e)
        }
    }
}
