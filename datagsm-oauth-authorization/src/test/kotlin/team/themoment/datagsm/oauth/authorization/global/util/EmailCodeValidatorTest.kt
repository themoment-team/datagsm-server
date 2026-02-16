package team.themoment.datagsm.oauth.authorization.global.util

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.data.repository.findByIdOrNull
import team.themoment.datagsm.common.domain.account.entity.EmailCodeRedisEntity
import team.themoment.datagsm.common.domain.account.entity.PasswordResetCodeRedisEntity
import team.themoment.datagsm.common.domain.account.repository.EmailCodeRedisRepository
import team.themoment.datagsm.common.domain.account.repository.PasswordResetCodeRedisRepository
import team.themoment.sdk.exception.ExpectedException
import java.util.Optional

class EmailCodeValidatorTest :
    BehaviorSpec({
        context("validateSignupCode") {
            val emailCodeRedisRepository = mockk<EmailCodeRedisRepository>()

            Given("이메일 코드가 존재하지 않을 때") {
                val email = "test@gsm.hs.kr"
                val code = "12345678"

                every { emailCodeRedisRepository.findByIdOrNull(email) } returns null

                When("validateSignupCode를 호출하면") {
                    Then("404 Not Found 예외가 발생한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                EmailCodeValidator.validateSignupCode(email, code, emailCodeRedisRepository)
                            }

                        exception.message shouldBe "해당 이메일에 인증 코드가 존재하지 않습니다."
                    }
                }
            }

            Given("이메일 코드가 일치하지 않을 때") {
                val email = "test@gsm.hs.kr"
                val code = "12345678"
                val storedEntity = EmailCodeRedisEntity(email = email, code = "87654321", ttl = 300)

                every { emailCodeRedisRepository.findByIdOrNull(email) } returns storedEntity

                When("validateSignupCode를 호출하면") {
                    Then("400 Bad Request 예외가 발생한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                EmailCodeValidator.validateSignupCode(email, code, emailCodeRedisRepository)
                            }

                        exception.message shouldBe "인증 코드가 일치하지 않습니다."
                    }
                }
            }

            Given("이메일 코드가 일치할 때") {
                val email = "test@gsm.hs.kr"
                val code = "12345678"
                val storedEntity = EmailCodeRedisEntity(email = email, code = code, ttl = 300)

                every { emailCodeRedisRepository.findByIdOrNull(email) } returns storedEntity

                When("validateSignupCode를 호출하면") {
                    Then("예외가 발생하지 않는다") {
                        EmailCodeValidator.validateSignupCode(email, code, emailCodeRedisRepository)
                    }
                }
            }
        }

        context("validatePasswordResetCode") {
            val passwordResetCodeRedisRepository = mockk<PasswordResetCodeRedisRepository>()

            Given("비밀번호 재설정 코드가 존재하지 않을 때") {
                val email = "test@gsm.hs.kr"
                val code = "12345678"

                every { passwordResetCodeRedisRepository.findById(email) } returns Optional.empty()

                When("validatePasswordResetCode를 호출하면") {
                    Then("404 Not Found 예외가 발생한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                EmailCodeValidator.validatePasswordResetCode(
                                    email,
                                    code,
                                    passwordResetCodeRedisRepository,
                                )
                            }

                        exception.message shouldBe "해당 이메일에 인증 코드가 존재하지 않습니다."
                    }
                }
            }

            Given("비밀번호 재설정 코드가 일치하지 않을 때") {
                val email = "test@gsm.hs.kr"
                val code = "12345678"
                val storedEntity = PasswordResetCodeRedisEntity(email = email, code = "87654321", ttl = 300)

                every { passwordResetCodeRedisRepository.findById(email) } returns Optional.of(storedEntity)

                When("validatePasswordResetCode를 호출하면") {
                    Then("400 Bad Request 예외가 발생한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                EmailCodeValidator.validatePasswordResetCode(
                                    email,
                                    code,
                                    passwordResetCodeRedisRepository,
                                )
                            }

                        exception.message shouldBe "인증 코드가 일치하지 않습니다."
                    }
                }
            }

            Given("비밀번호 재설정 코드가 일치할 때") {
                val email = "test@gsm.hs.kr"
                val code = "12345678"
                val storedEntity = PasswordResetCodeRedisEntity(email = email, code = code, ttl = 300)

                every { passwordResetCodeRedisRepository.findById(email) } returns Optional.of(storedEntity)

                When("validatePasswordResetCode를 호출하면") {
                    Then("예외가 발생하지 않는다") {
                        EmailCodeValidator.validatePasswordResetCode(
                            email,
                            code,
                            passwordResetCodeRedisRepository,
                        )
                    }
                }
            }
        }
    })
