package team.themoment.datagsm.authorization.domain.oauth.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import team.themoment.datagsm.authorization.domain.oauth.dto.request.OauthCodeReqDto
import team.themoment.datagsm.authorization.domain.oauth.property.OauthProperties
import team.themoment.datagsm.authorization.domain.oauth.service.impl.IssueOauthCodeServiceImpl
import team.themoment.datagsm.common.domain.account.AccountJpaEntity
import team.themoment.datagsm.common.domain.account.OauthCodeRedisEntity
import team.themoment.datagsm.common.domain.account.repository.AccountJpaRepository
import team.themoment.datagsm.common.domain.client.ClientJpaEntity
import team.themoment.datagsm.common.domain.client.repository.ClientJpaRepository
import team.themoment.datagsm.common.domain.oauth.repository.OauthCodeRedisRepository
import team.themoment.sdk.exception.ExpectedException
import java.util.Optional

class IssueOauthCodeServiceTest :
    DescribeSpec({

        val mockAccountJpaRepository = mockk<AccountJpaRepository>()
        val mockClientJpaRepository = mockk<ClientJpaRepository>()
        val mockPasswordEncoder = mockk<PasswordEncoder>()
        val mockOauthCodeRedisRepository = mockk<OauthCodeRedisRepository>()
        val mockOauthProperties = mockk<OauthProperties>()

        val issueOauthCodeService =
            IssueOauthCodeServiceImpl(
                mockAccountJpaRepository,
                mockClientJpaRepository,
                mockPasswordEncoder,
                mockOauthCodeRedisRepository,
                mockOauthProperties,
            )

        afterEach {
            clearAllMocks()
        }

        describe("IssueOauthCodeService 클래스의") {
            describe("execute 메서드는") {

                val testEmail = "test@gsm.hs.kr"
                val testPassword = "password123!"
                val testClientId = "client-123"
                val testRedirectUrl = "https://example.com/callback"
                val codeExpirationSeconds = 300L

                val mockAccount =
                    AccountJpaEntity().apply {
                        id = 1L
                        email = testEmail
                        password = "encodedPassword"
                    }

                val mockClient =
                    ClientJpaEntity().apply {
                        id = testClientId
                        secret = "encodedSecret"
                        redirectUrls = setOf(testRedirectUrl)
                        name = "Test Client"
                    }

                beforeEach {
                    every { mockOauthProperties.codeExpirationSeconds } returns codeExpirationSeconds
                }

                context("존재하지 않는 Client ID로 요청할 때") {
                    val reqDto =
                        OauthCodeReqDto(
                            email = testEmail,
                            password = testPassword,
                            clientId = "invalid-client-id",
                            redirectUrl = testRedirectUrl,
                        )

                    beforeEach {
                        every { mockClientJpaRepository.findById("invalid-client-id") } returns Optional.empty()
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                issueOauthCodeService.execute(reqDto)
                            }

                        exception.message shouldBe "존재하지 않는 Client Id 입니다."
                        exception.statusCode shouldBe HttpStatus.NOT_FOUND

                        verify(exactly = 1) { mockClientJpaRepository.findById("invalid-client-id") }
                        verify(exactly = 0) { mockAccountJpaRepository.findByEmail(any()) }
                    }
                }

                context("등록되지 않은 Redirect URL로 요청할 때") {
                    val invalidRedirectUrl = "https://malicious.com/callback"
                    val reqDto =
                        OauthCodeReqDto(
                            email = testEmail,
                            password = testPassword,
                            clientId = testClientId,
                            redirectUrl = invalidRedirectUrl,
                        )

                    beforeEach {
                        every { mockClientJpaRepository.findById(testClientId) } returns Optional.of(mockClient)
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                issueOauthCodeService.execute(reqDto)
                            }

                        exception.message shouldBe "등록되지 않은 Redirect URL 입니다."
                        exception.statusCode shouldBe HttpStatus.BAD_REQUEST

                        verify(exactly = 1) { mockClientJpaRepository.findById(testClientId) }
                        verify(exactly = 0) { mockAccountJpaRepository.findByEmail(any()) }
                    }
                }

                context("존재하지 않는 이메일로 요청할 때") {
                    val reqDto =
                        OauthCodeReqDto(
                            email = "nonexistent@gsm.hs.kr",
                            password = testPassword,
                            clientId = testClientId,
                            redirectUrl = testRedirectUrl,
                        )

                    beforeEach {
                        every { mockClientJpaRepository.findById(testClientId) } returns Optional.of(mockClient)
                        every { mockAccountJpaRepository.findByEmail("nonexistent@gsm.hs.kr") } returns Optional.empty()
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                issueOauthCodeService.execute(reqDto)
                            }

                        exception.message shouldBe "존재하지 않는 회원의 이메일입니다."
                        exception.statusCode shouldBe HttpStatus.NOT_FOUND

                        verify(exactly = 1) { mockClientJpaRepository.findById(testClientId) }
                        verify(exactly = 1) { mockAccountJpaRepository.findByEmail("nonexistent@gsm.hs.kr") }
                    }
                }

                context("비밀번호가 일치하지 않을 때") {
                    val reqDto =
                        OauthCodeReqDto(
                            email = testEmail,
                            password = "wrongPassword",
                            clientId = testClientId,
                            redirectUrl = testRedirectUrl,
                        )

                    beforeEach {
                        every { mockClientJpaRepository.findById(testClientId) } returns Optional.of(mockClient)
                        every { mockAccountJpaRepository.findByEmail(testEmail) } returns Optional.of(mockAccount)
                        every { mockPasswordEncoder.matches("wrongPassword", mockAccount.password) } returns false
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                issueOauthCodeService.execute(reqDto)
                            }

                        exception.message shouldBe "비밀번호가 일치하지 않습니다."
                        exception.statusCode shouldBe HttpStatus.UNAUTHORIZED

                        verify(exactly = 1) { mockPasswordEncoder.matches("wrongPassword", mockAccount.password) }
                        verify(exactly = 0) { mockOauthCodeRedisRepository.save(any()) }
                    }
                }

                context("모든 검증을 통과할 때") {
                    val reqDto =
                        OauthCodeReqDto(
                            email = testEmail,
                            password = testPassword,
                            clientId = testClientId,
                            redirectUrl = testRedirectUrl,
                        )

                    val savedEntitySlot = slot<OauthCodeRedisEntity>()

                    beforeEach {
                        every { mockClientJpaRepository.findById(testClientId) } returns Optional.of(mockClient)
                        every { mockAccountJpaRepository.findByEmail(testEmail) } returns Optional.of(mockAccount)
                        every { mockPasswordEncoder.matches(testPassword, mockAccount.password) } returns true
                        every { mockOauthCodeRedisRepository.save(capture(savedEntitySlot)) } answers { firstArg() }
                    }

                    it("OAuth 코드를 생성하고 반환해야 한다") {
                        val result = issueOauthCodeService.execute(reqDto)

                        result.code shouldNotBe null
                        result.code.isNotBlank() shouldBe true

                        verify(exactly = 1) { mockClientJpaRepository.findById(testClientId) }
                        verify(exactly = 1) { mockAccountJpaRepository.findByEmail(testEmail) }
                        verify(exactly = 1) { mockPasswordEncoder.matches(testPassword, mockAccount.password) }
                        verify(exactly = 1) { mockOauthCodeRedisRepository.save(any()) }

                        savedEntitySlot.captured.email shouldBe testEmail
                        savedEntitySlot.captured.clientId shouldBe testClientId
                        savedEntitySlot.captured.ttl shouldBe codeExpirationSeconds
                    }
                }
            }
        }
    })
