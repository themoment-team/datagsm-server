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
import team.themoment.datagsm.authorization.domain.oauth.service.impl.ReissueOauthTokenServiceImpl
import team.themoment.datagsm.common.global.data.JwtProperties
import team.themoment.datagsm.authorization.global.security.jwt.JwtProvider
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.common.domain.account.entity.constant.ApiScope
import team.themoment.datagsm.common.domain.oauth.entity.OauthRefreshTokenRedisEntity
import team.themoment.datagsm.common.domain.account.repository.AccountJpaRepository
import team.themoment.datagsm.common.domain.client.entity.ClientJpaEntity
import team.themoment.datagsm.common.domain.client.repository.ClientJpaRepository
import team.themoment.datagsm.common.domain.oauth.repository.OauthRefreshTokenRedisRepository
import team.themoment.sdk.exception.ExpectedException
import java.util.Optional

class ReissueOauthTokenServiceTest :
    DescribeSpec({

        val mockJwtProvider = mockk<JwtProvider>()
        val mockJwtProperties = mockk<JwtProperties>()
        val mockOauthRefreshTokenRedisRepository = mockk<OauthRefreshTokenRedisRepository>()
        val mockAccountJpaRepository = mockk<AccountJpaRepository>()
        val mockClientJpaRepository = mockk<ClientJpaRepository>()

        val reissueOauthTokenService =
            ReissueOauthTokenServiceImpl(
                mockJwtProvider,
                mockJwtProperties,
                mockOauthRefreshTokenRedisRepository,
                mockAccountJpaRepository,
                mockClientJpaRepository,
            )

        afterEach {
            clearAllMocks()
        }

        describe("ReissueOauthTokenService 클래스의") {
            describe("execute 메서드는") {

                val testEmail = "test@gsm.hs.kr"
                val testClientId = "client-123"
                val testRefreshToken = "valid.refresh.token"
                val newAccessToken = "new.access.token"
                val newRefreshToken = "new.refresh.token"
                val oauthRefreshTokenExpiration = 604800000L

                val mockAccount =
                    AccountJpaEntity().apply {
                        id = 1L
                        email = testEmail
                        password = "encodedPassword"
                        role = AccountRole.USER
                    }

                val testScopes = setOf(ApiScope.STUDENT_READ, ApiScope.CLUB_READ)

                val mockClient =
                    ClientJpaEntity().apply {
                        id = testClientId
                        secret = "encodedSecret"
                        redirectUrls = setOf("https://example.com/callback")
                        name = "Test Client"
                        scopes = testScopes
                    }

                val mockStoredRefreshToken =
                    OauthRefreshTokenRedisEntity.of(
                        email = testEmail,
                        clientId = testClientId,
                        token = testRefreshToken,
                        ttl = oauthRefreshTokenExpiration / 1000,
                    )

                beforeEach {
                    every { mockJwtProperties.oauthRefreshTokenExpiration } returns oauthRefreshTokenExpiration
                }

                context("유효하지 않은 refresh token으로 요청할 때") {
                    val invalidToken = "invalid.refresh.token"

                    beforeEach {
                        every { mockJwtProvider.validateToken(invalidToken) } returns false
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                reissueOauthTokenService.execute(invalidToken)
                            }

                        exception.message shouldBe "유효하지 않은 refresh token입니다."
                        exception.statusCode shouldBe HttpStatus.UNAUTHORIZED

                        verify(exactly = 1) { mockJwtProvider.validateToken(invalidToken) }
                        verify(exactly = 0) { mockJwtProvider.getEmailFromToken(any()) }
                    }
                }

                context("refresh token에 clientId가 없을 때") {
                    beforeEach {
                        every { mockJwtProvider.validateToken(testRefreshToken) } returns true
                        every { mockJwtProvider.getEmailFromToken(testRefreshToken) } returns testEmail
                        every { mockJwtProvider.getClientIdFromToken(testRefreshToken) } returns null
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                reissueOauthTokenService.execute(testRefreshToken)
                            }

                        exception.message shouldBe "Refresh token에 clientId가 없습니다."
                        exception.statusCode shouldBe HttpStatus.UNAUTHORIZED

                        verify(exactly = 1) { mockJwtProvider.getClientIdFromToken(testRefreshToken) }
                    }
                }

                context("저장된 refresh token을 찾을 수 없을 때") {
                    beforeEach {
                        every { mockJwtProvider.validateToken(testRefreshToken) } returns true
                        every { mockJwtProvider.getEmailFromToken(testRefreshToken) } returns testEmail
                        every { mockJwtProvider.getClientIdFromToken(testRefreshToken) } returns testClientId
                        every {
                            mockOauthRefreshTokenRedisRepository.findByEmailAndClientId(
                                testEmail,
                                testClientId,
                            )
                        } returns Optional.empty()
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                reissueOauthTokenService.execute(testRefreshToken)
                            }

                        exception.message shouldBe "저장된 refresh token을 찾을 수 없습니다."
                        exception.statusCode shouldBe HttpStatus.UNAUTHORIZED

                        verify(exactly = 1) {
                            mockOauthRefreshTokenRedisRepository.findByEmailAndClientId(
                                testEmail,
                                testClientId,
                            )
                        }
                    }
                }

                context("refresh token이 저장된 토큰과 일치하지 않을 때") {
                    val differentToken = "different.refresh.token"

                    beforeEach {
                        every { mockJwtProvider.validateToken(differentToken) } returns true
                        every { mockJwtProvider.getEmailFromToken(differentToken) } returns testEmail
                        every { mockJwtProvider.getClientIdFromToken(differentToken) } returns testClientId
                        every {
                            mockOauthRefreshTokenRedisRepository.findByEmailAndClientId(
                                testEmail,
                                testClientId,
                            )
                        } returns Optional.of(mockStoredRefreshToken)
                        every {
                            mockOauthRefreshTokenRedisRepository.deleteByEmailAndClientId(
                                testEmail,
                                testClientId,
                            )
                        } returns Unit
                    }

                    it("ExpectedException이 발생하고 저장된 토큰이 삭제되어야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                reissueOauthTokenService.execute(differentToken)
                            }

                        exception.message shouldBe "Refresh token이 일치하지 않습니다. 재로그인이 필요합니다."
                        exception.statusCode shouldBe HttpStatus.UNAUTHORIZED

                        verify(exactly = 1) {
                            mockOauthRefreshTokenRedisRepository.deleteByEmailAndClientId(
                                testEmail,
                                testClientId,
                            )
                        }
                    }
                }

                context("계정을 찾을 수 없을 때") {
                    beforeEach {
                        every { mockJwtProvider.validateToken(testRefreshToken) } returns true
                        every { mockJwtProvider.getEmailFromToken(testRefreshToken) } returns testEmail
                        every { mockJwtProvider.getClientIdFromToken(testRefreshToken) } returns testClientId
                        every {
                            mockOauthRefreshTokenRedisRepository.findByEmailAndClientId(
                                testEmail,
                                testClientId,
                            )
                        } returns Optional.of(mockStoredRefreshToken)
                        every { mockAccountJpaRepository.findByEmail(testEmail) } returns Optional.empty()
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                reissueOauthTokenService.execute(testRefreshToken)
                            }

                        exception.message shouldBe "계정을 찾을 수 없습니다."
                        exception.statusCode shouldBe HttpStatus.NOT_FOUND

                        verify(exactly = 1) { mockAccountJpaRepository.findByEmail(testEmail) }
                    }
                }

                context("Oauth 클라이언트를 찾을 수 없을 때") {
                    beforeEach {
                        every { mockJwtProvider.validateToken(testRefreshToken) } returns true
                        every { mockJwtProvider.getEmailFromToken(testRefreshToken) } returns testEmail
                        every { mockJwtProvider.getClientIdFromToken(testRefreshToken) } returns testClientId
                        every {
                            mockOauthRefreshTokenRedisRepository.findByEmailAndClientId(
                                testEmail,
                                testClientId,
                            )
                        } returns Optional.of(mockStoredRefreshToken)
                        every { mockAccountJpaRepository.findByEmail(testEmail) } returns Optional.of(mockAccount)
                        every { mockClientJpaRepository.findById(testClientId) } returns Optional.empty()
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                reissueOauthTokenService.execute(testRefreshToken)
                            }

                        exception.message shouldBe "Oauth 클라이언트를 찾을 수 없습니다."
                        exception.statusCode shouldBe HttpStatus.NOT_FOUND

                        verify(exactly = 1) { mockClientJpaRepository.findById(testClientId) }
                    }
                }

                context("모든 검증을 통과할 때") {
                    val savedRefreshTokenSlot = slot<OauthRefreshTokenRedisEntity>()

                    beforeEach {
                        every { mockJwtProvider.validateToken(testRefreshToken) } returns true
                        every { mockJwtProvider.getEmailFromToken(testRefreshToken) } returns testEmail
                        every { mockJwtProvider.getClientIdFromToken(testRefreshToken) } returns testClientId
                        every {
                            mockOauthRefreshTokenRedisRepository.findByEmailAndClientId(
                                testEmail,
                                testClientId,
                            )
                        } returns Optional.of(mockStoredRefreshToken)
                        every { mockAccountJpaRepository.findByEmail(testEmail) } returns Optional.of(mockAccount)
                        every { mockClientJpaRepository.findById(testClientId) } returns Optional.of(mockClient)
                        every {
                            mockJwtProvider.generateOauthAccessToken(
                                testEmail,
                                mockAccount.role,
                                testClientId,
                                testScopes,
                            )
                        } returns newAccessToken
                        every { mockJwtProvider.generateOauthRefreshToken(testEmail, testClientId) } returns newRefreshToken
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

                    it("새로운 토큰을 발급하고 반환해야 한다") {
                        val result = reissueOauthTokenService.execute(testRefreshToken)

                        result.accessToken shouldBe newAccessToken
                        result.refreshToken shouldBe newRefreshToken

                        verify(exactly = 1) { mockClientJpaRepository.findById(testClientId) }
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
                        savedRefreshTokenSlot.captured.token shouldBe newRefreshToken
                        savedRefreshTokenSlot.captured.ttl shouldBe oauthRefreshTokenExpiration / 1000
                    }
                }
            }
        }
    })
