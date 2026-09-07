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
import team.themoment.datagsm.common.domain.client.entity.ClientJpaEntity
import team.themoment.datagsm.common.domain.client.repository.ClientJpaRepository
import team.themoment.datagsm.common.domain.oauth.entity.IdpSessionHandoffRedisEntity
import team.themoment.datagsm.common.domain.oauth.exception.OAuthException
import team.themoment.datagsm.common.domain.oauth.repository.IdpSessionHandoffRedisRepository
import team.themoment.datagsm.common.global.data.OauthEnvironment
import team.themoment.datagsm.oauth.authorization.domain.oauth.service.impl.CompleteIdpSessionHandoffServiceImpl
import java.security.MessageDigest
import java.util.Optional

class CompleteIdpSessionHandoffServiceTest :
    DescribeSpec({

        val mockIdpSessionHandoffRedisRepository = mockk<IdpSessionHandoffRedisRepository>(relaxed = true)
        val mockClientJpaRepository = mockk<ClientJpaRepository>()
        val mockOauthEnvironment = mockk<OauthEnvironment>()

        val completeIdpSessionHandoffService =
            CompleteIdpSessionHandoffServiceImpl(
                mockIdpSessionHandoffRedisRepository,
                mockClientJpaRepository,
                mockOauthEnvironment,
            )

        afterEach {
            clearAllMocks()
        }

        describe("CompleteIdpSessionHandoffService 클래스의") {
            describe("execute 메서드는") {

                val testTicket = "test-ticket-123"
                val testVerifier = "test-verifier-456"
                val testSessionId = "session-abc"
                val testClientId = "client-123"
                val testRedirectUri = "https://example.com/callback"
                val testRedirectUrl = "$testRedirectUri?code=test-code&state=random-state"
                val cookieName = "datagsm_idp_session"
                val sessionTtl = 28800L

                val handoff =
                    IdpSessionHandoffRedisEntity(
                        ticket = testTicket,
                        verifierHash = sha256Hex(testVerifier),
                        sessionId = testSessionId,
                        clientId = testClientId,
                        redirectUrl = testRedirectUrl,
                        ttl = 60,
                    )

                val testClient =
                    ClientJpaEntity().apply {
                        id = testClientId
                        redirectUrls = setOf(testRedirectUri)
                    }

                beforeEach {
                    every { mockOauthEnvironment.idpSessionCookieName } returns cookieName
                    every { mockOauthEnvironment.idpSessionExpirationSeconds } returns sessionTtl
                    every { mockOauthEnvironment.idpSessionCookieSecure } returns true
                    every { mockOauthEnvironment.idpSessionCookieDomain } returns ".datagsm.kr"
                    every { mockClientJpaRepository.findById(testClientId) } returns Optional.of(testClient)
                }

                context("유효한 티켓과 verifier가 주어졌을 때") {
                    beforeEach {
                        every { mockIdpSessionHandoffRedisRepository.findById(testTicket) } returns Optional.of(handoff)
                    }

                    it("세션 쿠키를 설정하고 원래 목적지로 302 리다이렉트되어야 한다") {
                        val response = completeIdpSessionHandoffService.execute(testTicket, testVerifier)

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
                        completeIdpSessionHandoffService.execute(testTicket, testVerifier)

                        verify(exactly = 1) { mockIdpSessionHandoffRedisRepository.deleteById(testTicket) }
                    }
                }

                context("티켓은 맞지만 verifier가 틀렸을 때") {
                    beforeEach {
                        every { mockIdpSessionHandoffRedisRepository.findById(testTicket) } returns Optional.of(handoff)
                    }

                    it("세션을 발급하지 않고 InvalidRequest 예외가 발생해야 한다") {
                        val exception =
                            shouldThrow<OAuthException.InvalidRequest> {
                                completeIdpSessionHandoffService.execute(testTicket, "wrong-verifier")
                            }

                        exception.error shouldBe "invalid_request"
                    }

                    it("무차별 대입을 막기 위해 티켓이 폐기되어야 한다") {
                        shouldThrow<OAuthException.InvalidRequest> {
                            completeIdpSessionHandoffService.execute(testTicket, "wrong-verifier")
                        }

                        verify(exactly = 1) { mockIdpSessionHandoffRedisRepository.deleteById(testTicket) }
                    }
                }

                context("저장된 redirectUrl이 더 이상 허용되지 않을 때") {
                    beforeEach {
                        every { mockIdpSessionHandoffRedisRepository.findById(testTicket) } returns Optional.of(handoff)
                        every { mockClientJpaRepository.findById(testClientId) } returns
                            Optional.of(
                                ClientJpaEntity().apply {
                                    id = testClientId
                                    redirectUrls = setOf("https://another.example.com/callback")
                                },
                            )
                    }

                    it("오픈 리다이렉트를 막기 위해 예외가 발생해야 한다") {
                        shouldThrow<OAuthException.InvalidRequest> {
                            completeIdpSessionHandoffService.execute(testTicket, testVerifier)
                        }
                    }
                }

                context("등록된 URI의 접두사만 일치하는 위조 도메인일 때") {
                    val spoofedRedirectUrl = "https://example.com.attacker.io/callback?code=test-code"
                    val spoofedHandoff =
                        IdpSessionHandoffRedisEntity(
                            ticket = testTicket,
                            verifierHash = sha256Hex(testVerifier),
                            sessionId = testSessionId,
                            clientId = testClientId,
                            redirectUrl = spoofedRedirectUrl,
                            ttl = 60,
                        )

                    beforeEach {
                        every { mockIdpSessionHandoffRedisRepository.findById(testTicket) } returns
                            Optional.of(spoofedHandoff)
                        every { mockClientJpaRepository.findById(testClientId) } returns
                            Optional.of(
                                ClientJpaEntity().apply {
                                    id = testClientId
                                    redirectUrls = setOf("https://example.com")
                                },
                            )
                    }

                    it("접두사 일치만으로는 통과시키지 않아야 한다") {
                        shouldThrow<OAuthException.InvalidRequest> {
                            completeIdpSessionHandoffService.execute(testTicket, testVerifier)
                        }
                    }
                }

                context("쿠키 도메인이 설정되지 않았을 때") {
                    beforeEach {
                        every { mockOauthEnvironment.idpSessionCookieDomain } returns null
                        every { mockIdpSessionHandoffRedisRepository.findById(testTicket) } returns Optional.of(handoff)
                    }

                    it("Domain 속성 없이 호스트 전용 쿠키가 설정되어야 한다") {
                        val response = completeIdpSessionHandoffService.execute(testTicket, testVerifier)

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
                                completeIdpSessionHandoffService.execute("expired-ticket", testVerifier)
                            }

                        exception.error shouldBe "invalid_request"

                        verify(exactly = 0) { mockIdpSessionHandoffRedisRepository.deleteById(any()) }
                    }
                }
            }
        }
    })

private fun sha256Hex(value: String): String =
    MessageDigest
        .getInstance("SHA-256")
        .digest(value.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }
