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
import team.themoment.datagsm.common.domain.oauth.dto.request.OauthAuthorizeReqDto
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
                every { authorizeStateExpirationMs } returns 600000L
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
                        scopes = setOf("self:read")
                        clientName = "Test Client"
                        serviceName = "Test Service"
                    }

                context("유효한 OAuth Authorize 요청이 주어졌을 때") {
                    val savedEntitySlot = slot<OauthAuthorizeStateRedisEntity>()

                    beforeEach {
                        every { mockClientJpaRepository.findById(testClientId) } returns Optional.of(mockClient)
                        every { mockOauthAuthorizeStateRedisRepository.save(capture(savedEntitySlot)) } answers { firstArg() }
                    }

                    it("Redis에 OAuth 파라미터가 저장되고 302 리다이렉트가 반환되어야 한다") {
                        val reqDto =
                            OauthAuthorizeReqDto(
                                client_id = testClientId,
                                redirect_uri = testRedirectUri,
                                response_type = "code",
                                state = "random-state",
                                code_challenge = "challenge",
                                code_challenge_method = "S256",
                            )
                        val response = startOauthAuthorizeFlowService.execute(reqDto)

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
                        savedEntitySlot.captured.scopes shouldBe setOf("self:read")
                    }
                }

                context("scope 파라미터가 null일 때") {
                    val savedEntitySlot = slot<OauthAuthorizeStateRedisEntity>()

                    beforeEach {
                        every { mockOauthEnvironment.frontendUrl } returns "http://localhost:3000"
                        every { mockOauthEnvironment.authorizeStateExpirationMs } returns 600000L
                        every { mockClientJpaRepository.findById(testClientId) } returns Optional.of(mockClient)
                        every { mockOauthAuthorizeStateRedisRepository.save(capture(savedEntitySlot)) } answers { firstArg() }
                    }

                    it("client의 전체 scope가 state entity에 저장되어야 한다") {
                        val reqDto =
                            OauthAuthorizeReqDto(
                                client_id = testClientId,
                                redirect_uri = testRedirectUri,
                                response_type = "code",
                            )
                        startOauthAuthorizeFlowService.execute(reqDto)

                        savedEntitySlot.captured.scopes shouldBe setOf("self:read")
                    }
                }

                context("허용된 scope를 요청할 때") {
                    val savedEntitySlot = slot<OauthAuthorizeStateRedisEntity>()

                    beforeEach {
                        every { mockOauthEnvironment.frontendUrl } returns "http://localhost:3000"
                        every { mockOauthEnvironment.authorizeStateExpirationMs } returns 600000L
                        every { mockClientJpaRepository.findById(testClientId) } returns Optional.of(mockClient)
                        every { mockOauthAuthorizeStateRedisRepository.save(capture(savedEntitySlot)) } answers { firstArg() }
                    }

                    it("요청한 scope가 state entity에 저장되어야 한다") {
                        val reqDto =
                            OauthAuthorizeReqDto(
                                client_id = testClientId,
                                redirect_uri = testRedirectUri,
                                response_type = "code",
                                scope = "self:read",
                            )
                        startOauthAuthorizeFlowService.execute(reqDto)

                        savedEntitySlot.captured.scopes shouldBe setOf("self:read")
                    }
                }

                context("허용된 여러 scope를 공백으로 구분하여 요청할 때") {
                    val multiScopeClient =
                        ClientJpaEntity().apply {
                            id = testClientId
                            secret = "encodedSecret"
                            redirectUrls = setOf(testRedirectUri)
                            scopes = setOf("self:read", "profile:read")
                            clientName = "Test Client"
                            serviceName = "Test Service"
                        }
                    val savedEntitySlot = slot<OauthAuthorizeStateRedisEntity>()

                    beforeEach {
                        every { mockOauthEnvironment.frontendUrl } returns "http://localhost:3000"
                        every { mockOauthEnvironment.authorizeStateExpirationMs } returns 600000L
                        every { mockClientJpaRepository.findById(testClientId) } returns Optional.of(multiScopeClient)
                        every { mockOauthAuthorizeStateRedisRepository.save(capture(savedEntitySlot)) } answers { firstArg() }
                    }

                    it("요청한 모든 scope가 state entity에 저장되어야 한다") {
                        val reqDto =
                            OauthAuthorizeReqDto(
                                client_id = testClientId,
                                redirect_uri = testRedirectUri,
                                response_type = "code",
                                scope = "self:read profile:read",
                            )
                        startOauthAuthorizeFlowService.execute(reqDto)

                        savedEntitySlot.captured.scopes shouldBe setOf("self:read", "profile:read")
                    }
                }

                context("허용되지 않은 scope를 요청할 때") {
                    beforeEach {
                        every { mockClientJpaRepository.findById(testClientId) } returns Optional.of(mockClient)
                    }

                    it("InvalidScope 예외가 발생하고 Redis에 저장되지 않아야 한다") {
                        val reqDto =
                            OauthAuthorizeReqDto(
                                client_id = testClientId,
                                redirect_uri = testRedirectUri,
                                response_type = "code",
                                scope = "admin:write",
                            )
                        val exception =
                            shouldThrow<OAuthException.InvalidScope> {
                                startOauthAuthorizeFlowService.execute(reqDto)
                            }

                        exception.error shouldBe "invalid_scope"

                        verify(exactly = 0) { mockOauthAuthorizeStateRedisRepository.save(any()) }
                    }
                }

                context("response_type이 code가 아닐 때") {
                    it("InvalidRequest 예외가 발생해야 한다") {
                        val reqDto =
                            OauthAuthorizeReqDto(
                                client_id = testClientId,
                                redirect_uri = testRedirectUri,
                                response_type = "token",
                                state = null,
                                code_challenge = null,
                                code_challenge_method = null,
                            )
                        val exception =
                            shouldThrow<OAuthException.InvalidRequest> {
                                startOauthAuthorizeFlowService.execute(reqDto)
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
                        val reqDto =
                            OauthAuthorizeReqDto(
                                client_id = "invalid-client",
                                redirect_uri = testRedirectUri,
                                response_type = "code",
                                state = null,
                                code_challenge = null,
                                code_challenge_method = null,
                            )
                        val exception =
                            shouldThrow<OAuthException.InvalidClient> {
                                startOauthAuthorizeFlowService.execute(reqDto)
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
                        val reqDto =
                            OauthAuthorizeReqDto(
                                client_id = testClientId,
                                redirect_uri = "https://malicious.com/callback",
                                response_type = "code",
                                state = null,
                                code_challenge = null,
                                code_challenge_method = null,
                            )
                        val exception =
                            shouldThrow<OAuthException.InvalidRequest> {
                                startOauthAuthorizeFlowService.execute(reqDto)
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
                        val reqDto =
                            OauthAuthorizeReqDto(
                                client_id = testClientId,
                                redirect_uri = testRedirectUri,
                                response_type = "code",
                                state = null,
                                code_challenge = "challenge",
                                code_challenge_method = "unsupported",
                            )
                        val exception =
                            shouldThrow<OAuthException.InvalidRequest> {
                                startOauthAuthorizeFlowService.execute(reqDto)
                            }

                        exception.errorDescription shouldBe "지원하지 않는 code_challenge_method입니다."

                        verify(exactly = 0) { mockOauthAuthorizeStateRedisRepository.save(any()) }
                    }
                }
            }
        }
    })
