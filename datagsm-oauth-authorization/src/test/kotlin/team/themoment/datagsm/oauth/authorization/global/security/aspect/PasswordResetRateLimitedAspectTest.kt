package team.themoment.datagsm.oauth.authorization.global.security.aspect

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.http.HttpStatus
import team.themoment.datagsm.common.domain.oauth.dto.request.OauthAuthorizeSubmitReqDto
import team.themoment.datagsm.common.domain.student.dto.request.QueryStudentDataEditRequestReqDto
import team.themoment.datagsm.common.global.dto.internal.RateLimitConsumeResult
import team.themoment.datagsm.oauth.authorization.domain.oauth.service.impl.CompleteOauthAuthorizeFlowServiceImpl
import team.themoment.datagsm.oauth.authorization.domain.oauth.service.impl.QueryStudentDataEditRequestServiceImpl
import team.themoment.datagsm.oauth.authorization.global.security.annotation.PasswordResetRateLimitType
import team.themoment.datagsm.oauth.authorization.global.security.annotation.PasswordResetRateLimited
import team.themoment.datagsm.oauth.authorization.global.security.service.PasswordResetRateLimitService
import team.themoment.sdk.exception.ExpectedException

class PasswordResetRateLimitedAspectTest :
    DescribeSpec({

        val loginReqDto =
            OauthAuthorizeSubmitReqDto(
                email = "student@gsm.hs.kr",
                password = "password123!",
                token = "state-token",
            )

        fun loginJoinPoint(): JoinPoint {
            val method =
                CompleteOauthAuthorizeFlowServiceImpl::class.java
                    .getMethod("execute", OauthAuthorizeSubmitReqDto::class.java)
            val signature = mockk<MethodSignature>()
            every { signature.method } returns method
            val joinPoint = mockk<JoinPoint>()
            every { joinPoint.signature } returns signature
            every { joinPoint.args } returns arrayOf(loginReqDto)
            return joinPoint
        }

        describe("PasswordResetRateLimitedAspect 클래스의") {
            describe("checkPasswordResetRateLimit 메서드는") {

                context("로그인 요청의 한도가 남아 있을 때") {
                    it("요청 이메일과 LOGIN 타입으로 한도를 차감하고 통과시켜야 한다") {
                        val rateLimitService = mockk<PasswordResetRateLimitService>()
                        every { rateLimitService.tryConsume(any(), any()) } returns
                            RateLimitConsumeResult(consumed = true, remainingTokens = 9L, secondsToWaitForRefill = 0L)
                        val aspect = PasswordResetRateLimitedAspect(rateLimitService)

                        aspect.checkPasswordResetRateLimit(loginJoinPoint())

                        verify(exactly = 1) {
                            rateLimitService.tryConsume("student@gsm.hs.kr", PasswordResetRateLimitType.LOGIN)
                        }
                    }
                }

                context("로그인 요청의 한도를 모두 소진했을 때") {
                    it("429 예외를 던져야 한다") {
                        val rateLimitService = mockk<PasswordResetRateLimitService>()
                        every { rateLimitService.tryConsume(any(), any()) } returns
                            RateLimitConsumeResult(consumed = false, remainingTokens = 0L, secondsToWaitForRefill = 60L)
                        val aspect = PasswordResetRateLimitedAspect(rateLimitService)

                        val exception =
                            shouldThrow<ExpectedException> {
                                aspect.checkPasswordResetRateLimit(loginJoinPoint())
                            }

                        exception.statusCode shouldBe HttpStatus.TOO_MANY_REQUESTS
                    }
                }
            }
        }

        describe("자격증명을 검증하는 서비스는") {
            it("로그인 제출에 LOGIN 한도가 걸려 있어야 한다") {
                val annotation =
                    CompleteOauthAuthorizeFlowServiceImpl::class.java
                        .getMethod("execute", OauthAuthorizeSubmitReqDto::class.java)
                        .getAnnotation(PasswordResetRateLimited::class.java)

                annotation?.type shouldBe PasswordResetRateLimitType.LOGIN
            }

            it("정보 수정 항목 조회의 비밀번호 재검증에 CREDENTIAL_CHECK 한도가 걸려 있어야 한다") {
                val annotation =
                    QueryStudentDataEditRequestServiceImpl::class.java
                        .getMethod("execute", QueryStudentDataEditRequestReqDto::class.java)
                        .getAnnotation(PasswordResetRateLimited::class.java)

                annotation?.type shouldBe PasswordResetRateLimitType.CREDENTIAL_CHECK
            }
        }
    })
