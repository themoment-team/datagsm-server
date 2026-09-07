package team.themoment.datagsm.oauth.authorization.domain.oauth.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import team.themoment.datagsm.common.domain.oauth.entity.IdpSessionHandoffRedisEntity
import team.themoment.datagsm.common.domain.oauth.exception.OAuthException
import team.themoment.datagsm.common.domain.oauth.repository.IdpSessionHandoffRedisRepository
import team.themoment.datagsm.common.global.data.OauthEnvironment
import team.themoment.datagsm.oauth.authorization.domain.oauth.service.impl.CompleteIdpSessionHandoffServiceImpl
import java.util.Optional

class CompleteIdpSessionHandoffServiceTest :
    DescribeSpec({

        val mockIdpSessionHandoffRedisRepository = mockk<IdpSessionHandoffRedisRepository>(relaxed = true)
        val mockOauthEnvironment = mockk<OauthEnvironment>()

        val completeIdpSessionHandoffService =
            CompleteIdpSessionHandoffServiceImpl(
                mockIdpSessionHandoffRedisRepository,
                mockOauthEnvironment,
            )

        afterEach {
            clearAllMocks()
        }

        describe("CompleteIdpSessionHandoffService 클래스의") {
            describe("execute 메서드는") {

                val testTicket = "test-ticket-123"
                val testSessionId = "session-abc"
                val testRedirectUrl = "https://example.com/callback?code=test-code&state=random-state"
                val cookieName = "datagsm_idp_session"
                val sessionTtl = 28800L

                val handoff =
                    IdpSessionHandoffRedisEntity(
                        ticket = testTicket,
                        sessionId = testSessionId,
                        redirectUrl = testRedirectUrl,
                        ttl = 60,
                    )

                beforeEach {
                    every { mockOauthEnvironment.idpSessionCookieName } returns cookieName
                    every { mockOauthEnvironment.idpSessionExpirationSeconds } returns sessionTtl
                    every { mockOauthEnvironment.idpSessionCookieSecure } returns true
                    every { mockOauthEnvironment.idpSessionCookieDomain } returns ".datagsm.kr"
                }

                context("유효한 티켓이 주어졌을 때") {
                    beforeEach {
                        every { mockIdpSessionHandoffRedisRepository.findById(testTicket) } returns Optional.of(handoff)
                    }

                    it("세션 쿠키를 설정하고 원래 목적지로 302 리다이렉트되어야 한다") {
                        val response = completeIdpSessionHandoffService.execute(testTicket)

                        response.statusCode shouldBe HttpStatus.FOUND
                        response.headers.location?.toString() shouldBe testRedirectUrl

                        val setCookie = response.headers.getFirst(HttpHeaders.SET_COOKIE) ?: ""
                        setCookie shouldContain "$cookieName=$testSessionId"
                        setCookie shouldContain "HttpOnly"
                        setCookie shouldContain "Secure"
                        setCookie shouldContain "SameSite=Lax"
                        setCookie shouldContain "Domain=.datagsm.kr"
                        setCookie shouldContain "Max-Age=$sessionTtl"
                    }

                    it("티켓은 일회용이므로 사용 즉시 삭제되어야 한다") {
                        completeIdpSessionHandoffService.execute(testTicket)

                        verify(exactly = 1) { mockIdpSessionHandoffRedisRepository.deleteById(testTicket) }
                    }
                }

                context("쿠키 도메인이 설정되지 않았을 때") {
                    beforeEach {
                        every { mockOauthEnvironment.idpSessionCookieDomain } returns null
                        every { mockIdpSessionHandoffRedisRepository.findById(testTicket) } returns Optional.of(handoff)
                    }

                    it("Domain 속성 없이 호스트 전용 쿠키가 설정되어야 한다") {
                        val response = completeIdpSessionHandoffService.execute(testTicket)

                        val setCookie = response.headers.getFirst(HttpHeaders.SET_COOKIE) ?: ""
                        setCookie shouldContain "$cookieName=$testSessionId"
                        setCookie.contains("Domain=") shouldBe false
                    }
                }

                context("유효하지 않거나 만료된 티켓이 주어졌을 때") {
                    beforeEach {
                        every { mockIdpSessionHandoffRedisRepository.findById(any()) } returns Optional.empty()
                    }

                    it("InvalidRequest 예외가 발생하고 티켓이 삭제되지 않아야 한다") {
                        val exception =
                            shouldThrow<OAuthException.InvalidRequest> {
                                completeIdpSessionHandoffService.execute("expired-ticket")
                            }

                        exception.error shouldBe "invalid_request"

                        verify(exactly = 0) { mockIdpSessionHandoffRedisRepository.deleteById(any()) }
                    }
                }
            }
        }
    })
