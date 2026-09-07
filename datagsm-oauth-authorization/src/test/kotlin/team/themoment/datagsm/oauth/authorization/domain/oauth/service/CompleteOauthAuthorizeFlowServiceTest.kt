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
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.account.entity.constant.AccountObjectType
import team.themoment.datagsm.common.domain.account.entity.constant.AccountStatus
import team.themoment.datagsm.common.domain.account.repository.AccountJpaRepository
import team.themoment.datagsm.common.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.common.domain.oauth.dto.request.OauthAuthorizeSubmitReqDto
import team.themoment.datagsm.common.domain.oauth.entity.IdpSessionHandoffRedisEntity
import team.themoment.datagsm.common.domain.oauth.entity.IdpSessionRedisEntity
import team.themoment.datagsm.common.domain.oauth.entity.OauthAuthorizeStateRedisEntity
import team.themoment.datagsm.common.domain.oauth.entity.OauthConsentJpaEntity
import team.themoment.datagsm.common.domain.oauth.exception.OAuthException
import team.themoment.datagsm.common.domain.oauth.repository.IdpSessionHandoffRedisRepository
import team.themoment.datagsm.common.domain.oauth.repository.IdpSessionRedisRepository
import team.themoment.datagsm.common.domain.oauth.repository.OauthAuthorizeStateRedisRepository
import team.themoment.datagsm.common.domain.oauth.repository.OauthConsentJpaRepository
import team.themoment.datagsm.common.domain.student.entity.StudentDataEditRequestJpaEntity
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.common.domain.student.entity.constant.Sex
import team.themoment.datagsm.common.domain.student.repository.StudentDataEditRequestJpaRepository
import team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.common.global.data.OauthEnvironment
import team.themoment.datagsm.common.global.dto.internal.RateLimitConsumeResult
import team.themoment.datagsm.oauth.authorization.domain.oauth.service.IssueAuthorizationCodeService
import team.themoment.datagsm.oauth.authorization.domain.oauth.service.impl.CompleteOauthAuthorizeFlowServiceImpl
import team.themoment.datagsm.oauth.authorization.global.security.service.OAuthClientRateLimitService
import team.themoment.sdk.exception.ExpectedException
import java.security.MessageDigest
import java.util.Optional

class CompleteOauthAuthorizeFlowServiceTest :
    DescribeSpec({

        val mockAccountJpaRepository = mockk<AccountJpaRepository>()
        val mockStudentJpaRepository = mockk<StudentJpaRepository>()
        val mockClubJpaRepository = mockk<ClubJpaRepository>()
        val mockStudentDataEditRequestJpaRepository = mockk<StudentDataEditRequestJpaRepository>(relaxed = true)
        val mockOauthAuthorizeStateRedisRepository = mockk<OauthAuthorizeStateRedisRepository>(relaxed = true)
        val mockIdpSessionRedisRepository = mockk<IdpSessionRedisRepository>(relaxed = true)
        val mockIdpSessionHandoffRedisRepository = mockk<IdpSessionHandoffRedisRepository>(relaxed = true)
        val mockOauthConsentJpaRepository = mockk<OauthConsentJpaRepository>(relaxed = true)
        val mockIssueAuthorizationCodeService = mockk<IssueAuthorizationCodeService>()
        val mockPasswordEncoder = mockk<PasswordEncoder>()
        val mockOauthEnvironment = mockk<OauthEnvironment>()
        val mockOauthClientRateLimitService = mockk<OAuthClientRateLimitService>()
        val mockApplicationEventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)

        val completeOauthAuthorizeFlowService =
            CompleteOauthAuthorizeFlowServiceImpl(
                mockAccountJpaRepository,
                mockStudentJpaRepository,
                mockClubJpaRepository,
                mockStudentDataEditRequestJpaRepository,
                mockOauthAuthorizeStateRedisRepository,
                mockIdpSessionRedisRepository,
                mockIdpSessionHandoffRedisRepository,
                mockOauthConsentJpaRepository,
                mockIssueAuthorizationCodeService,
                mockPasswordEncoder,
                mockOauthEnvironment,
                mockOauthClientRateLimitService,
                mockApplicationEventPublisher,
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
                val testIssuerUrl = "https://oauth.example.com"
                val codeExpirationSeconds = 300L
                val idpSessionExpirationSeconds = 28800L
                val idpSessionHandoffExpirationSeconds = 60L

                val mockAccount =
                    AccountJpaEntity().apply {
                        id = 1L
                        email = testEmail
                        password = "hashedPassword"
                    }

                beforeEach {
                    every { mockOauthEnvironment.codeExpirationSeconds } returns codeExpirationSeconds
                    every { mockOauthEnvironment.issuerUrl } returns testIssuerUrl
                    every { mockOauthEnvironment.idpSessionExpirationSeconds } returns idpSessionExpirationSeconds
                    every { mockOauthEnvironment.idpSessionHandoffExpirationSeconds } returns idpSessionHandoffExpirationSeconds
                    every { mockOauthClientRateLimitService.tryConsumeAndReturnRemaining(any()) } returns
                        RateLimitConsumeResult(consumed = true, remainingTokens = 299, secondsToWaitForRefill = 0)
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
                            scopes = setOf("self:read"),
                            ttl = 600,
                        )

                    val issuedRedirectUrl = "$testRedirectUri?code=test-code&state=random-state"
                    val sessionSlot = slot<IdpSessionRedisEntity>()
                    val handoffSlot = slot<IdpSessionHandoffRedisEntity>()

                    beforeEach {
                        every { mockOauthAuthorizeStateRedisRepository.findById(testToken) } returns Optional.of(mockStateEntity)
                        every { mockAccountJpaRepository.findByEmail(testEmail) } returns Optional.of(mockAccount)
                        every { mockPasswordEncoder.matches("password123!", mockAccount.password) } returns true
                        every {
                            mockIssueAuthorizationCodeService.execute(any(), any(), any(), any(), any(), any(), any())
                        } returns issuedRedirectUrl
                        every { mockIdpSessionRedisRepository.save(capture(sessionSlot)) } answers { firstArg() }
                        every { mockIdpSessionHandoffRedisRepository.save(capture(handoffSlot)) } answers { firstArg() }
                        every { mockOauthConsentJpaRepository.findByAccountIdAndClientId(any(), any()) } returns Optional.empty()
                        every { mockOauthConsentJpaRepository.save(any<OauthConsentJpaEntity>()) } answers { firstArg() }
                    }

                    it("세션 핸드오프 URL로 302 리다이렉트되어야 한다") {
                        val response = completeOauthAuthorizeFlowService.execute(reqDto)

                        response.statusCode shouldBe HttpStatus.FOUND
                        response.headers.location shouldNotBe null

                        val redirectUrl = response.headers.location?.toString() ?: ""
                        redirectUrl shouldStartWith "$testIssuerUrl/v1/oauth/authorize/session"
                        redirectUrl shouldContain "ticket="
                        redirectUrl shouldContain "verifier="
                    }

                    it("verifier는 평문이 아닌 해시로만 저장되어야 한다") {
                        val response = completeOauthAuthorizeFlowService.execute(reqDto)

                        val redirectUrl = response.headers.location?.toString() ?: ""
                        val verifier =
                            redirectUrl
                                .substringAfter("verifier=")
                                .substringBefore("&")

                        verifier.isNotBlank() shouldBe true
                        handoffSlot.captured.verifierHash shouldNotBe verifier
                        handoffSlot.captured.verifierHash shouldBe sha256Hex(verifier)
                    }

                    it("티켓과 verifier는 서로 다른 값이어야 한다") {
                        val response = completeOauthAuthorizeFlowService.execute(reqDto)

                        val redirectUrl = response.headers.location?.toString() ?: ""
                        val ticket = redirectUrl.substringAfter("ticket=").substringBefore("&")
                        val verifier = redirectUrl.substringAfter("verifier=").substringBefore("&")

                        ticket shouldNotBe verifier
                    }

                    it("Authorization Code 발급이 위임되어야 한다") {
                        completeOauthAuthorizeFlowService.execute(reqDto)

                        verify(exactly = 1) {
                            mockIssueAuthorizationCodeService.execute(
                                testEmail,
                                testClientId,
                                testRedirectUri,
                                "random-state",
                                "challenge",
                                "S256",
                                setOf("self:read"),
                            )
                        }
                    }

                    it("IdP 세션이 생성되고 핸드오프 티켓에 연결되어야 한다") {
                        completeOauthAuthorizeFlowService.execute(reqDto)

                        sessionSlot.captured.email shouldBe testEmail
                        sessionSlot.captured.ttl shouldBe idpSessionExpirationSeconds

                        handoffSlot.captured.sessionId shouldBe sessionSlot.captured.sessionId
                        handoffSlot.captured.redirectUrl shouldBe issuedRedirectUrl
                        handoffSlot.captured.clientId shouldBe testClientId
                        handoffSlot.captured.ttl shouldBe idpSessionHandoffExpirationSeconds
                    }

                    it("요청한 scope가 동의 기록으로 저장되어야 한다") {
                        val consentSlot = slot<OauthConsentJpaEntity>()
                        every { mockOauthConsentJpaRepository.save(capture(consentSlot)) } answers { firstArg() }

                        completeOauthAuthorizeFlowService.execute(reqDto)

                        consentSlot.captured.clientId shouldBe testClientId
                        consentSlot.captured.grantedScopes shouldBe mutableSetOf("self:read")
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
                        verify(exactly = 0) { mockIssueAuthorizationCodeService.execute(any(), any(), any(), any(), any(), any(), any()) }
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
                            scopes = setOf("self:read"),
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

                        exception.message shouldBe "이메일 또는 비밀번호가 일치하지 않습니다."
                        exception.statusCode shouldBe HttpStatus.UNAUTHORIZED

                        verify(exactly = 0) { mockIssueAuthorizationCodeService.execute(any(), any(), any(), any(), any(), any(), any()) }
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
                            scopes = setOf("self:read"),
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

                        exception.message shouldBe "이메일 또는 비밀번호가 일치하지 않습니다."
                        exception.statusCode shouldBe HttpStatus.UNAUTHORIZED

                        verify(exactly = 0) { mockIssueAuthorizationCodeService.execute(any(), any(), any(), any(), any(), any(), any()) }
                    }
                }

                context("gsm.hs.kr 도메인이 아닌 이메일이 주어졌을 때") {
                    val reqDto =
                        OauthAuthorizeSubmitReqDto(
                            email = "outsider@gmail.com",
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
                            scopes = setOf("self:read"),
                            ttl = 600,
                        )

                    beforeEach {
                        every { mockOauthAuthorizeStateRedisRepository.findById(testToken) } returns Optional.of(mockStateEntity)
                    }

                    it("계정 조회 없이 ExpectedException 예외가 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                completeOauthAuthorizeFlowService.execute(reqDto)
                            }

                        exception.message shouldBe "이메일 또는 비밀번호가 일치하지 않습니다."
                        exception.statusCode shouldBe HttpStatus.UNAUTHORIZED

                        verify(exactly = 0) { mockAccountJpaRepository.findByEmail(any()) }
                        verify(exactly = 0) { mockIssueAuthorizationCodeService.execute(any(), any(), any(), any(), any(), any(), any()) }
                    }
                }

                context("승인 대기 중인(PENDING) 계정으로 로그인할 때") {
                    val reqDto =
                        OauthAuthorizeSubmitReqDto(
                            email = testEmail,
                            password = "password123!",
                            token = testToken,
                        )

                    val pendingAccount =
                        AccountJpaEntity().apply {
                            id = 2L
                            email = testEmail
                            password = "hashedPassword"
                            status = AccountStatus.PENDING
                        }

                    val mockStateEntity =
                        OauthAuthorizeStateRedisEntity(
                            token = testToken,
                            clientId = testClientId,
                            redirectUri = testRedirectUri,
                            state = "random-state",
                            codeChallenge = "challenge",
                            codeChallengeMethod = "S256",
                            scopes = setOf("self:read"),
                            ttl = 600,
                        )

                    beforeEach {
                        every { mockOauthAuthorizeStateRedisRepository.findById(testToken) } returns Optional.of(mockStateEntity)
                        every { mockAccountJpaRepository.findByEmail(testEmail) } returns Optional.of(pendingAccount)
                        every { mockPasswordEncoder.matches("password123!", pendingAccount.password) } returns true
                    }

                    it("FORBIDDEN ExpectedException 예외가 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                completeOauthAuthorizeFlowService.execute(reqDto)
                            }

                        exception.message shouldBe "아직 승인되지 않은 계정입니다."
                        exception.statusCode shouldBe HttpStatus.FORBIDDEN

                        verify(exactly = 0) { mockIssueAuthorizationCodeService.execute(any(), any(), any(), any(), any(), any(), any()) }
                    }
                }

                context("미해소 정보 수정 요청이 있고 수정 필드 없이 로그인할 때") {
                    val reqDto =
                        OauthAuthorizeSubmitReqDto(
                            email = testEmail,
                            password = "password123!",
                            token = testToken,
                        )

                    val studentAccount =
                        AccountJpaEntity().apply {
                            id = 3L
                            email = testEmail
                            password = "hashedPassword"
                            objectId = 10L
                            objectType = AccountObjectType.STUDENT
                        }

                    val editRequest =
                        StudentDataEditRequestJpaEntity().apply {
                            studentId = 10L
                            requestStudentNumber = true
                        }

                    val mockStateEntity =
                        OauthAuthorizeStateRedisEntity(
                            token = testToken,
                            clientId = testClientId,
                            redirectUri = testRedirectUri,
                            state = "random-state",
                            codeChallenge = "challenge",
                            codeChallengeMethod = "S256",
                            scopes = setOf("self:read"),
                            ttl = 600,
                        )

                    beforeEach {
                        every { mockOauthAuthorizeStateRedisRepository.findById(testToken) } returns Optional.of(mockStateEntity)
                        every { mockAccountJpaRepository.findByEmail(testEmail) } returns Optional.of(studentAccount)
                        every { mockPasswordEncoder.matches("password123!", studentAccount.password) } returns true
                        every { mockStudentDataEditRequestJpaRepository.findByStudentId(10L) } returns Optional.of(editRequest)
                    }

                    it("UNPROCESSABLE_ENTITY ExpectedException 예외가 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                completeOauthAuthorizeFlowService.execute(reqDto)
                            }

                        exception.message shouldBe "정보 수정이 필요합니다. 정보를 수정한 후 다시 로그인해주세요."
                        exception.statusCode shouldBe HttpStatus.UNPROCESSABLE_ENTITY

                        verify(exactly = 0) { mockIssueAuthorizationCodeService.execute(any(), any(), any(), any(), any(), any(), any()) }
                    }
                }

                context("미해소 정보 수정 요청이 있고 요청된 필드를 채워 로그인할 때") {
                    val reqDto =
                        OauthAuthorizeSubmitReqDto(
                            email = testEmail,
                            password = "password123!",
                            token = testToken,
                            studentGrade = 2,
                            studentClass = 1,
                            studentNumber = 5,
                        )

                    val studentAccount =
                        AccountJpaEntity().apply {
                            id = 3L
                            email = testEmail
                            password = "hashedPassword"
                            objectId = 10L
                            objectType = AccountObjectType.STUDENT
                        }

                    val editRequest =
                        StudentDataEditRequestJpaEntity().apply {
                            studentId = 10L
                            requestStudentNumber = true
                        }

                    val mockStudent =
                        StudentJpaEntity().apply {
                            id = 10L
                            name = "홍길동"
                            email = testEmail
                            sex = Sex.MAN
                        }

                    val mockStateEntity =
                        OauthAuthorizeStateRedisEntity(
                            token = testToken,
                            clientId = testClientId,
                            redirectUri = testRedirectUri,
                            state = "random-state",
                            codeChallenge = "challenge",
                            codeChallengeMethod = "S256",
                            scopes = setOf("self:read"),
                            ttl = 600,
                        )

                    beforeEach {
                        every { mockOauthAuthorizeStateRedisRepository.findById(testToken) } returns Optional.of(mockStateEntity)
                        every { mockAccountJpaRepository.findByEmail(testEmail) } returns Optional.of(studentAccount)
                        every { mockPasswordEncoder.matches("password123!", studentAccount.password) } returns true
                        every { mockStudentDataEditRequestJpaRepository.findByStudentId(10L) } returns Optional.of(editRequest)
                        every { mockStudentJpaRepository.findById(10L) } returns Optional.of(mockStudent)
                        every { mockStudentJpaRepository.existsByStudentNumberAndNotId(2, 1, 5, 10L) } returns false
                        every {
                            mockIssueAuthorizationCodeService.execute(any(), any(), any(), any(), any(), any(), any())
                        } returns "$testRedirectUri?code=test-code&state=random-state"
                        every { mockOauthConsentJpaRepository.findByAccountIdAndClientId(any(), any()) } returns Optional.empty()
                        every { mockOauthConsentJpaRepository.save(any<OauthConsentJpaEntity>()) } answers { firstArg() }
                        every { mockIdpSessionRedisRepository.save(any<IdpSessionRedisEntity>()) } answers { firstArg() }
                        every {
                            mockIdpSessionHandoffRedisRepository.save(any<IdpSessionHandoffRedisEntity>())
                        } answers { firstArg() }
                    }

                    it("정보가 수정되고 302 리다이렉트 ResponseEntity가 반환되어야 한다") {
                        val response = completeOauthAuthorizeFlowService.execute(reqDto)

                        response.statusCode shouldBe HttpStatus.FOUND
                        mockStudent.studentNumber?.studentGrade shouldBe 2
                        mockStudent.studentNumber?.studentClass shouldBe 1
                        mockStudent.studentNumber?.studentNumber shouldBe 5

                        verify(exactly = 1) { mockStudentDataEditRequestJpaRepository.delete(editRequest) }
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
