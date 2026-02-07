package team.themoment.datagsm.oauth.authorization.domain.account.service.impl

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.data.repository.findByIdOrNull
import team.themoment.datagsm.common.domain.account.dto.request.EmailCodeReqDto
import team.themoment.datagsm.common.domain.account.entity.EmailCodeRedisEntity
import team.themoment.datagsm.common.domain.account.repository.EmailCodeRedisRepository
import team.themoment.sdk.exception.ExpectedException

class CheckSignupEmailServiceImplTest :
    BehaviorSpec({
        val emailCodeRedisRepository = mockk<EmailCodeRedisRepository>()
        val service = CheckSignupEmailServiceImpl(emailCodeRedisRepository)

        Given("인증 코드가 존재하지 않을 때") {
            val email = "test@gsm.hs.kr"
            val reqDto = EmailCodeReqDto(email = email, code = "12345678")

            every { emailCodeRedisRepository.findByIdOrNull(email) } returns null

            When("인증 코드를 확인하면") {
                Then("404 Not Found 예외가 발생한다") {
                    val exception =
                        shouldThrow<ExpectedException> {
                            service.execute(reqDto)
                        }

                    exception.message shouldBe "해당 이메일에 인증 코드가 존재하지 않습니다."
                }
            }
        }

        Given("인증 코드가 일치하지 않을 때") {
            val email = "test@gsm.hs.kr"
            val reqDto = EmailCodeReqDto(email = email, code = "12345678")
            val storedEntity = EmailCodeRedisEntity(email = email, code = "87654321", ttl = 300)

            every { emailCodeRedisRepository.findByIdOrNull(email) } returns storedEntity

            When("인증 코드를 확인하면") {
                Then("400 Bad Request 예외가 발생한다") {
                    val exception =
                        shouldThrow<ExpectedException> {
                            service.execute(reqDto)
                        }

                    exception.message shouldBe "인증 코드가 일치하지 않습니다."
                }
            }
        }

        Given("인증 코드가 일치할 때") {
            val email = "test@gsm.hs.kr"
            val code = "12345678"
            val reqDto = EmailCodeReqDto(email = email, code = code)
            val storedEntity = EmailCodeRedisEntity(email = email, code = code, ttl = 300)

            every { emailCodeRedisRepository.findByIdOrNull(email) } returns storedEntity

            When("인증 코드를 확인하면") {
                Then("예외가 발생하지 않는다") {
                    service.execute(reqDto)
                }
            }
        }
    })
