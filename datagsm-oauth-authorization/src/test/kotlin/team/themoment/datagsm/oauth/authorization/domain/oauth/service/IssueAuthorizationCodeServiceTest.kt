package team.themoment.datagsm.oauth.authorization.domain.oauth.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldStartWith
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.springframework.http.HttpStatus
import team.themoment.datagsm.common.domain.oauth.entity.OauthCodeRedisEntity
import team.themoment.datagsm.common.domain.oauth.repository.OauthCodeRedisRepository
import team.themoment.datagsm.common.global.data.OauthEnvironment
import team.themoment.datagsm.common.global.dto.internal.RateLimitConsumeResult
import team.themoment.datagsm.oauth.authorization.domain.oauth.service.impl.IssueAuthorizationCodeServiceImpl
import team.themoment.datagsm.oauth.authorization.global.security.service.OAuthClientRateLimitService
import team.themoment.sdk.exception.ExpectedException
import java.net.URI

class IssueAuthorizationCodeServiceTest :
    DescribeSpec({

        val mockOauthCodeRedisRepository = mockk<OauthCodeRedisRepository>(relaxed = true)
        val mockOauthClientRateLimitService = mockk<OAuthClientRateLimitService>()
        val mockOauthEnvironment = mockk<OauthEnvironment>()

        val issueAuthorizationCodeService =
            IssueAuthorizationCodeServiceImpl(
                mockOauthCodeRedisRepository,
                mockOauthClientRateLimitService,
                mockOauthEnvironment,
            )

        afterEach {
            clearAllMocks()
        }

        describe("IssueAuthorizationCodeService 클래스의") {
            describe("execute 메서드는") {

                val testEmail = "user@gsm.hs.kr"
                val testClientId = "client-123"
                val testRedirectUri = "https://example.com/callback"
                val testScopes = setOf("datagsm:account_read")

                beforeEach {
                    every { mockOauthClientRateLimitService.tryConsumeAndReturnRemaining(any()) } returns
                        RateLimitConsumeResult(consumed = true, remainingTokens = 299, secondsToWaitForRefill = 0)
                    every { mockOauthEnvironment.codeExpirationSeconds } returns 300L
                    every { mockOauthCodeRedisRepository.save(any<OauthCodeRedisEntity>()) } answers { firstArg() }
                }

                fun issue(state: String?): String =
                    issueAuthorizationCodeService.execute(
                        email = testEmail,
                        clientId = testClientId,
                        redirectUri = testRedirectUri,
                        state = state,
                        codeChallenge = null,
                        codeChallengeMethod = null,
                        scopes = testScopes,
                    )

                context("state가 주어졌을 때") {
                    it("code와 state를 담은 리다이렉트 URL을 반환해야 한다") {
                        val redirectUrl = issue("random-state")

                        redirectUrl shouldStartWith "$testRedirectUri?code="
                        redirectUrl shouldContain "&state=random-state"
                    }
                }

                context("state에 URL 예약 문자가 포함됐을 때") {
                    it("파라미터가 주입되지 않도록 인코딩되어야 한다") {
                        val redirectUrl = issue("a&injected=evil")

                        redirectUrl shouldContain "state=a%26injected%3Devil"

                        // 인코딩되지 않으면 injected가 별도 쿼리 파라미터로 분리된다.
                        // rawQuery는 디코딩하지 않으므로 실제 전송되는 형태를 그대로 검증할 수 있다.
                        val parsedKeys =
                            URI
                                .create(redirectUrl)
                                .rawQuery
                                .split("&")
                                .map { it.substringBefore("=") }
                        parsedKeys.contains("injected") shouldBe false
                        parsedKeys shouldBe listOf("code", "state")
                    }

                    it("공백이 포함돼도 URL이 깨지지 않아야 한다") {
                        val redirectUrl = issue("with space")

                        redirectUrl shouldContain "state=with+space"
                    }
                }

                context("state가 없을 때") {
                    it("state 파라미터를 붙이지 않아야 한다") {
                        val redirectUrl = issue(null)

                        redirectUrl shouldStartWith "$testRedirectUri?code="
                        redirectUrl.contains("state=") shouldBe false
                    }
                }

                context("redirect_uri에 이미 쿼리가 있을 때") {
                    it("'&'로 이어 붙여야 한다") {
                        val redirectUrl =
                            issueAuthorizationCodeService.execute(
                                email = testEmail,
                                clientId = testClientId,
                                redirectUri = "https://example.com/callback?tenant=gsm",
                                state = null,
                                codeChallenge = null,
                                codeChallengeMethod = null,
                                scopes = testScopes,
                            )

                        redirectUrl shouldStartWith "https://example.com/callback?tenant=gsm&code="
                    }
                }

                context("클라이언트가 요청 한도를 초과했을 때") {
                    beforeEach {
                        every { mockOauthClientRateLimitService.tryConsumeAndReturnRemaining(any()) } returns
                            RateLimitConsumeResult(consumed = false, remainingTokens = 0, secondsToWaitForRefill = 30)
                    }

                    it("코드를 발급하지 않고 429가 반환되어야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                issue("random-state")
                            }

                        exception.statusCode shouldBe HttpStatus.TOO_MANY_REQUESTS
                        verify(exactly = 0) { mockOauthCodeRedisRepository.save(any<OauthCodeRedisEntity>()) }
                    }
                }

                context("코드를 발급할 때") {
                    it("Redis에 이메일과 scope가 저장되어야 한다") {
                        val codeSlot = slot<OauthCodeRedisEntity>()
                        every { mockOauthCodeRedisRepository.save(capture(codeSlot)) } answers { firstArg() }

                        val redirectUrl = issue("random-state")
                        val issuedCode =
                            URI
                                .create(redirectUrl)
                                .rawQuery
                                .substringAfter("code=")
                                .substringBefore("&")

                        codeSlot.captured.email shouldBe testEmail
                        codeSlot.captured.clientId shouldBe testClientId
                        codeSlot.captured.scopes shouldBe testScopes
                        codeSlot.captured.code shouldBe issuedCode
                        codeSlot.captured.ttl shouldBe 300L
                    }
                }
            }
        }
    })
