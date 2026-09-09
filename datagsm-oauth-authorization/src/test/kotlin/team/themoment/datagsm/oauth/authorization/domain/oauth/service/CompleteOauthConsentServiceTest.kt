package team.themoment.datagsm.oauth.authorization.domain.oauth.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldStartWith
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.account.entity.constant.AccountObjectType
import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.common.domain.account.entity.constant.AccountStatus
import team.themoment.datagsm.common.domain.account.repository.AccountJpaRepository
import team.themoment.datagsm.common.domain.oauth.dto.request.OauthConsentReqDto
import team.themoment.datagsm.common.domain.oauth.entity.IdpSessionRedisEntity
import team.themoment.datagsm.common.domain.oauth.entity.OauthAuthorizeStateRedisEntity
import team.themoment.datagsm.common.domain.oauth.entity.OauthConsentJpaEntity
import team.themoment.datagsm.common.domain.oauth.exception.OAuthException
import team.themoment.datagsm.common.domain.oauth.repository.IdpSessionRedisRepository
import team.themoment.datagsm.common.domain.oauth.repository.OauthAuthorizeStateRedisRepository
import team.themoment.datagsm.common.domain.oauth.repository.OauthConsentJpaRepository
import team.themoment.datagsm.common.domain.student.entity.StudentDataEditRequestJpaEntity
import team.themoment.datagsm.common.domain.student.repository.StudentDataEditRequestJpaRepository
import team.themoment.datagsm.oauth.authorization.domain.oauth.component.IdpSessionResolver
import team.themoment.datagsm.oauth.authorization.domain.oauth.service.impl.CompleteOauthConsentServiceImpl
import team.themoment.sdk.exception.ExpectedException
import java.net.URI
import java.util.Optional

class CompleteOauthConsentServiceTest :
    DescribeSpec({

        val mockOauthAuthorizeStateRedisRepository = mockk<OauthAuthorizeStateRedisRepository>(relaxed = true)
        val mockOauthConsentJpaRepository = mockk<OauthConsentJpaRepository>(relaxed = true)
        val mockIssueAuthorizationCodeService = mockk<IssueAuthorizationCodeService>(relaxed = true)
        val mockIdpSessionRedisRepository = mockk<IdpSessionRedisRepository>(relaxed = true)
        val mockAccountJpaRepository = mockk<AccountJpaRepository>(relaxed = true)
        val mockStudentDataEditRequestJpaRepository = mockk<StudentDataEditRequestJpaRepository>(relaxed = true)

        // 실제 IdpSessionResolver를 태워, 동의 경로에서도 계정 상태·정보 수정 요청 게이트가
        // 그대로 적용되는지 검증한다. 이 엔드포인트는 비밀번호 없이 코드를 발급하므로 중요하다.
        val idpSessionResolver =
            IdpSessionResolver(
                mockIdpSessionRedisRepository,
                mockAccountJpaRepository,
                mockStudentDataEditRequestJpaRepository,
            )

        val completeOauthConsentService =
            CompleteOauthConsentServiceImpl(
                mockOauthAuthorizeStateRedisRepository,
                mockOauthConsentJpaRepository,
                mockIssueAuthorizationCodeService,
                idpSessionResolver,
            )

        afterEach {
            clearAllMocks()
        }

        describe("CompleteOauthConsentService 클래스의") {
            describe("execute 메서드는") {

                val testToken = "state-token-1"
                val testSessionId = "session-1"
                val testEmail = "user@gsm.hs.kr"
                val testClientId = "client-123"
                val testRedirectUri = "https://example.com/callback"
                val testScopes = setOf("datagsm:account_read", "datagsm:student_read")
                val issuedRedirectUrl = "$testRedirectUri?code=issued-code&state=xyz"

                fun activeAccount(): AccountJpaEntity =
                    AccountJpaEntity().apply {
                        id = 1L
                        email = testEmail
                        password = "encoded"
                        status = AccountStatus.ACTIVE
                        role = AccountRole.USER
                        objectType = AccountObjectType.STUDENT
                        objectId = null
                    }

                fun stateEntity(state: String? = "xyz"): OauthAuthorizeStateRedisEntity =
                    OauthAuthorizeStateRedisEntity(
                        token = testToken,
                        clientId = testClientId,
                        redirectUri = testRedirectUri,
                        state = state,
                        codeChallenge = null,
                        codeChallengeMethod = null,
                        scopes = testScopes,
                        ttl = 600,
                    )

                beforeEach {
                    every { mockOauthAuthorizeStateRedisRepository.findById(testToken) } returns
                        Optional.of(stateEntity())
                    every { mockIdpSessionRedisRepository.findById(testSessionId) } returns
                        Optional.of(IdpSessionRedisEntity(testSessionId, testEmail, 28800))
                    every { mockAccountJpaRepository.findByEmail(testEmail) } returns Optional.of(activeAccount())
                    every { mockOauthConsentJpaRepository.findByAccountIdAndClientId(1L, testClientId) } returns
                        Optional.empty()
                    every { mockOauthConsentJpaRepository.saveAndFlush(any<OauthConsentJpaEntity>()) } answers { firstArg() }
                    every {
                        mockIssueAuthorizationCodeService.execute(any(), any(), any(), any(), any(), any(), any())
                    } returns issuedRedirectUrl
                }

                context("사용자가 동의를 승인했을 때") {
                    it("코드를 발급하고 클라이언트로 리다이렉트해야 한다") {
                        val response =
                            completeOauthConsentService.execute(
                                OauthConsentReqDto(testToken, approved = true),
                                testSessionId,
                            )

                        response.statusCode shouldBe HttpStatus.FOUND
                        response.headers.location?.toString() shouldBe issuedRedirectUrl
                    }

                    it("승인한 scope가 동의 기록으로 저장되어야 한다") {
                        val consentSlot = slot<OauthConsentJpaEntity>()
                        every { mockOauthConsentJpaRepository.saveAndFlush(capture(consentSlot)) } answers { firstArg() }

                        completeOauthConsentService.execute(
                            OauthConsentReqDto(testToken, approved = true),
                            testSessionId,
                        )

                        consentSlot.captured.accountId shouldBe 1L
                        consentSlot.captured.clientId shouldBe testClientId
                        consentSlot.captured.grantedScopes shouldBe testScopes
                    }

                    it("재사용되지 않도록 state 토큰을 소비해야 한다") {
                        completeOauthConsentService.execute(
                            OauthConsentReqDto(testToken, approved = true),
                            testSessionId,
                        )

                        verify(exactly = 1) { mockOauthAuthorizeStateRedisRepository.deleteById(testToken) }
                    }
                }

                context("사용자가 동의를 거부했을 때") {
                    it("access_denied로 클라이언트에 리다이렉트해야 한다") {
                        val response =
                            completeOauthConsentService.execute(
                                OauthConsentReqDto(testToken, approved = false),
                                testSessionId,
                            )

                        response.statusCode shouldBe HttpStatus.FOUND
                        val location = response.headers.location?.toString() ?: ""
                        location shouldStartWith "$testRedirectUri?error=access_denied"
                        location shouldContain "state=xyz"
                    }

                    it("코드를 발급하거나 동의를 기록하지 않아야 한다") {
                        completeOauthConsentService.execute(
                            OauthConsentReqDto(testToken, approved = false),
                            testSessionId,
                        )

                        verify(exactly = 0) {
                            mockIssueAuthorizationCodeService.execute(any(), any(), any(), any(), any(), any(), any())
                        }
                        verify(exactly = 0) { mockOauthConsentJpaRepository.saveAndFlush(any<OauthConsentJpaEntity>()) }
                    }

                    it("승인으로 뒤집지 못하도록 state 토큰을 소비해야 한다") {
                        completeOauthConsentService.execute(
                            OauthConsentReqDto(testToken, approved = false),
                            testSessionId,
                        )

                        verify(exactly = 1) { mockOauthAuthorizeStateRedisRepository.deleteById(testToken) }
                    }

                    it("state에 URL 예약 문자가 있어도 파라미터가 주입되지 않아야 한다") {
                        every { mockOauthAuthorizeStateRedisRepository.findById(testToken) } returns
                            Optional.of(stateEntity(state = "a&injected=evil"))

                        val response =
                            completeOauthConsentService.execute(
                                OauthConsentReqDto(testToken, approved = false),
                                testSessionId,
                            )

                        val location = response.headers.location?.toString() ?: ""
                        val parsedKeys =
                            URI
                                .create(location)
                                .rawQuery
                                .split("&")
                                .map { it.substringBefore("=") }
                        parsedKeys.contains("injected") shouldBe false
                        parsedKeys shouldBe listOf("error", "error_description", "state")
                    }
                }

                context("세션 쿠키가 없을 때") {
                    it("코드를 발급하지 않고 401이 반환되어야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                completeOauthConsentService.execute(
                                    OauthConsentReqDto(testToken, approved = true),
                                    null,
                                )
                            }

                        exception.statusCode shouldBe HttpStatus.UNAUTHORIZED
                        verify(exactly = 0) {
                            mockIssueAuthorizationCodeService.execute(any(), any(), any(), any(), any(), any(), any())
                        }
                    }
                }

                context("세션이 만료되었을 때") {
                    it("코드를 발급하지 않고 401이 반환되어야 한다") {
                        every { mockIdpSessionRedisRepository.findById(testSessionId) } returns Optional.empty()

                        shouldThrow<ExpectedException> {
                            completeOauthConsentService.execute(
                                OauthConsentReqDto(testToken, approved = true),
                                testSessionId,
                            )
                        }

                        verify(exactly = 0) {
                            mockIssueAuthorizationCodeService.execute(any(), any(), any(), any(), any(), any(), any())
                        }
                    }
                }

                context("계정이 ACTIVE 상태가 아닐 때") {
                    it("코드를 발급하지 않아야 한다") {
                        every { mockAccountJpaRepository.findByEmail(testEmail) } returns
                            Optional.of(activeAccount().apply { status = AccountStatus.PENDING })

                        shouldThrow<ExpectedException> {
                            completeOauthConsentService.execute(
                                OauthConsentReqDto(testToken, approved = true),
                                testSessionId,
                            )
                        }

                        verify(exactly = 0) {
                            mockIssueAuthorizationCodeService.execute(any(), any(), any(), any(), any(), any(), any())
                        }
                    }
                }

                context("해소되지 않은 학생 정보 수정 요청이 있을 때") {
                    it("정보 수정 강제가 동의 화면으로 우회되지 않아야 한다") {
                        every { mockAccountJpaRepository.findByEmail(testEmail) } returns
                            Optional.of(activeAccount().apply { objectId = 10L })
                        every { mockStudentDataEditRequestJpaRepository.findByStudentId(10L) } returns
                            Optional.of(StudentDataEditRequestJpaEntity())

                        shouldThrow<ExpectedException> {
                            completeOauthConsentService.execute(
                                OauthConsentReqDto(testToken, approved = true),
                                testSessionId,
                            )
                        }

                        verify(exactly = 0) {
                            mockIssueAuthorizationCodeService.execute(any(), any(), any(), any(), any(), any(), any())
                        }
                    }
                }

                context("state 토큰이 유효하지 않을 때") {
                    it("400이 반환되어야 한다") {
                        every { mockOauthAuthorizeStateRedisRepository.findById(testToken) } returns Optional.empty()

                        shouldThrow<OAuthException.InvalidRequest> {
                            completeOauthConsentService.execute(
                                OauthConsentReqDto(testToken, approved = true),
                                testSessionId,
                            )
                        }
                    }
                }

                context("동의 기록 저장이 유니크 제약에 걸렸을 때") {
                    it("재조회 후 병합해 코드 발급이 실패하지 않아야 한다") {
                        val existing = OauthConsentJpaEntity.create(1L, testClientId, setOf("datagsm:account_read"))
                        every { mockOauthConsentJpaRepository.findByAccountIdAndClientId(1L, testClientId) } returnsMany
                            listOf(Optional.empty(), Optional.of(existing))
                        every { mockOauthConsentJpaRepository.saveAndFlush(any<OauthConsentJpaEntity>()) } throws
                            DataIntegrityViolationException("duplicate") andThen existing

                        val response =
                            completeOauthConsentService.execute(
                                OauthConsentReqDto(testToken, approved = true),
                                testSessionId,
                            )

                        response.statusCode shouldBe HttpStatus.FOUND
                        existing.grantedScopes shouldBe testScopes
                    }
                }
            }
        }
    })
