package team.themoment.datagsm.oauth.authorization.domain.account.service.impl

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import team.themoment.datagsm.common.domain.account.dto.request.CreateAccountReqDto
import team.themoment.datagsm.common.domain.account.repository.AccountJpaRepository
import team.themoment.datagsm.common.domain.account.repository.EmailCodeRedisRepository
import team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.common.domain.teacher.repository.TeacherJpaRepository
import team.themoment.datagsm.common.global.dto.internal.RateLimitConsumeResult
import team.themoment.datagsm.oauth.authorization.domain.account.service.CreateAccountService
import team.themoment.datagsm.oauth.authorization.global.security.annotation.PasswordResetRateLimitType
import team.themoment.datagsm.oauth.authorization.global.security.aspect.PasswordResetRateLimitedAspect
import team.themoment.datagsm.oauth.authorization.global.security.service.PasswordResetRateLimitService
import team.themoment.sdk.exception.ExpectedException
import java.util.Optional

class CreateAccountServiceRateLimitTest :
    DescribeSpec({
        lateinit var accountJpaRepository: AccountJpaRepository
        lateinit var passwordResetRateLimitService: PasswordResetRateLimitService
        lateinit var createAccountService: CreateAccountService

        val email = "s25001@gsm.hs.kr"
        val reqDto = CreateAccountReqDto(email = email, password = "password123", code = "12345678")

        beforeEach {
            accountJpaRepository = mockk()
            passwordResetRateLimitService = mockk()

            val target =
                CreateAccountServiceImpl(
                    accountJpaRepository,
                    mockk<StudentJpaRepository>(),
                    mockk<TeacherJpaRepository>(),
                    mockk<EmailCodeRedisRepository>(),
                    mockk<PasswordEncoder>(),
                )
            createAccountService =
                AspectJProxyFactory(target)
                    .apply {
                        // 애노테이션이 구현 클래스에 있으므로 운영과 같이 클래스 기반 프록시로 만든다.
                        isProxyTargetClass = true
                        addAspect(PasswordResetRateLimitedAspect(passwordResetRateLimitService))
                    }.getProxy()
        }

        describe("CreateAccountService 클래스의") {
            describe("execute 메서드는") {
                context("가입 코드 검증 한도를 모두 소진한 이메일이면") {
                    it("코드를 확인하기 전에 429로 거절해야 한다") {
                        // Given
                        every {
                            passwordResetRateLimitService.tryConsume(email, PasswordResetRateLimitType.SIGNUP_CHECK_CODE)
                        } returns RateLimitConsumeResult(consumed = false, remainingTokens = 0, secondsToWaitForRefill = 60)

                        // When
                        val exception = shouldThrow<ExpectedException> { createAccountService.execute(reqDto) }

                        // Then
                        exception.statusCode shouldBe HttpStatus.TOO_MANY_REQUESTS
                        verify(exactly = 0) { accountJpaRepository.findByEmail(any()) }
                    }
                }

                context("한도가 남아 있으면") {
                    it("가입 코드 검증 API와 같은 버킷을 차감한 뒤 계정 생성을 진행해야 한다") {
                        // Given
                        every {
                            passwordResetRateLimitService.tryConsume(email, PasswordResetRateLimitType.SIGNUP_CHECK_CODE)
                        } returns RateLimitConsumeResult(consumed = true, remainingTokens = 9, secondsToWaitForRefill = 0)
                        every { accountJpaRepository.findByEmail(email) } returns Optional.of(mockk())

                        // When
                        shouldThrow<ExpectedException> { createAccountService.execute(reqDto) }

                        // Then
                        verify(exactly = 1) {
                            passwordResetRateLimitService.tryConsume(email, PasswordResetRateLimitType.SIGNUP_CHECK_CODE)
                        }
                        verify(exactly = 1) { accountJpaRepository.findByEmail(email) }
                    }
                }
            }
        }
    })
