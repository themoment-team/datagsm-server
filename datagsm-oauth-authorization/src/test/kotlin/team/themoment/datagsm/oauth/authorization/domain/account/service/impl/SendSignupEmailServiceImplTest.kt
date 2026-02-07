package team.themoment.datagsm.oauth.authorization.domain.account.service.impl

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import team.themoment.datagsm.common.domain.account.dto.request.SendEmailReqDto
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.account.repository.AccountJpaRepository
import team.themoment.datagsm.common.domain.account.repository.EmailCodeRedisRepository
import team.themoment.datagsm.oauth.authorization.global.service.EmailSenderService
import team.themoment.sdk.exception.ExpectedException
import java.util.Optional

class SendSignupEmailServiceImplTest :
    BehaviorSpec({
        val emailCodeRedisRepository = mockk<EmailCodeRedisRepository>(relaxed = true)
        val accountJpaRepository = mockk<AccountJpaRepository>()
        val emailSenderService = mockk<EmailSenderService>(relaxed = true)

        val service =
            SendSignupEmailServiceImpl(
                emailCodeRedisRepository,
                accountJpaRepository,
                emailSenderService,
            )

        Given("이미 존재하는 이메일로") {
            val email = "existing@gsm.hs.kr"
            val reqDto = SendEmailReqDto(email = email)
            val existingAccount = mockk<AccountJpaEntity>()

            every { accountJpaRepository.findByEmail(email) } returns Optional.of(existingAccount)

            When("회원가입 이메일을 요청하면") {
                Then("409 Conflict 예외가 발생한다") {
                    val exception =
                        shouldThrow<ExpectedException> {
                            service.execute(reqDto)
                        }

                    exception.message shouldBe "이미 해당 이메일을 가진 계정이 존재합니다."
                }
            }
        }

        Given("새로운 이메일로") {
            val email = "new@gsm.hs.kr"
            val reqDto = SendEmailReqDto(email = email)

            every { accountJpaRepository.findByEmail(email) } returns Optional.empty()

            When("회원가입 이메일을 요청하면") {
                service.execute(reqDto)

                Then("인증 코드가 생성되고 이메일이 발송된다") {
                    verify(exactly = 1) { emailSenderService.sendEmail(eq(email), any(), any()) }
                    verify(exactly = 1) { emailCodeRedisRepository.save(any()) }
                }
            }
        }
    })
