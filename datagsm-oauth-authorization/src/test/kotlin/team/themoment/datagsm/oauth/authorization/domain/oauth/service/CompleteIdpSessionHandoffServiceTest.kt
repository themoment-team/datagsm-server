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
import team.themoment.datagsm.common.domain.oauth.repository.IdpSessionRedisRepository
import team.themoment.datagsm.common.global.data.OauthEnvironment
import team.themoment.datagsm.oauth.authorization.domain.oauth.service.impl.CompleteIdpSessionHandoffServiceImpl
import java.security.MessageDigest
import java.util.Optional

class CompleteIdpSessionHandoffServiceTest :
    DescribeSpec({

        val mockIdpSessionHandoffRedisRepository = mockk<IdpSessionHandoffRedisRepository>(relaxed = true)
        val mockIdpSessionRedisRepository = mockk<IdpSessionRedisRepository>(relaxed = true)
        val mockClientJpaRepository = mockk<ClientJpaRepository>()
        val mockOauthEnvironment = mockk<OauthEnvironment>()

        val completeIdpSessionHandoffService =
            CompleteIdpSessionHandoffServiceImpl(
                mockIdpSessionHandoffRedisRepository,
                mockIdpSessionRedisRepository,
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
                    every { mockOauthEnvironment.idpSessionHandoffRequireFetchMetadata } returns false
                    every { mockClientJpaRepository.findById(testClientId) } returns Optional.of(testClient)
                }

                context("유효한 티켓과 verifier가 주어졌을 때") {
                    beforeEach {
                        every { mockIdpSessionHandoffRedisRepository.findById(testTicket) } returns Optional.of(handoff)
                    }

                    it("세션 쿠키를 설정하고 원래 목적지로 302 리다이렉트되어야 한다") {
                        val response = completeIdpSessionHandoffService.execute(testTicket, testVerifier, "same-site", "navigate")

                        response.statusCode shouldBe HttpStatus.FOUND
                        response.headers.location?.toString() shouldBe testRedirectUrl

                        val setCookie = response.headers.getFirst(HttpHeaders.SET_COOKIE) ?: ""
                        setCookie shouldContain "$cookieName=$testSessionId"
                        setCookie shouldContain "HttpOnly"
                        setCookie shouldContain "Secure"
                        setCookie shouldContain "SameSite=Lax"
                        setCookie.contains("Domain=") shouldBe false
                        setCookie shouldContain "Max-Age=$sessionTtl"
                    }

                    it("티켓은 일회용이므로 사용 즉시 삭제되어야 한다") {
                        completeIdpSessionHandoffService.execute(testTicket, testVerifier, "same-site", "navigate")

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
                                completeIdpSessionHandoffService.execute(testTicket, "wrong-verifier", "same-site", "navigate")
                            }

                        exception.error shouldBe "invalid_request"
                    }

                    it("티켓을 삭제하지 않아 정상 사용자의 로그인이 무효화되지 않아야 한다") {
                        shouldThrow<OAuthException.InvalidRequest> {
                            completeIdpSessionHandoffService.execute(testTicket, "wrong-verifier", "same-site", "navigate")
                        }

                        // ticket만 아는 공격자가 요청 한 번으로 피해자 로그인을 깨뜨리는 것을 막는다.
                        verify(exactly = 0) { mockIdpSessionHandoffRedisRepository.deleteById(any()) }
                    }
                }

                context("최상위 내비게이션이 아닌 요청일 때") {
                    beforeEach {
                        every { mockIdpSessionHandoffRedisRepository.findById(testTicket) } returns Optional.of(handoff)
                    }

                    it("cross-site 요청은 거부되어야 한다") {
                        shouldThrow<OAuthException.InvalidRequest> {
                            completeIdpSessionHandoffService.execute(testTicket, testVerifier, "cross-site", "navigate")
                        }

                        verify(exactly = 0) { mockIdpSessionHandoffRedisRepository.deleteById(any()) }
                    }

                    it("navigate가 아닌 mode는 거부되어야 한다") {
                        shouldThrow<OAuthException.InvalidRequest> {
                            completeIdpSessionHandoffService.execute(testTicket, testVerifier, "same-site", "no-cors")
                        }
                    }

                    it("URL을 나중에 입수해 이미지로 불러오는 시도는 거부되어야 한다") {
                        shouldThrow<OAuthException.InvalidRequest> {
                            completeIdpSessionHandoffService.execute(testTicket, testVerifier, "cross-site", "no-cors")
                        }
                    }

                    it("주소창 직접 입력(none)은 정상 흐름이므로 허용되어야 한다") {
                        val response = completeIdpSessionHandoffService.execute(testTicket, testVerifier, "none", "navigate")

                        response.statusCode shouldBe HttpStatus.FOUND
                    }
                }

                context("Sec-Fetch 헤더를 보내지 않는 브라우저일 때") {
                    beforeEach {
                        every { mockIdpSessionHandoffRedisRepository.findById(testTicket) } returns Optional.of(handoff)
                    }

                    it("기본 설정에서는 정상 로그인을 막지 않아야 한다") {
                        val response = completeIdpSessionHandoffService.execute(testTicket, testVerifier, null, null)

                        response.statusCode shouldBe HttpStatus.FOUND
                    }

                    it("엄격 모드에서는 거부되어야 한다") {
                        every { mockOauthEnvironment.idpSessionHandoffRequireFetchMetadata } returns true

                        shouldThrow<OAuthException.InvalidRequest> {
                            completeIdpSessionHandoffService.execute(testTicket, testVerifier, null, null)
                        }
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
                            completeIdpSessionHandoffService.execute(testTicket, testVerifier, "same-site", "navigate")
                        }
                    }

                    it("쓰이지 못할 티켓과 세션이 함께 정리되어야 한다") {
                        shouldThrow<OAuthException.InvalidRequest> {
                            completeIdpSessionHandoffService.execute(testTicket, testVerifier, "same-site", "navigate")
                        }

                        // 검증 실패 시 세션이 남으면 8시간 동안 쓰이지 못한 채 Redis를 차지한다.
                        verify(exactly = 1) { mockIdpSessionHandoffRedisRepository.deleteById(testTicket) }
                        verify(exactly = 1) { mockIdpSessionRedisRepository.deleteById(testSessionId) }
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
                            completeIdpSessionHandoffService.execute(testTicket, testVerifier, "same-site", "navigate")
                        }
                    }
                }

                context("verifier 파라미터가 아예 빠졌을 때") {
                    beforeEach {
                        every { mockIdpSessionHandoffRedisRepository.findById(testTicket) } returns Optional.of(handoff)
                    }

                    it("스프링 기본 400이 아닌 InvalidRequest로 처리되어야 한다") {
                        val exception =
                            shouldThrow<OAuthException.InvalidRequest> {
                                completeIdpSessionHandoffService.execute(testTicket, null, "same-site", "navigate")
                            }

                        exception.error shouldBe "invalid_request"
                    }
                }

                context("유효하지 않거나 만료된 티켓이 주어졌을 때") {
                    beforeEach {
                        every { mockIdpSessionHandoffRedisRepository.findById(any()) } returns Optional.empty()
                    }

                    it("InvalidRequest 예외가 발생하고 티켓이 삭제되지 않아야 한다") {
                        val exception =
                            shouldThrow<OAuthException.InvalidRequest> {
                                completeIdpSessionHandoffService.execute("expired-ticket", testVerifier, "same-site", "navigate")
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
