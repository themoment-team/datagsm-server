package team.themoment.datagsm.oauth.authorization.domain.account.service.impl

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.springframework.security.crypto.password.PasswordEncoder
import team.themoment.datagsm.common.domain.account.dto.request.CreateAccountReqDto
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.common.domain.account.repository.AccountJpaRepository
import team.themoment.datagsm.common.domain.account.repository.EmailCodeRedisRepository
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.oauth.authorization.domain.account.service.CheckSignupEmailService
import team.themoment.sdk.exception.ExpectedException
import java.util.Optional

class CreateAccountServiceImplTest :
    BehaviorSpec({
        val accountJpaRepository = mockk<AccountJpaRepository>()
        val studentJpaRepository = mockk<StudentJpaRepository>()
        val checkSignupEmailService = mockk<CheckSignupEmailService>(relaxed = true)
        val emailCodeRedisRepository = mockk<EmailCodeRedisRepository>(relaxed = true)
        val passwordEncoder = mockk<PasswordEncoder>()

        val service =
            CreateAccountServiceImpl(
                accountJpaRepository,
                studentJpaRepository,
                checkSignupEmailService,
                emailCodeRedisRepository,
                passwordEncoder,
            )

        Given("이미 존재하는 이메일로") {
            val email = "existing@gsm.hs.kr"
            val reqDto = CreateAccountReqDto(email = email, password = "password123", code = "12345678")
            val existingAccount = mockk<AccountJpaEntity>()

            every { accountJpaRepository.findByEmail(email) } returns Optional.of(existingAccount)

            When("계정 생성을 요청하면") {
                Then("409 Conflict 예외가 발생한다") {
                    val exception =
                        shouldThrow<ExpectedException> {
                            service.execute(reqDto)
                        }

                    exception.message shouldBe "이미 해당 이메일을 가진 계정이 존재합니다."
                }
            }
        }

        Given("새로운 이메일로 Student가 없을 때") {
            val email = "new@gsm.hs.kr"
            val password = "password123"
            val encodedPassword = "encodedPassword"
            val reqDto = CreateAccountReqDto(email = email, password = password, code = "12345678")
            val accountSlot = slot<AccountJpaEntity>()
            val savedAccount = mockk<AccountJpaEntity>()

            every { accountJpaRepository.findByEmail(email) } returns Optional.empty()
            every { studentJpaRepository.findByEmail(email) } returns Optional.empty()
            every { passwordEncoder.encode(password) } returns encodedPassword
            every { accountJpaRepository.save(capture(accountSlot)) } returns savedAccount

            When("계정 생성을 요청하면") {
                service.execute(reqDto)

                Then("인증 코드가 검증되고 계정이 생성된다") {
                    verify(exactly = 1) { checkSignupEmailService.execute(any()) }
                    verify(exactly = 1) { emailCodeRedisRepository.deleteById(email) }
                    verify(exactly = 1) { accountJpaRepository.save(any()) }

                    val capturedAccount = accountSlot.captured
                    capturedAccount.email shouldBe email
                    capturedAccount.password shouldBe encodedPassword
                    capturedAccount.role shouldBe AccountRole.USER
                    capturedAccount.student.shouldBeNull()
                }
            }
        }

        Given("새로운 이메일로 Student가 있을 때") {
            val email = "student@gsm.hs.kr"
            val password = "password123"
            val encodedPassword = "encodedPassword"
            val reqDto = CreateAccountReqDto(email = email, password = password, code = "12345678")
            val student = mockk<StudentJpaEntity>()
            val accountSlot = slot<AccountJpaEntity>()
            val savedAccount = mockk<AccountJpaEntity>()

            every { accountJpaRepository.findByEmail(email) } returns Optional.empty()
            every { studentJpaRepository.findByEmail(email) } returns Optional.of(student)
            every { passwordEncoder.encode(password) } returns encodedPassword
            every { accountJpaRepository.save(capture(accountSlot)) } returns savedAccount

            When("계정 생성을 요청하면") {
                service.execute(reqDto)

                Then("Student가 자동으로 연결된다") {
                    val capturedAccount = accountSlot.captured
                    capturedAccount.student.shouldNotBeNull()
                    capturedAccount.student shouldBe student
                }
            }
        }
    })
