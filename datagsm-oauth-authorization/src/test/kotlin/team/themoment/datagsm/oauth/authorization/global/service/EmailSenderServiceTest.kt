package team.themoment.datagsm.oauth.authorization.global.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import team.themoment.datagsm.oauth.authorization.global.template.EmailTemplate

class EmailSenderServiceTest :
    BehaviorSpec({
        val javaMailSender = mockk<JavaMailSender>()
        val emailSenderService = EmailSenderService(javaMailSender, "noreply@datagsm.kr")

        Given("회원가입 인증 코드를 발송할 때") {
            val email = "test@gsm.hs.kr"
            val code = "12345678"
            val messageSlot = slot<SimpleMailMessage>()

            every { javaMailSender.send(capture(messageSlot)) } returns Unit

            When("sendEmail을 호출하면") {
                emailSenderService.sendEmail(email, EmailTemplate.SIGNUP, code)

                Then("올바른 내용으로 이메일이 발송된다") {
                    verify(exactly = 1) { javaMailSender.send(any<SimpleMailMessage>()) }

                    val capturedMessage = messageSlot.captured
                    capturedMessage.from shouldBe "noreply@datagsm.kr"
                    capturedMessage.to?.first() shouldBe email
                    capturedMessage.subject shouldBe "DataGSM 인증 코드"
                    capturedMessage.text shouldBe "인증 코드는 $code 입니다. 5분 이내로 입력해주세요."
                }
            }
        }

        Given("비밀번호 재설정 코드를 발송할 때") {
            val email = "test@gsm.hs.kr"
            val code = "87654321"
            val messageSlot = slot<SimpleMailMessage>()

            every { javaMailSender.send(capture(messageSlot)) } returns Unit

            When("sendEmail을 호출하면") {
                emailSenderService.sendEmail(email, EmailTemplate.PASSWORD_RESET, code)

                Then("올바른 내용으로 이메일이 발송된다") {
                    val capturedMessage = messageSlot.captured
                    capturedMessage.subject shouldBe "DataGSM 비밀번호 재설정 코드"
                    capturedMessage.text shouldBe "비밀번호 재설정 인증 코드는 $code 입니다. 5분 이내로 입력해주세요."
                }
            }
        }

        Given("이메일 발송이 실패할 때") {
            val email = "test@gsm.hs.kr"
            val code = "12345678"

            every { javaMailSender.send(any<SimpleMailMessage>()) } throws RuntimeException("발송 실패")

            When("sendEmail을 호출하면") {
                Then("RuntimeException이 발생한다") {
                    shouldThrow<RuntimeException> {
                        emailSenderService.sendEmail(email, EmailTemplate.SIGNUP, code)
                    }
                }
            }
        }
    })
