package team.themoment.datagsm.oauth.authorization.domain.oauth.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldStartWith
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.account.repository.AccountJpaRepository
import team.themoment.datagsm.common.domain.oauth.dto.request.OauthAuthorizeSubmitReqDto
import team.themoment.datagsm.common.domain.oauth.entity.OauthAuthorizeStateRedisEntity
import team.themoment.datagsm.common.domain.oauth.entity.OauthCodeRedisEntity
import team.themoment.datagsm.common.domain.oauth.exception.OAuthException
import team.themoment.datagsm.common.domain.oauth.repository.OauthAuthorizeStateRedisRepository
import team.themoment.datagsm.common.domain.oauth.repository.OauthCodeRedisRepository
import team.themoment.datagsm.common.global.data.OauthEnvironment
import team.themoment.datagsm.oauth.authorization.domain.oauth.service.impl.CompleteOauthAuthorizeFlowServiceImpl
import team.themoment.sdk.exception.ExpectedException
import java.util.Optional

class CompleteOauthAuthorizeFlowServiceTest :
    DescribeSpec({

        val mockAccountJpaRepository = mockk<AccountJpaRepository>()
        val mockOauthCodeRedisRepository = mockk<OauthCodeRedisRepository>(relaxed = true)
        val mockOauthAuthorizeStateRedisRepository = mockk<OauthAuthorizeStateRedisRepository>(relaxed = true)
        val mockPasswordEncoder = mockk<PasswordEncoder>()
        val mockOauthEnvironment = mockk<OauthEnvironment>()

        val completeOauthAuthorizeFlowService =
            CompleteOauthAuthorizeFlowServiceImpl(
                mockAccountJpaRepository,
                mockOauthCodeRedisRepository,
                mockOauthAuthorizeStateRedisRepository,
                mockPasswordEncoder,
                mockOauthEnvironment,
            )

        afterEach {
            clearAllMocks()
        }

        describe("CompleteOauthAuthorizeFlowService 클래스의") {
            describe("execute 메서드는") {

                val testEmail = "user@gsm.hs.kr"
                val testToken = "test-token-123"
                val testClientId = "client-123"
                val testRedirectUri = "https://example.com/callback"
                val codeExpirationSeconds = 300L

                val mockAccount =
                    AccountJpaEntity().apply {
                        id = 1L
                        email = testEmail
                        password = "hashedPassword"
                    }

                beforeEach {
                    every { mockOauthEnvironment.codeExpirationSeconds } returns codeExpirationSeconds
                }

                context("유효한 토큰과 인증 정보가 주어졌을 때") {
                    val reqDto =
                        OauthAuthorizeSubmitReqDto(
                            email = testEmail,
                            password = "password123!",
                            token = testToken,
                        )

                    val mockStateEntity =
                        OauthAuthorizeStateRedisEntity(
                            token = testToken,
                            clientId = testClientId,
                            redirectUri = testRedirectUri,
                            state = "random-state",
                            codeChallenge = "challenge",
                            codeChallengeMethod = "S256",
                            ttl = 600,
                        )

                    val savedEntitySlot = slot<OauthCodeRedisEntity>()

                    beforeEach {
                        every { mockOauthAuthorizeStateRedisRepository.findById(testToken) } returns Optional.of(mockStateEntity)
                        every { mockAccountJpaRepository.findByEmail(testEmail) } returns Optional.of(mockAccount)
                        every { mockPasswordEncoder.matches("password123!", mockAccount.password) } returns true
                        every { mockOauthCodeRedisRepository.save(capture(savedEntitySlot)) } answers { firstArg() }
                    }

                    it("302 리다이렉트 ResponseEntity가 반환되어야 한다") {
                        val response = completeOauthAuthorizeFlowService.execute(reqDto)

                        response.statusCode shouldBe HttpStatus.FOUND
                        response.headers.location shouldNotBe null

                        val redirectUrl = response.headers.location?.toString() ?: ""
                        redirectUrl shouldStartWith testRedirectUri
                        redirectUrl shouldContain "code="
                        redirectUrl shouldContain "state=random-state"
                    }

                    it("Redis에 Authorization Code가 저장되어야 한다") {
                        completeOauthAuthorizeFlowService.execute(reqDto)

                        verify(exactly = 1) { mockOauthCodeRedisRepository.save(any()) }

                        savedEntitySlot.captured.email shouldBe testEmail
                        savedEntitySlot.captured.clientId shouldBe testClientId
                        savedEntitySlot.captured.redirectUri shouldBe testRedirectUri
                        savedEntitySlot.captured.codeChallenge shouldBe "challenge"
                        savedEntitySlot.captured.codeChallengeMethod shouldBe "S256"
                        savedEntitySlot.captured.ttl shouldBe codeExpirationSeconds
                    }

                    it("Redis에서 인증 상태가 삭제되어야 한다") {
                        completeOauthAuthorizeFlowService.execute(reqDto)

                        verify(exactly = 1) { mockOauthAuthorizeStateRedisRepository.deleteById(testToken) }
                    }
                }

                context("토큰이 유효하지 않거나 만료되었을 때") {
                    val reqDto =
                        OauthAuthorizeSubmitReqDto(
                            email = testEmail,
                            password = "password123!",
                            token = "invalid-token",
                        )

                    beforeEach {
                        every { mockOauthAuthorizeStateRedisRepository.findById("invalid-token") } returns Optional.empty()
                    }

                    it("InvalidRequest 예외가 발생해야 한다") {
                        val exception =
                            shouldThrow<OAuthException.InvalidRequest> {
                                completeOauthAuthorizeFlowService.execute(reqDto)
                            }

                        exception.errorDescription shouldBe "인증 토큰이 유효하지 않거나 만료되었습니다. 다시 시도해주세요."

                        verify(exactly = 0) { mockAccountJpaRepository.findByEmail(any()) }
                        verify(exactly = 0) { mockOauthCodeRedisRepository.save(any()) }
                    }
                }

                context("존재하지 않는 이메일이 주어졌을 때") {
                    val reqDto =
                        OauthAuthorizeSubmitReqDto(
                            email = "invalid@gsm.hs.kr",
                            password = "password123!",
                            token = testToken,
                        )

                    val mockStateEntity =
                        OauthAuthorizeStateRedisEntity(
                            token = testToken,
                            clientId = testClientId,
                            redirectUri = testRedirectUri,
                            state = "random-state",
                            codeChallenge = "challenge",
                            codeChallengeMethod = "S256",
                            ttl = 600,
                        )

                    beforeEach {
                        every { mockOauthAuthorizeStateRedisRepository.findById(testToken) } returns Optional.of(mockStateEntity)
                        every { mockAccountJpaRepository.findByEmail("invalid@gsm.hs.kr") } returns Optional.empty()
                    }

                    it("ExpectedException 예외가 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                completeOauthAuthorizeFlowService.execute(reqDto)
                            }

                        exception.message shouldBe "존재하지 않는 이메일입니다."
                        exception.statusCode shouldBe HttpStatus.UNAUTHORIZED

                        verify(exactly = 0) { mockOauthCodeRedisRepository.save(any()) }
                    }
                }

                context("비밀번호가 일치하지 않을 때") {
                    val reqDto =
                        OauthAuthorizeSubmitReqDto(
                            email = testEmail,
                            password = "wrongPassword",
                            token = testToken,
                        )

                    val mockStateEntity =
                        OauthAuthorizeStateRedisEntity(
                            token = testToken,
                            clientId = testClientId,
                            redirectUri = testRedirectUri,
                            state = "random-state",
                            codeChallenge = "challenge",
                            codeChallengeMethod = "S256",
                            ttl = 600,
                        )

                    beforeEach {
                        every { mockOauthAuthorizeStateRedisRepository.findById(testToken) } returns Optional.of(mockStateEntity)
                        every { mockAccountJpaRepository.findByEmail(testEmail) } returns Optional.of(mockAccount)
                        every { mockPasswordEncoder.matches("wrongPassword", mockAccount.password) } returns false
                    }

                    it("ExpectedException 예외가 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                completeOauthAuthorizeFlowService.execute(reqDto)
                            }

                        exception.message shouldBe "비밀번호가 일치하지 않습니다."
                        exception.statusCode shouldBe HttpStatus.UNAUTHORIZED

                        verify(exactly = 0) { mockOauthCodeRedisRepository.save(any()) }
                    }
                }
            }
        }
    })
