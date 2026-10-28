package team.themoment.datagsm.web.domain.account.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import team.themoment.datagsm.common.domain.account.dto.request.DeleteMyAccountReqDto
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.account.entity.constant.AccountObjectType
import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.common.domain.account.repository.AccountJpaRepository
import team.themoment.datagsm.common.domain.application.entity.ApplicationJpaEntity
import team.themoment.datagsm.common.domain.application.repository.ApplicationJpaRepository
import team.themoment.datagsm.common.domain.auth.repository.ApiKeyJpaRepository
import team.themoment.datagsm.common.domain.client.repository.ClientJpaRepository
import team.themoment.datagsm.common.domain.event.entity.EventJpaEntity
import team.themoment.datagsm.common.domain.event.repository.EventJpaRepository
import team.themoment.datagsm.common.domain.oauth.entity.IdpSessionRedisEntity
import team.themoment.datagsm.common.domain.oauth.entity.OauthRefreshTokenRedisEntity
import team.themoment.datagsm.common.domain.oauth.repository.IdpSessionRedisRepository
import team.themoment.datagsm.common.domain.oauth.repository.OauthConsentJpaRepository
import team.themoment.datagsm.common.domain.oauth.repository.OauthRefreshTokenRedisRepository
import team.themoment.datagsm.common.domain.teacher.repository.TeacherJpaRepository
import team.themoment.datagsm.web.domain.account.service.impl.DeleteMyAccountServiceImpl
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException

class DeleteMyAccountServiceTest :
    DescribeSpec({

        lateinit var mockCurrentUserProvider: CurrentUserProvider
        lateinit var mockPasswordEncoder: PasswordEncoder
        lateinit var mockApiKeyJpaRepository: ApiKeyJpaRepository
        lateinit var mockClientJpaRepository: ClientJpaRepository
        lateinit var mockAccountJpaRepository: AccountJpaRepository
        lateinit var mockEventJpaRepository: EventJpaRepository
        lateinit var mockApplicationJpaRepository: ApplicationJpaRepository
        lateinit var mockOauthConsentJpaRepository: OauthConsentJpaRepository
        lateinit var mockTeacherJpaRepository: TeacherJpaRepository
        lateinit var mockIdpSessionRedisRepository: IdpSessionRedisRepository
        lateinit var mockOauthRefreshTokenRedisRepository: OauthRefreshTokenRedisRepository
        lateinit var deleteMyAccountService: DeleteMyAccountService

        val email = "user@gsm.hs.kr"

        fun account(
            objectType: AccountObjectType? = AccountObjectType.STUDENT,
            objectId: Long? = 10L,
        ) = AccountJpaEntity().apply {
            id = 1L
            this.email = email
            password = "encoded_password"
            role = AccountRole.USER
            this.objectType = objectType
            this.objectId = objectId
        }

        fun stubCleanup(account: AccountJpaEntity) {
            every { mockCurrentUserProvider.getCurrentAccount() } returns account
            every { mockPasswordEncoder.matches("plain_password", "encoded_password") } returns true
            every { mockEventJpaRepository.findAllByAccount(account) } returns emptyList()
            justRun { mockEventJpaRepository.deleteAll(any<Iterable<EventJpaEntity>>()) }
            every { mockApplicationJpaRepository.findAllByAccount(account) } returns emptyList()
            justRun { mockApiKeyJpaRepository.deleteByAccount(account) }
            justRun { mockClientJpaRepository.deleteAllByAccount(account) }
            justRun { mockOauthConsentJpaRepository.deleteAllByAccountId(1L) }
            justRun { mockTeacherJpaRepository.deleteById(any()) }
            justRun { mockAccountJpaRepository.delete(account) }
            every { mockIdpSessionRedisRepository.findAllByEmail(email) } returns emptyList()
            justRun { mockIdpSessionRedisRepository.deleteAll(any<Iterable<IdpSessionRedisEntity>>()) }
            every { mockOauthRefreshTokenRedisRepository.findAllByEmail(email) } returns emptyList()
            justRun { mockOauthRefreshTokenRedisRepository.deleteAll(any<Iterable<OauthRefreshTokenRedisEntity>>()) }
        }

        beforeEach {
            mockCurrentUserProvider = mockk()
            mockPasswordEncoder = mockk()
            mockApiKeyJpaRepository = mockk()
            mockClientJpaRepository = mockk()
            mockAccountJpaRepository = mockk()
            mockEventJpaRepository = mockk()
            mockApplicationJpaRepository = mockk()
            mockOauthConsentJpaRepository = mockk()
            mockTeacherJpaRepository = mockk()
            mockIdpSessionRedisRepository = mockk()
            mockOauthRefreshTokenRedisRepository = mockk()
            deleteMyAccountService =
                DeleteMyAccountServiceImpl(
                    mockCurrentUserProvider,
                    mockPasswordEncoder,
                    mockApiKeyJpaRepository,
                    mockClientJpaRepository,
                    mockAccountJpaRepository,
                    mockEventJpaRepository,
                    mockApplicationJpaRepository,
                    mockOauthConsentJpaRepository,
                    mockTeacherJpaRepository,
                    mockIdpSessionRedisRepository,
                    mockOauthRefreshTokenRedisRepository,
                )
        }

        describe("DeleteMyAccountService 클래스의") {
            describe("execute 메서드는") {

                context("웹훅 이벤트와 애플리케이션을 가진 계정이 탈퇴할 때") {
                    it("계정을 참조하는 행을 모두 지운 뒤 계정을 삭제해야 한다") {
                        // Given
                        val account = account()
                        val event = EventJpaEntity()
                        val application = ApplicationJpaEntity().apply { id = "app-1" }
                        stubCleanup(account)
                        every { mockEventJpaRepository.findAllByAccount(account) } returns listOf(event)
                        every { mockApplicationJpaRepository.findAllByAccount(account) } returns listOf(application)
                        justRun { mockClientJpaRepository.removeScopesByApplicationId("app-1:") }
                        justRun { mockApplicationJpaRepository.delete(application) }

                        // When
                        deleteMyAccountService.execute(DeleteMyAccountReqDto(password = "plain_password"))

                        // Then
                        verifyOrder {
                            mockEventJpaRepository.deleteAll(listOf(event))
                            mockClientJpaRepository.removeScopesByApplicationId("app-1:")
                            mockApplicationJpaRepository.delete(application)
                            mockApiKeyJpaRepository.deleteByAccount(account)
                            mockClientJpaRepository.deleteAllByAccount(account)
                            mockOauthConsentJpaRepository.deleteAllByAccountId(1L)
                            mockAccountJpaRepository.delete(account)
                        }
                    }
                }

                context("로그인 세션과 refresh token이 남아 있을 때") {
                    it("해당 이메일의 세션과 토큰을 모두 삭제해야 한다") {
                        // Given
                        val account = account()
                        val sessions = listOf(IdpSessionRedisEntity(sessionId = "s1", email = email, ttl = 60))
                        val tokens =
                            listOf(
                                OauthRefreshTokenRedisEntity(
                                    id = "$email:client",
                                    email = email,
                                    clientId = "client",
                                    token = "t",
                                    scopes = emptySet(),
                                    ttl = 60,
                                ),
                            )
                        stubCleanup(account)
                        every { mockIdpSessionRedisRepository.findAllByEmail(email) } returns sessions
                        every { mockOauthRefreshTokenRedisRepository.findAllByEmail(email) } returns tokens

                        // When
                        deleteMyAccountService.execute(DeleteMyAccountReqDto(password = "plain_password"))

                        // Then
                        verify(exactly = 1) { mockIdpSessionRedisRepository.deleteAll(sessions) }
                        verify(exactly = 1) { mockOauthRefreshTokenRedisRepository.deleteAll(tokens) }
                    }
                }

                context("선생님 계정이 탈퇴할 때") {
                    it("같은 이메일로 다시 가입할 수 있도록 선생님 정보도 삭제해야 한다") {
                        // Given
                        val account = account(objectType = AccountObjectType.TEACHER, objectId = 7L)
                        stubCleanup(account)

                        // When
                        deleteMyAccountService.execute(DeleteMyAccountReqDto(password = "plain_password"))

                        // Then
                        verify(exactly = 1) { mockTeacherJpaRepository.deleteById(7L) }
                    }
                }

                context("학생 계정이 탈퇴할 때") {
                    it("학교 데이터인 학생 정보는 남기고 선생님 정보도 건드리지 않아야 한다") {
                        // Given
                        val account = account(objectType = AccountObjectType.STUDENT, objectId = 10L)
                        stubCleanup(account)

                        // When
                        deleteMyAccountService.execute(DeleteMyAccountReqDto(password = "plain_password"))

                        // Then
                        verify(exactly = 0) { mockTeacherJpaRepository.deleteById(any()) }
                        verify(exactly = 1) { mockAccountJpaRepository.delete(account) }
                    }
                }

                context("잘못된 비밀번호로 탈퇴를 요청할 때") {
                    it("UNAUTHORIZED 상태의 ExpectedException이 발생하고 아무것도 삭제하지 않아야 한다") {
                        // Given
                        val account = account()
                        every { mockCurrentUserProvider.getCurrentAccount() } returns account
                        every { mockPasswordEncoder.matches("wrong_password", "encoded_password") } returns false

                        // When
                        val exception =
                            shouldThrow<ExpectedException> {
                                deleteMyAccountService.execute(DeleteMyAccountReqDto(password = "wrong_password"))
                            }

                        // Then
                        exception.statusCode shouldBe HttpStatus.UNAUTHORIZED
                        verify(exactly = 0) { mockEventJpaRepository.deleteAll(any<Iterable<EventJpaEntity>>()) }
                        verify(exactly = 0) { mockApiKeyJpaRepository.deleteByAccount(any()) }
                        verify(exactly = 0) { mockAccountJpaRepository.delete(any()) }
                        verify(exactly = 0) { mockIdpSessionRedisRepository.deleteAll(any<Iterable<IdpSessionRedisEntity>>()) }
                    }
                }

                context("계정을 찾을 수 없을 때") {
                    it("NOT_FOUND 상태의 ExpectedException이 발생해야 한다") {
                        // Given
                        every { mockCurrentUserProvider.getCurrentAccount() } throws
                            ExpectedException("계정을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)

                        // When
                        val exception =
                            shouldThrow<ExpectedException> {
                                deleteMyAccountService.execute(DeleteMyAccountReqDto(password = "any_password"))
                            }

                        // Then
                        exception.statusCode shouldBe HttpStatus.NOT_FOUND
                        verify(exactly = 0) { mockApiKeyJpaRepository.deleteByAccount(any()) }
                    }
                }
            }
        }
    })
