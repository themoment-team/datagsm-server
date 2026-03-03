package team.themoment.datagsm.oauth.authorization.domain.oauth.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import team.themoment.datagsm.common.domain.client.entity.ClientJpaEntity
import team.themoment.datagsm.common.domain.client.repository.ClientJpaRepository
import team.themoment.datagsm.common.domain.oauth.entity.OauthAuthorizeStateRedisEntity
import team.themoment.datagsm.common.domain.oauth.exception.OAuthException
import team.themoment.datagsm.common.domain.oauth.repository.OauthAuthorizeStateRedisRepository
import team.themoment.datagsm.oauth.authorization.domain.oauth.service.impl.QueryOauthSessionServiceImpl
import java.util.Optional

class QueryOauthSessionServiceTest :
    DescribeSpec({
        val mockOauthAuthorizeStateRedisRepository = mockk<OauthAuthorizeStateRedisRepository>()
        val mockClientJpaRepository = mockk<ClientJpaRepository>()

        val queryOauthSessionService =
            QueryOauthSessionServiceImpl(
                mockOauthAuthorizeStateRedisRepository,
                mockClientJpaRepository,
            )

        afterEach {
            clearAllMocks()
        }

        describe("QueryOauthSessionService 클래스의") {
            describe("execute 메서드는") {

                val testToken = "valid-token-uuid"
                val testClientId = "client-123"

                val mockStateEntity =
                    OauthAuthorizeStateRedisEntity(
                        token = testToken,
                        clientId = testClientId,
                        redirectUri = "https://example.com/callback",
                        state = null,
                        codeChallenge = null,
                        codeChallengeMethod = null,
                    )

                val mockClient =
                    ClientJpaEntity().apply {
                        id = testClientId
                        secret = "encodedSecret"
                        redirectUrls = setOf("https://example.com/callback")
                        scopes = setOf("self:read")
                        clientName = "Test Client"
                        serviceName = "Test Service"
                    }

                context("유효한 세션 토큰이 주어졌을 때") {
                    beforeEach {
                        every { mockOauthAuthorizeStateRedisRepository.findById(testToken) } returns Optional.of(mockStateEntity)
                        every { mockClientJpaRepository.findById(testClientId) } returns Optional.of(mockClient)
                    }

                    it("클라이언트의 서비스 이름이 반환되어야 한다") {
                        val result = queryOauthSessionService.execute(testToken)

                        result.serviceName shouldBe "Test Service"
                    }
                }

                context("Redis에 존재하지 않는 토큰이 주어졌을 때") {
                    beforeEach {
                        every { mockOauthAuthorizeStateRedisRepository.findById("expired-token") } returns Optional.empty()
                    }

                    it("InvalidRequest 예외가 발생해야 한다") {
                        val exception =
                            shouldThrow<OAuthException.InvalidRequest> {
                                queryOauthSessionService.execute("expired-token")
                            }

                        exception.error shouldBe "invalid_request"
                        exception.errorDescription shouldBe "유효하지 않거나 만료된 세션입니다."
                    }
                }

                context("세션에 연결된 클라이언트가 DB에 존재하지 않을 때") {
                    beforeEach {
                        every { mockOauthAuthorizeStateRedisRepository.findById(testToken) } returns Optional.of(mockStateEntity)
                        every { mockClientJpaRepository.findById(testClientId) } returns Optional.empty()
                    }

                    it("InvalidClient 예외가 발생해야 한다") {
                        val exception =
                            shouldThrow<OAuthException.InvalidClient> {
                                queryOauthSessionService.execute(testToken)
                            }

                        exception.error shouldBe "invalid_client"
                        exception.errorDescription shouldBe "존재하지 않는 클라이언트입니다."
                    }
                }
            }
        }
    })
