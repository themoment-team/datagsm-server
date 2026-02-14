package team.themoment.datagsm.oauth.authorization.domain.oauth.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.springframework.http.HttpStatus
import team.themoment.datagsm.common.domain.client.entity.ClientJpaEntity
import team.themoment.datagsm.common.domain.client.repository.ClientJpaRepository
import team.themoment.datagsm.common.domain.oauth.entity.OauthAuthorizeStateRedisEntity
import team.themoment.datagsm.common.domain.oauth.exception.OAuthException
import team.themoment.datagsm.common.domain.oauth.repository.OauthAuthorizeStateRedisRepository
import team.themoment.datagsm.common.global.data.OauthEnvironment
import team.themoment.datagsm.oauth.authorization.domain.oauth.service.impl.StartOauthAuthorizeFlowServiceImpl
import java.util.Optional

class StartOauthAuthorizeFlowServiceTest :
    DescribeSpec({

        val mockClientJpaRepository = mockk<ClientJpaRepository>()
        val mockOauthAuthorizeStateRedisRepository = mockk<OauthAuthorizeStateRedisRepository>(relaxed = true)
        val mockOauthEnvironment =
            mockk<OauthEnvironment> {
                every { frontendUrl } returns "http://localhost:3000"
            }

        val startOauthAuthorizeFlowService =
            StartOauthAuthorizeFlowServiceImpl(
                mockClientJpaRepository,
                mockOauthEnvironment,
                mockOauthAuthorizeStateRedisRepository,
            )

        afterEach {
            clearAllMocks()
        }

        describe("StartOauthAuthorizeFlowService 클래스의") {
            describe("execute 메서드는") {

                val testClientId = "client-123"
                val testRedirectUri = "https://example.com/callback"

                val mockClient =
                    ClientJpaEntity().apply {
                        id = testClientId
                        secret = "encodedSecret"
                        redirectUrls = setOf(testRedirectUri)
                        name = "Test Client"
                    }

                context("유효한 OAuth Authorize 요청이 주어졌을 때") {
                    val savedEntitySlot = slot<OauthAuthorizeStateRedisEntity>()

                    beforeEach {
                        every { mockClientJpaRepository.findById(testClientId) } returns Optional.of(mockClient)
                        every { mockOauthAuthorizeStateRedisRepository.save(capture(savedEntitySlot)) } answers { firstArg() }
                    }

                    it("Redis에 OAuth 파라미터가 저장되고 302 리다이렉트가 반환되어야 한다") {
                        val response =
                            startOauthAuthorizeFlowService.execute(
                                clientId = testClientId,
                                redirectUri = testRedirectUri,
                                responseType = "code",
                                state = "random-state",
                                codeChallenge = "challenge",
                                codeChallengeMethod = "S256",
                            )

                        response.statusCode shouldBe HttpStatus.FOUND
                        response.headers.location shouldNotBe null

                        val locationUrl = response.headers.location?.toString() ?: ""
                        locationUrl shouldContain "http://localhost:3000/oauth/authorize"
                        locationUrl shouldContain "token="

                        verify(exactly = 1) { mockOauthAuthorizeStateRedisRepository.save(any()) }

                        savedEntitySlot.captured.clientId shouldBe testClientId
                        savedEntitySlot.captured.redirectUri shouldBe testRedirectUri
                        savedEntitySlot.captured.state shouldBe "random-state"
                        savedEntitySlot.captured.codeChallenge shouldBe "challenge"
                        savedEntitySlot.captured.codeChallengeMethod shouldBe "S256"
                        savedEntitySlot.captured.ttl shouldBe 600
                    }
                }

                context("response_type이 code가 아닐 때") {
                    it("InvalidRequest 예외가 발생해야 한다") {
                        val exception =
                            shouldThrow<OAuthException.InvalidRequest> {
                                startOauthAuthorizeFlowService.execute(
                                    clientId = testClientId,
                                    redirectUri = testRedirectUri,
                                    responseType = "token",
                                    state = null,
                                    codeChallenge = null,
                                    codeChallengeMethod = null,
                                )
                            }

                        exception.error shouldBe "invalid_request"
                        exception.errorDescription shouldBe "response_type은 'code'여야 합니다."
                    }
                }

                context("존재하지 않는 client_id가 주어졌을 때") {
                    beforeEach {
                        every { mockClientJpaRepository.findById("invalid-client") } returns Optional.empty()
                    }

                    it("InvalidClient 예외가 발생해야 한다") {
                        val exception =
                            shouldThrow<OAuthException.InvalidClient> {
                                startOauthAuthorizeFlowService.execute(
                                    clientId = "invalid-client",
                                    redirectUri = testRedirectUri,
                                    responseType = "code",
                                    state = null,
                                    codeChallenge = null,
                                    codeChallengeMethod = null,
                                )
                            }

                        exception.error shouldBe "invalid_client"
                        exception.errorDescription shouldBe "존재하지 않는 클라이언트입니다."

                        verify(exactly = 0) { mockOauthAuthorizeStateRedisRepository.save(any()) }
                    }
                }

                context("등록되지 않은 redirect_uri가 주어졌을 때") {
                    beforeEach {
                        every { mockClientJpaRepository.findById(testClientId) } returns Optional.of(mockClient)
                    }

                    it("InvalidRequest 예외가 발생해야 한다") {
                        val exception =
                            shouldThrow<OAuthException.InvalidRequest> {
                                startOauthAuthorizeFlowService.execute(
                                    clientId = testClientId,
                                    redirectUri = "https://malicious.com/callback",
                                    responseType = "code",
                                    state = null,
                                    codeChallenge = null,
                                    codeChallengeMethod = null,
                                )
                            }

                        exception.errorDescription shouldBe "등록되지 않은 redirect_uri입니다."

                        verify(exactly = 0) { mockOauthAuthorizeStateRedisRepository.save(any()) }
                    }
                }

                context("지원하지 않는 code_challenge_method가 주어졌을 때") {
                    beforeEach {
                        every { mockClientJpaRepository.findById(testClientId) } returns Optional.of(mockClient)
                    }

                    it("InvalidRequest 예외가 발생해야 한다") {
                        val exception =
                            shouldThrow<OAuthException.InvalidRequest> {
                                startOauthAuthorizeFlowService.execute(
                                    clientId = testClientId,
                                    redirectUri = testRedirectUri,
                                    responseType = "code",
                                    state = null,
                                    codeChallenge = "challenge",
                                    codeChallengeMethod = "unsupported",
                                )
                            }

                        exception.errorDescription shouldBe "지원하지 않는 code_challenge_method입니다."

                        verify(exactly = 0) { mockOauthAuthorizeStateRedisRepository.save(any()) }
                    }
                }
            }
        }
    })
