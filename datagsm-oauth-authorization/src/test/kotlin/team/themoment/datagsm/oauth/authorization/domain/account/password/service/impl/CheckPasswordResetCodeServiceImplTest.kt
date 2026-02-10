package team.themoment.datagsm.oauth.authorization.domain.account.password.service.impl

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import team.themoment.datagsm.common.domain.account.dto.request.VerifyPasswordResetCodeReqDto
import team.themoment.datagsm.common.domain.account.entity.PasswordResetCodeRedisEntity
import team.themoment.datagsm.common.domain.account.repository.PasswordResetCodeRedisRepository
import team.themoment.datagsm.oauth.authorization.domain.account.service.impl.CheckPasswordResetCodeServiceImpl
import team.themoment.sdk.exception.ExpectedException
import java.util.Optional

class CheckPasswordResetCodeServiceImplTest :
    BehaviorSpec({
        val passwordResetCodeRedisRepository = mockk<PasswordResetCodeRedisRepository>()

        val service = CheckPasswordResetCodeServiceImpl(passwordResetCodeRedisRepository)

        Given("Redis에 코드가 없을 때") {
            val email = "user@gsm.hs.kr"
            val code = "12345678"
            val reqDto = VerifyPasswordResetCodeReqDto(email = email, code = code)

            every { passwordResetCodeRedisRepository.findById(email) } returns Optional.empty()

            When("코드 검증을 요청하면") {
                Then("404 Not Found 예외가 발생한다") {
                    val exception =
                        shouldThrow<ExpectedException> {
                            service.execute(reqDto)
                        }

                    exception.message shouldBe "인증 코드가 존재하지 않습니다."
                }
            }
        }

        Given("코드가 일치하지 않을 때") {
            val email = "user@gsm.hs.kr"
            val correctCode = "12345678"
            val wrongCode = "87654321"
            val reqDto = VerifyPasswordResetCodeReqDto(email = email, code = wrongCode)
            val redisEntity =
                PasswordResetCodeRedisEntity(
                    email = email,
                    code = correctCode,
                    verified = false,
                    ttl = 300,
                )

            every { passwordResetCodeRedisRepository.findById(email) } returns Optional.of(redisEntity)
            every { passwordResetCodeRedisRepository.save(any()) } answers { firstArg() }

            When("코드 검증을 요청하면") {
                Then("400 Bad Request 예외가 발생한다") {
                    val exception =
                        shouldThrow<ExpectedException> {
                            service.execute(reqDto)
                        }

                    exception.message shouldBe "인증 코드가 일치하지 않습니다."
                }
            }
        }

        Given("코드가 일치할 때") {
            val email = "user@gsm.hs.kr"
            val code = "12345678"
            val reqDto = VerifyPasswordResetCodeReqDto(email = email, code = code)
            val redisEntity =
                PasswordResetCodeRedisEntity(
                    email = email,
                    code = code,
                    verified = false,
                    ttl = 300,
                )

            every { passwordResetCodeRedisRepository.findById(email) } returns Optional.of(redisEntity)
            every { passwordResetCodeRedisRepository.save(any()) } answers { firstArg() }

            When("코드 검증을 요청하면") {
                Then("verified가 true로 변경되고 Redis에 저장된다") {
                    service.execute(reqDto)
                    verify(exactly = 1) { passwordResetCodeRedisRepository.save(any()) }
                }
            }
        }
    })
