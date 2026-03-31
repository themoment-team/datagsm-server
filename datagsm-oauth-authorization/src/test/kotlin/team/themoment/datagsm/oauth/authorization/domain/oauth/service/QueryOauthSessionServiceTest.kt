package team.themoment.datagsm.oauth.authorization.domain.oauth.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.longs.shouldBeGreaterThanOrEqual
import io.kotest.matchers.longs.shouldBeLessThanOrEqual
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import org.springframework.http.HttpStatus
import team.themoment.datagsm.common.domain.application.entity.ApplicationJpaEntity
import team.themoment.datagsm.common.domain.application.entity.OAuthScopeJpaEntity
import team.themoment.datagsm.common.domain.application.repository.OAuthScopeJpaRepository
import team.themoment.datagsm.common.domain.client.dto.response.OAuthScopeResDto
import team.themoment.datagsm.common.domain.client.entity.ClientJpaEntity
import team.themoment.datagsm.common.domain.client.repository.ClientJpaRepository
import team.themoment.datagsm.common.domain.oauth.entity.OauthAuthorizeStateRedisEntity
import team.themoment.datagsm.common.domain.oauth.repository.OauthAuthorizeStateRedisRepository
import team.themoment.datagsm.common.global.data.OauthEnvironment
import team.themoment.datagsm.oauth.authorization.domain.oauth.service.impl.QueryOauthSessionServiceImpl
import team.themoment.sdk.exception.ExpectedException
import java.time.Instant
import java.util.Optional

class QueryOauthSessionServiceTest :
    DescribeSpec({
        val mockOauthAuthorizeStateRedisRepository = mockk<OauthAuthorizeStateRedisRepository>()
        val mockClientJpaRepository = mockk<ClientJpaRepository>()
        val mockOauthEnvironment = mockk<OauthEnvironment>()
        val mockOauthScopeJpaRepository = mockk<OAuthScopeJpaRepository>()

        val queryOauthSessionService =
            QueryOauthSessionServiceImpl(
                mockOauthAuthorizeStateRedisRepository,
                mockClientJpaRepository,
                mockOauthEnvironment,
                mockOauthScopeJpaRepository,
            )

        afterEach {
            clearAllMocks()
        }

        describe("QueryOauthSessionService 클래스의") {
            describe("execute 메서드는") {

                val testToken = "valid-token-uuid"
                val testClientId = "client-123"

                val mockClient =
                    ClientJpaEntity().apply {
                        id = testClientId
                        secret = "encodedSecret"
                        redirectUrls = setOf("https://example.com/callback")
                        scopes = setOf("self:read")
                        clientName = "Test Client"
                        serviceName = "Test Service"
                    }

                context("단일 OAuthScope가 포함된 유효한 세션 토큰이 주어졌을 때") {
                    val mockStateEntity =
                        OauthAuthorizeStateRedisEntity(
                            token = testToken,
                            clientId = testClientId,
                            redirectUri = "https://example.com/callback",
                            state = null,
                            codeChallenge = null,
                            codeChallengeMethod = null,
                            scopes = setOf("self:read"),
                        )

                    val selfApplication =
                        ApplicationJpaEntity().apply {
                            id = "self"
                            name = "DataGSM"
                        }

                    val selfReadScope =
                        OAuthScopeJpaEntity().apply {
                            id = 1L
                            scopeName = "read"
                            description = "내 정보 조회"
                            application = selfApplication
                        }

                    beforeEach {
                        every { mockOauthAuthorizeStateRedisRepository.findById(testToken) } returns Optional.of(mockStateEntity)
                        every { mockClientJpaRepository.findById(testClientId) } returns Optional.of(mockClient)
                        every { mockOauthEnvironment.authorizeStateExpirationMs } returns 600000L
                        every { mockOauthScopeJpaRepository.findAllByApplicationIdIn(setOf("self")) } returns
                            listOf(selfReadScope)
                    }

                    it("클라이언트의 서비스 이름이 반환되어야 한다") {
                        val result = queryOauthSessionService.execute(testToken)

                        result.serviceName shouldBe "Test Service"
                    }

                    it("expiresAt이 현재 시각 + TTL 근방이어야 한다") {
                        val before = Instant.now().toEpochMilli() + 600000L
                        val result = queryOauthSessionService.execute(testToken)
                        val after = Instant.now().toEpochMilli() + 600000L

                        result.expiresAt shouldBeGreaterThanOrEqual before
                        result.expiresAt shouldBeLessThanOrEqual after
                    }

                    it("scope와 description이 포함된 requestedScopes가 반환되어야 한다") {
                        val result = queryOauthSessionService.execute(testToken)

                        result.requestedScopes shouldBe
                            listOf(
                                OAuthScopeResDto(
                                    scope = "self:read",
                                    description = "내 정보 조회",
                                    applicationName = "DataGSM",
                                ),
                            )
                    }
                }

                context("여러 Application의 OAuthScope가 포함된 유효한 세션 토큰이 주어졌을 때") {
                    val multiScopeStateEntity =
                        OauthAuthorizeStateRedisEntity(
                            token = testToken,
                            clientId = testClientId,
                            redirectUri = "https://example.com/callback",
                            state = null,
                            codeChallenge = null,
                            codeChallengeMethod = null,
                            scopes = setOf("self:read", "app-1:profile"),
                        )

                    val selfApplication =
                        ApplicationJpaEntity().apply {
                            id = "self"
                            name = "DataGSM"
                        }

                    val selfReadScope =
                        OAuthScopeJpaEntity().apply {
                            id = 1L
                            scopeName = "read"
                            description = "내 정보 조회"
                            application = selfApplication
                        }

                    val mockApplication =
                        ApplicationJpaEntity().apply {
                            id = "app-1"
                            name = "Test App"
                        }

                    val mockOAuthScopeEntity =
                        OAuthScopeJpaEntity().apply {
                            id = 2L
                            scopeName = "profile"
                            description = "프로필 정보 조회"
                            application = mockApplication
                        }

                    beforeEach {
                        every { mockOauthAuthorizeStateRedisRepository.findById(testToken) } returns Optional.of(multiScopeStateEntity)
                        every { mockClientJpaRepository.findById(testClientId) } returns Optional.of(mockClient)
                        every { mockOauthEnvironment.authorizeStateExpirationMs } returns 600000L
                        every { mockOauthScopeJpaRepository.findAllByApplicationIdIn(setOf("self", "app-1")) } returns
                            listOf(selfReadScope, mockOAuthScopeEntity)
                    }

                    it("모든 OAuthScope에 description과 applicationName이 포함되어 반환되어야 한다") {
                        val result = queryOauthSessionService.execute(testToken)

                        result.requestedScopes shouldContainExactlyInAnyOrder
                            listOf(
                                OAuthScopeResDto(
                                    scope = "self:read",
                                    description = "내 정보 조회",
                                    applicationName = "DataGSM",
                                ),
                                OAuthScopeResDto(
                                    scope = "app-1:profile",
                                    description = "프로필 정보 조회",
                                    applicationName = "Test App",
                                ),
                            )
                    }
                }

                context("Redis에 존재하지 않는 토큰이 주어졌을 때") {
                    beforeEach {
                        every { mockOauthAuthorizeStateRedisRepository.findById("expired-token") } returns Optional.empty()
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                queryOauthSessionService.execute("expired-token")
                            }

                        exception.message shouldBe "유효하지 않은 토큰입니다."
                        exception.statusCode shouldBe HttpStatus.UNAUTHORIZED
                    }
                }

                context("세션에 연결된 클라이언트가 DB에 존재하지 않을 때") {
                    val mockStateEntity =
                        OauthAuthorizeStateRedisEntity(
                            token = testToken,
                            clientId = testClientId,
                            redirectUri = "https://example.com/callback",
                            state = null,
                            codeChallenge = null,
                            codeChallengeMethod = null,
                            scopes = setOf("self:read"),
                        )

                    beforeEach {
                        every { mockOauthAuthorizeStateRedisRepository.findById(testToken) } returns Optional.of(mockStateEntity)
                        every { mockClientJpaRepository.findById(testClientId) } returns Optional.empty()
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                queryOauthSessionService.execute(testToken)
                            }

                        exception.message shouldBe "유효하지 않은 클라이언트입니다."
                        exception.statusCode shouldBe HttpStatus.UNAUTHORIZED
                    }
                }
            }
        }
    })
