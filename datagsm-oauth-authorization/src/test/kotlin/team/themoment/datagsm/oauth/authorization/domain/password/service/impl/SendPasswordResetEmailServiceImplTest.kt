package team.themoment.datagsm.oauth.authorization.domain.password.service.impl

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import team.themoment.datagsm.common.domain.account.dto.request.SendPasswordResetEmailReqDto
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.account.repository.AccountJpaRepository
import team.themoment.datagsm.common.domain.account.repository.PasswordResetCodeRedisRepository
import team.themoment.sdk.exception.ExpectedException
import java.util.Optional

class SendPasswordResetEmailServiceImplTest :
    BehaviorSpec({
        val passwordResetCodeRedisRepository = mockk<PasswordResetCodeRedisRepository>()
        val accountJpaRepository = mockk<AccountJpaRepository>()
        val javaMailSender = mockk<JavaMailSender>()

        val service =
            SendPasswordResetEmailServiceImpl(
                passwordResetCodeRedisRepository,
                accountJpaRepository,
                javaMailSender,
            )

        Given("존재하지 않는 이메일로") {
            val email = "nonexistent@gsm.hs.kr"
            val reqDto = SendPasswordResetEmailReqDto(email = email)

            every { accountJpaRepository.findByEmail(email) } returns Optional.empty()

            When("비밀번호 재설정 이메일을 요청하면") {
                Then("404 Not Found 예외가 발생한다") {
                    val exception =
                        shouldThrow<ExpectedException> {
                            service.execute(reqDto)
                        }

                    exception.message shouldBe "존재하지 않는 이메일입니다."
                }
            }
        }

        Given("존재하는 이메일로") {
            val email = "user@gsm.hs.kr"
            val reqDto = SendPasswordResetEmailReqDto(email = email)
            val account =
                AccountJpaEntity().apply {
                    this.email = email
                    this.password = "hashedPassword"
                }

            every { accountJpaRepository.findByEmail(email) } returns Optional.of(account)
            every { passwordResetCodeRedisRepository.save(any()) } answers { firstArg() }
            every { javaMailSender.send(any<SimpleMailMessage>()) } returns Unit

            When("비밀번호 재설정 이메일을 요청하면") {
                service.execute(reqDto)

                Then("코드가 생성되고 Redis에 저장되며 이메일이 발송된다") {
                    verify(exactly = 1) { passwordResetCodeRedisRepository.save(any()) }
                    verify(exactly = 1) { javaMailSender.send(any<SimpleMailMessage>()) }
                    verify(exactly = 1) { accountJpaRepository.findByEmail(email) }
                }
            }
        }
    })
