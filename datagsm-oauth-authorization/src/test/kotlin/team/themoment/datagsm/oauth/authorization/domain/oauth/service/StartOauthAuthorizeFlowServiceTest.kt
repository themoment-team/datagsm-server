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
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.account.entity.constant.AccountObjectType
import team.themoment.datagsm.common.domain.account.entity.constant.AccountStatus
import team.themoment.datagsm.common.domain.account.repository.AccountJpaRepository
import team.themoment.datagsm.common.domain.client.entity.ClientJpaEntity
import team.themoment.datagsm.common.domain.client.repository.ClientJpaRepository
import team.themoment.datagsm.common.domain.oauth.dto.request.OauthAuthorizeReqDto
import team.themoment.datagsm.common.domain.oauth.entity.IdpSessionRedisEntity
import team.themoment.datagsm.common.domain.oauth.entity.OauthAuthorizeStateRedisEntity
import team.themoment.datagsm.common.domain.oauth.entity.OauthConsentJpaEntity
import team.themoment.datagsm.common.domain.oauth.exception.OAuthException
import team.themoment.datagsm.common.domain.oauth.repository.IdpSessionRedisRepository
import team.themoment.datagsm.common.domain.oauth.repository.OauthAuthorizeStateRedisRepository
import team.themoment.datagsm.common.domain.oauth.repository.OauthConsentJpaRepository
import team.themoment.datagsm.common.domain.student.entity.StudentDataEditRequestJpaEntity
import team.themoment.datagsm.common.domain.student.repository.StudentDataEditRequestJpaRepository
import team.themoment.datagsm.common.global.data.OauthEnvironment
import team.themoment.datagsm.oauth.authorization.domain.oauth.component.IdpSessionResolver
import team.themoment.datagsm.oauth.authorization.domain.oauth.service.IssueAuthorizationCodeService
import team.themoment.datagsm.oauth.authorization.domain.oauth.service.impl.StartOauthAuthorizeFlowServiceImpl
import team.themoment.datagsm.oauth.authorization.global.data.OauthJwtProvisionEnvironment
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
        val mockJwtEnvironment =
            mockk<OauthJwtProvisionEnvironment> {
                every { datagsmApplicationId } returns "datagsm"
            }

        val mockIdpSessionRedisRepository = mockk<IdpSessionRedisRepository>(relaxed = true)
        val mockAccountJpaRepository = mockk<AccountJpaRepository>(relaxed = true)
        val mockStudentDataEditRequestJpaRepository = mockk<StudentDataEditRequestJpaRepository>(relaxed = true)
        val mockOauthConsentJpaRepository = mockk<OauthConsentJpaRepository>(relaxed = true)
        val mockIssueAuthorizationCodeService = mockk<IssueAuthorizationCodeService>(relaxed = true)

        // 세션 자격 판정은 실제 IdpSessionResolver를 그대로 태워, 계정 상태/정보 수정 요청
        // 게이트가 목으로 우회되지 않고 이 테스트에서 함께 검증되게 한다.
        val idpSessionResolver =
            IdpSessionResolver(
                mockIdpSessionRedisRepository,
                mockAccountJpaRepository,
                mockStudentDataEditRequestJpaRepository,
            )

        val startOauthAuthorizeFlowService =
            StartOauthAuthorizeFlowServiceImpl(
                mockClientJpaRepository,
                mockOauthEnvironment,
                mockOauthAuthorizeStateRedisRepository,
                mockJwtEnvironment,
                idpSessionResolver,
                mockOauthConsentJpaRepository,
                mockIssueAuthorizationCodeService,
            )

        afterEach {
            clearAllMocks()
        }

        beforeEach {
            every { mockJwtEnvironment.datagsmApplicationId } returns "datagsm"
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
                        scopes.addAll(setOf("datagsm:account_read", "datagsm:student_read"))
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
                        val response = startOauthAuthorizeFlowService.execute(reqDto, null)

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
                        savedEntitySlot.captured.scopes shouldBe setOf("datagsm:account_read", "datagsm:student_read")
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

                    it("client 전체 scope가 아닌 기본 scope(account_read, student_read)만 state entity에 저장되어야 한다") {
                        val reqDto =
                            OauthAuthorizeReqDto(
                                client_id = testClientId,
                                redirect_uri = testRedirectUri,
                                response_type = "code",
                            )
                        startOauthAuthorizeFlowService.execute(reqDto, null)

                        savedEntitySlot.captured.scopes shouldBe setOf("datagsm:account_read", "datagsm:student_read")
                    }
                }

                context("scope 파라미터가 null이고 client가 deprecated self_read만 가지고 있을 때") {
                    val legacyClient =
                        ClientJpaEntity().apply {
                            id = testClientId
                            secret = "encodedSecret"
                            redirectUrls = setOf(testRedirectUri)
                            scopes.add("datagsm:self_read")
                            clientName = "Legacy Client"
                            serviceName = "Legacy Service"
                        }
                    val savedEntitySlot = slot<OauthAuthorizeStateRedisEntity>()

                    beforeEach {
                        every { mockOauthEnvironment.frontendUrl } returns "http://localhost:3000"
                        every { mockOauthEnvironment.authorizeStateExpirationMs } returns 600000L
                        every { mockClientJpaRepository.findById(testClientId) } returns Optional.of(legacyClient)
                        every { mockOauthAuthorizeStateRedisRepository.save(capture(savedEntitySlot)) } answers { firstArg() }
                    }

                    it("하위 호환을 위해 self_read가 기본 scope로 저장되어야 한다") {
                        val reqDto =
                            OauthAuthorizeReqDto(
                                client_id = testClientId,
                                redirect_uri = testRedirectUri,
                                response_type = "code",
                            )
                        startOauthAuthorizeFlowService.execute(reqDto, null)

                        savedEntitySlot.captured.scopes shouldBe setOf("datagsm:self_read")
                    }
                }

                context("scope 파라미터가 null이고 client가 기본 scope를 하나도 가지고 있지 않을 때") {
                    val noDefaultScopeClient =
                        ClientJpaEntity().apply {
                            id = testClientId
                            secret = "encodedSecret"
                            redirectUrls = setOf(testRedirectUri)
                            scopes.add("datagsm:club_read")
                            clientName = "No Default Scope Client"
                            serviceName = "No Default Scope Service"
                        }

                    beforeEach {
                        every { mockOauthEnvironment.frontendUrl } returns "http://localhost:3000"
                        every { mockOauthEnvironment.authorizeStateExpirationMs } returns 600000L
                        every { mockClientJpaRepository.findById(testClientId) } returns Optional.of(noDefaultScopeClient)
                    }

                    it("OAuthException.InvalidScope가 발생해야 한다") {
                        val reqDto =
                            OauthAuthorizeReqDto(
                                client_id = testClientId,
                                redirect_uri = testRedirectUri,
                                response_type = "code",
                            )

                        shouldThrow<OAuthException.InvalidScope> {
                            startOauthAuthorizeFlowService.execute(reqDto, null)
                        }
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
                                scope = "datagsm:account_read",
                            )
                        startOauthAuthorizeFlowService.execute(reqDto, null)

                        savedEntitySlot.captured.scopes shouldBe setOf("datagsm:account_read")
                    }
                }

                context("허용된 여러 scope를 공백으로 구분하여 요청할 때") {
                    val multiScopeClient =
                        ClientJpaEntity().apply {
                            id = testClientId
                            secret = "encodedSecret"
                            redirectUrls = setOf(testRedirectUri)
                            scopes.addAll(
                                setOf("datagsm:account_read", "datagsm:student_read", "datagsm:club_read", "datagsm:project_read"),
                            )
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
                                scope = "datagsm:account_read datagsm:club_read",
                            )
                        startOauthAuthorizeFlowService.execute(reqDto, null)

                        savedEntitySlot.captured.scopes shouldBe setOf("datagsm:account_read", "datagsm:club_read")
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
                                startOauthAuthorizeFlowService.execute(reqDto, null)
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
                                startOauthAuthorizeFlowService.execute(reqDto, null)
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
                                startOauthAuthorizeFlowService.execute(reqDto, null)
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
                                startOauthAuthorizeFlowService.execute(reqDto, null)
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
                                startOauthAuthorizeFlowService.execute(reqDto, null)
                            }

                        exception.errorDescription shouldBe "지원하지 않는 code_challenge_method입니다."

                        verify(exactly = 0) { mockOauthAuthorizeStateRedisRepository.save(any()) }
                    }
                }

                describe("SSO 세션이 주어졌을 때") {
                    val testSessionId = "session-abc"
                    val testEmail = "user@gsm.hs.kr"
                    val issuedRedirectUrl = "$testRedirectUri?code=test-code"
                    val loginPageUrl = "http://localhost:3000/oauth/authorize"
                    val consentPageUrl = "http://localhost:3000/oauth/consent"

                    val ssoReqDto =
                        OauthAuthorizeReqDto(
                            client_id = testClientId,
                            redirect_uri = testRedirectUri,
                            response_type = "code",
                            scope = "datagsm:account_read",
                        )

                    fun activeAccount() =
                        AccountJpaEntity().apply {
                            id = 1L
                            email = testEmail
                            password = "hashedPassword"
                            status = AccountStatus.ACTIVE
                        }

                    beforeEach {
                        every { mockOauthEnvironment.frontendUrl } returns "http://localhost:3000"
                        every { mockOauthEnvironment.authorizeStateExpirationMs } returns 600000L
                        every {
                            mockOauthAuthorizeStateRedisRepository.save(any<OauthAuthorizeStateRedisEntity>())
                        } answers { firstArg() }
                        every { mockClientJpaRepository.findById(testClientId) } returns Optional.of(mockClient)
                        every { mockIdpSessionRedisRepository.findById(testSessionId) } returns
                            Optional.of(IdpSessionRedisEntity(sessionId = testSessionId, email = testEmail, ttl = 28800))
                        every { mockAccountJpaRepository.findByEmail(testEmail) } returns Optional.of(activeAccount())
                        every { mockOauthConsentJpaRepository.findByAccountIdAndClientId(1L, testClientId) } returns
                            Optional.of(OauthConsentJpaEntity.create(1L, testClientId, setOf("datagsm:account_read")))
                        every {
                            mockIssueAuthorizationCodeService.execute(any(), any(), any(), any(), any(), any(), any())
                        } returns issuedRedirectUrl
                    }

                    context("모든 검증 항목을 충족할 때") {
                        it("로그인 페이지를 거치지 않고 클라이언트로 바로 리다이렉트되어야 한다") {
                            val response = startOauthAuthorizeFlowService.execute(ssoReqDto, testSessionId)

                            response.statusCode shouldBe HttpStatus.FOUND
                            response.headers.location?.toString() shouldBe issuedRedirectUrl

                            verify(exactly = 0) { mockOauthAuthorizeStateRedisRepository.save(any()) }
                        }
                    }

                    context("세션 쿠키가 없을 때") {
                        it("기존 로그인 플로우로 폴백되어야 한다") {
                            val response = startOauthAuthorizeFlowService.execute(ssoReqDto, null)

                            (response.headers.location?.toString() ?: "") shouldContain loginPageUrl
                            verify(exactly = 1) { mockOauthAuthorizeStateRedisRepository.save(any()) }
                        }
                    }

                    context("세션이 만료되었을 때") {
                        beforeEach {
                            every { mockIdpSessionRedisRepository.findById(testSessionId) } returns Optional.empty()
                        }

                        it("기존 로그인 플로우로 폴백되어야 한다") {
                            val response = startOauthAuthorizeFlowService.execute(ssoReqDto, testSessionId)

                            (response.headers.location?.toString() ?: "") shouldContain loginPageUrl
                            verify(exactly = 1) { mockOauthAuthorizeStateRedisRepository.save(any()) }
                        }
                    }

                    context("계정이 ACTIVE 상태가 아닐 때") {
                        beforeEach {
                            every { mockAccountJpaRepository.findByEmail(testEmail) } returns
                                Optional.of(activeAccount().apply { status = AccountStatus.PENDING })
                        }

                        it("기존 로그인 플로우로 폴백되어야 한다") {
                            val response = startOauthAuthorizeFlowService.execute(ssoReqDto, testSessionId)

                            (response.headers.location?.toString() ?: "") shouldContain loginPageUrl
                            verify(exactly = 0) {
                                mockIssueAuthorizationCodeService.execute(any(), any(), any(), any(), any(), any(), any())
                            }
                        }
                    }

                    context("해소되지 않은 학생 정보 수정 요청이 있을 때") {
                        beforeEach {
                            every { mockAccountJpaRepository.findByEmail(testEmail) } returns
                                Optional.of(
                                    activeAccount().apply {
                                        objectType = AccountObjectType.STUDENT
                                        objectId = 10L
                                    },
                                )
                            every { mockStudentDataEditRequestJpaRepository.findByStudentId(10L) } returns
                                Optional.of(StudentDataEditRequestJpaEntity())
                        }

                        it("정보 수정 강제가 우회되지 않도록 로그인 플로우로 폴백되어야 한다") {
                            val response = startOauthAuthorizeFlowService.execute(ssoReqDto, testSessionId)

                            (response.headers.location?.toString() ?: "") shouldContain loginPageUrl
                            verify(exactly = 0) {
                                mockIssueAuthorizationCodeService.execute(any(), any(), any(), any(), any(), any(), any())
                            }
                        }
                    }

                    context("해당 클라이언트에 대한 동의 기록이 없을 때") {
                        beforeEach {
                            every { mockOauthConsentJpaRepository.findByAccountIdAndClientId(1L, testClientId) } returns
                                Optional.empty()
                        }

                        it("비밀번호를 다시 받지 않고 동의 화면으로 보내야 한다") {
                            val response = startOauthAuthorizeFlowService.execute(ssoReqDto, testSessionId)

                            (response.headers.location?.toString() ?: "") shouldContain consentPageUrl
                            verify(exactly = 1) { mockOauthAuthorizeStateRedisRepository.save(any()) }
                            verify(exactly = 0) {
                                mockIssueAuthorizationCodeService.execute(any(), any(), any(), any(), any(), any(), any())
                            }
                        }
                    }

                    context("동의 기록이 요청한 scope를 모두 포함하지 않을 때") {
                        beforeEach {
                            every { mockOauthConsentJpaRepository.findByAccountIdAndClientId(1L, testClientId) } returns
                                Optional.of(OauthConsentJpaEntity.create(1L, testClientId, setOf("datagsm:student_read")))
                        }

                        it("추가 동의를 받도록 동의 화면으로 보내야 한다") {
                            val response = startOauthAuthorizeFlowService.execute(ssoReqDto, testSessionId)

                            (response.headers.location?.toString() ?: "") shouldContain consentPageUrl
                            verify(exactly = 0) {
                                mockIssueAuthorizationCodeService.execute(any(), any(), any(), any(), any(), any(), any())
                            }
                        }
                    }
                }
            }
        }
    })
