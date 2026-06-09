package team.themoment.datagsm.oauth.authorization.domain.account.password.service.impl

import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import team.themoment.datagsm.common.domain.account.dto.request.SendPasswordResetEmailReqDto
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.account.repository.AccountJpaRepository
import team.themoment.datagsm.common.domain.account.repository.PasswordResetCodeRedisRepository
import team.themoment.datagsm.oauth.authorization.domain.account.service.impl.SendPasswordResetEmailServiceImpl
import team.themoment.datagsm.oauth.authorization.global.service.EmailSenderService
import java.util.Optional

class SendPasswordResetEmailServiceImplTest :
    BehaviorSpec({
        val passwordResetCodeRedisRepository = mockk<PasswordResetCodeRedisRepository>(relaxed = true)
        val accountJpaRepository = mockk<AccountJpaRepository>()
        val emailSenderService = mockk<EmailSenderService>(relaxed = true)

        val service =
            SendPasswordResetEmailServiceImpl(
                passwordResetCodeRedisRepository,
                accountJpaRepository,
                emailSenderService,
            )

        Given("존재하지 않는 이메일로") {
            val email = "nonexistent@gsm.hs.kr"
            val reqDto = SendPasswordResetEmailReqDto(email = email)

            every { accountJpaRepository.findByEmail(email) } returns Optional.empty()

            When("비밀번호 재설정 이메일을 요청하면") {
                Then("예외 없이 정상 종료하며 코드 발송과 저장이 일어나지 않는다") {
                    service.execute(reqDto)

                    verify(exactly = 0) { emailSenderService.sendEmail(any(), any(), any()) }
                    verify(exactly = 0) { passwordResetCodeRedisRepository.save(any()) }
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

            When("비밀번호 재설정 이메일을 요청하면") {
                Then("코드가 생성되고 Redis에 저장되며 이메일이 발송된다") {
                    service.execute(reqDto)

                    verify(exactly = 1) { passwordResetCodeRedisRepository.save(any()) }
                    verify(exactly = 1) { emailSenderService.sendEmail(any(), any(), any()) }
                    verify(exactly = 1) { accountJpaRepository.findByEmail(email) }
                }
            }
        }
    })
