package team.themoment.datagsm.authorization.domain.oauth.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import team.themoment.datagsm.authorization.domain.oauth.service.impl.ExchangeTokenServiceImpl
import team.themoment.datagsm.authorization.global.security.jwt.JwtProvider
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.common.domain.account.entity.constant.ApiScope
import team.themoment.datagsm.common.domain.account.repository.AccountJpaRepository
import team.themoment.datagsm.common.domain.client.entity.ClientJpaEntity
import team.themoment.datagsm.common.domain.client.repository.ClientJpaRepository
import team.themoment.datagsm.common.domain.oauth.dto.request.OauthTokenReqDto
import team.themoment.datagsm.common.domain.oauth.entity.OauthCodeRedisEntity
import team.themoment.datagsm.common.domain.oauth.entity.OauthRefreshTokenRedisEntity
import team.themoment.datagsm.common.domain.oauth.repository.OauthCodeRedisRepository
import team.themoment.datagsm.common.domain.oauth.repository.OauthRefreshTokenRedisRepository
import team.themoment.datagsm.common.global.data.OauthJwtEnvironment
import team.themoment.sdk.exception.ExpectedException
import java.util.Optional

class ExchangeTokenServiceTest :
    DescribeSpec({

        val mockAccountJpaRepository = mockk<AccountJpaRepository>()
        val mockClientJpaRepository = mockk<ClientJpaRepository>()
        val mockPasswordEncoder = mockk<PasswordEncoder>()
        val mockOauthCodeRedisRepository = mockk<OauthCodeRedisRepository>()
        val mockJwtProvider = mockk<JwtProvider>()
        val mockJwtEnvironment = mockk<OauthJwtEnvironment>()
        val mockOauthRefreshTokenRedisRepository = mockk<OauthRefreshTokenRedisRepository>()

        val exchangeTokenService =
            ExchangeTokenServiceImpl(
                mockAccountJpaRepository,
                mockClientJpaRepository,
                mockPasswordEncoder,
                mockOauthCodeRedisRepository,
                mockJwtProvider,
                mockJwtEnvironment,
                mockOauthRefreshTokenRedisRepository,
            )

        afterEach {
            clearAllMocks()
        }

        describe("ExchangeTokenService 클래스의") {
            describe("execute 메서드는") {

                val testCode = "test-oauth-code"
                val testClientSecret = "client-secret-123"
                val testClientId = "client-123"
                val testEmail = "test@gsm.hs.kr"
                val testAccessToken = "oauth.access.token"
                val testRefreshToken = "oauth.refresh.token"
                val oauthRefreshTokenExpiration = 604800000L

                val mockOauthCode =
                    OauthCodeRedisEntity(
                        email = testEmail,
                        clientId = testClientId,
                        code = testCode,
                        ttl = 300L,
                    )

                val testScopes = setOf(ApiScope.STUDENT_READ, ApiScope.CLUB_READ)

                val mockClient =
                    ClientJpaEntity().apply {
                        id = testClientId
                        secret = "encodedSecret"
                        redirectUrls = setOf("https://example.com/callback")
                        name = "Test Client"
                        scopes = testScopes
                    }

                val mockAccount =
                    AccountJpaEntity().apply {
                        id = 1L
                        email = testEmail
                        password = "encodedPassword"
                        role = AccountRole.USER
                    }

                beforeEach {
                    every { mockJwtEnvironment.oauthRefreshTokenExpiration } returns oauthRefreshTokenExpiration
                }

                context("존재하지 않거나 만료된 코드로 요청할 때") {
                    val reqDto =
                        OauthTokenReqDto(
                            code = "invalid-code",
                            clientSecret = testClientSecret,
                        )

                    beforeEach {
                        every { mockOauthCodeRedisRepository.findById("invalid-code") } returns Optional.empty()
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                exchangeTokenService.execute(reqDto)
                            }

                        exception.message shouldBe "존재하지 않거나 만료된 코드입니다."
                        exception.statusCode shouldBe HttpStatus.NOT_FOUND

                        verify(exactly = 1) { mockOauthCodeRedisRepository.findById("invalid-code") }
                        verify(exactly = 0) { mockClientJpaRepository.findById(any()) }
                    }
                }

                context("존재하지 않는 Client로 요청할 때") {
                    val reqDto =
                        OauthTokenReqDto(
                            code = testCode,
                            clientSecret = testClientSecret,
                        )

                    beforeEach {
                        every { mockOauthCodeRedisRepository.findById(testCode) } returns Optional.of(mockOauthCode)
                        every { mockClientJpaRepository.findById(testClientId) } returns Optional.empty()
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                exchangeTokenService.execute(reqDto)
                            }

                        exception.message shouldBe "인증하려는 Client가 존재하지 않습니다."
                        exception.statusCode shouldBe HttpStatus.NOT_FOUND

                        verify(exactly = 1) { mockOauthCodeRedisRepository.findById(testCode) }
                        verify(exactly = 1) { mockClientJpaRepository.findById(testClientId) }
                    }
                }

                context("Client Secret이 일치하지 않을 때") {
                    val reqDto =
                        OauthTokenReqDto(
                            code = testCode,
                            clientSecret = "wrong-secret",
                        )

                    beforeEach {
                        every { mockOauthCodeRedisRepository.findById(testCode) } returns Optional.of(mockOauthCode)
                        every { mockClientJpaRepository.findById(testClientId) } returns Optional.of(mockClient)
                        every { mockPasswordEncoder.matches("wrong-secret", mockClient.secret) } returns false
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                exchangeTokenService.execute(reqDto)
                            }

                        exception.message shouldBe "Client Secret이 일치하지 않습니다."
                        exception.statusCode shouldBe HttpStatus.BAD_REQUEST

                        verify(exactly = 1) { mockPasswordEncoder.matches("wrong-secret", mockClient.secret) }
                        verify(exactly = 0) { mockAccountJpaRepository.findByEmail(any()) }
                    }
                }

                context("코드에 해당하는 사용자가 존재하지 않을 때") {
                    val reqDto =
                        OauthTokenReqDto(
                            code = testCode,
                            clientSecret = testClientSecret,
                        )

                    beforeEach {
                        every { mockOauthCodeRedisRepository.findById(testCode) } returns Optional.of(mockOauthCode)
                        every { mockClientJpaRepository.findById(testClientId) } returns Optional.of(mockClient)
                        every { mockPasswordEncoder.matches(testClientSecret, mockClient.secret) } returns true
                        every { mockAccountJpaRepository.findByEmail(testEmail) } returns Optional.empty()
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                exchangeTokenService.execute(reqDto)
                            }

                        exception.message shouldBe "코드에 해당하는 사용자가 존재하지 않습니다."
                        exception.statusCode shouldBe HttpStatus.NOT_FOUND

                        verify(exactly = 1) { mockAccountJpaRepository.findByEmail(testEmail) }
                    }
                }

                context("모든 검증을 통과할 때") {
                    val reqDto =
                        OauthTokenReqDto(
                            code = testCode,
                            clientSecret = testClientSecret,
                        )

                    val savedRefreshTokenSlot = slot<OauthRefreshTokenRedisEntity>()

                    beforeEach {
                        every { mockOauthCodeRedisRepository.findById(testCode) } returns Optional.of(mockOauthCode)
                        every { mockClientJpaRepository.findById(testClientId) } returns Optional.of(mockClient)
                        every { mockPasswordEncoder.matches(testClientSecret, mockClient.secret) } returns true
                        every { mockAccountJpaRepository.findByEmail(testEmail) } returns Optional.of(mockAccount)
                        every { mockOauthCodeRedisRepository.delete(mockOauthCode) } returns Unit
                        every {
                            mockJwtProvider.generateOauthAccessToken(
                                testEmail,
                                mockAccount.role,
                                testClientId,
                                testScopes,
                            )
                        } returns testAccessToken
                        every { mockJwtProvider.generateOauthRefreshToken(testEmail, testClientId) } returns testRefreshToken
                        every {
                            mockOauthRefreshTokenRedisRepository.deleteByEmailAndClientId(
                                testEmail,
                                testClientId,
                            )
                        } returns Unit
                        every {
                            mockOauthRefreshTokenRedisRepository.save(capture(savedRefreshTokenSlot))
                        } answers { firstArg() }
                    }

                    it("토큰을 발급하고 반환해야 한다") {
                        val result = exchangeTokenService.execute(reqDto)

                        result.accessToken shouldBe testAccessToken
                        result.refreshToken shouldBe testRefreshToken

                        verify(exactly = 1) { mockOauthCodeRedisRepository.delete(mockOauthCode) }
                        verify(exactly = 1) {
                            mockJwtProvider.generateOauthAccessToken(
                                testEmail,
                                mockAccount.role,
                                testClientId,
                                testScopes,
                            )
                        }
                        verify(exactly = 1) { mockJwtProvider.generateOauthRefreshToken(testEmail, testClientId) }
                        verify(exactly = 1) {
                            mockOauthRefreshTokenRedisRepository.deleteByEmailAndClientId(
                                testEmail,
                                testClientId,
                            )
                        }
                        verify(exactly = 1) { mockOauthRefreshTokenRedisRepository.save(any()) }

                        savedRefreshTokenSlot.captured.email shouldBe testEmail
                        savedRefreshTokenSlot.captured.clientId shouldBe testClientId
                        savedRefreshTokenSlot.captured.token shouldBe testRefreshToken
                        savedRefreshTokenSlot.captured.ttl shouldBe oauthRefreshTokenExpiration / 1000
                    }
                }
            }
        }
    })
