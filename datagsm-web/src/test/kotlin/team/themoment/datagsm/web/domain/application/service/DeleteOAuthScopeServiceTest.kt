package team.themoment.datagsm.web.domain.application.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.http.HttpStatus
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.common.domain.application.entity.ApplicationJpaEntity
import team.themoment.datagsm.common.domain.application.entity.OAuthScopeJpaEntity
import team.themoment.datagsm.common.domain.application.repository.OAuthScopeJpaRepository
import team.themoment.datagsm.common.domain.client.repository.ClientJpaRepository
import team.themoment.datagsm.web.domain.application.service.impl.DeleteOAuthScopeServiceImpl
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException
import java.util.Optional

class DeleteOAuthScopeServiceTest :
    DescribeSpec({

        val mockOauthScopeJpaRepository = mockk<OAuthScopeJpaRepository>()
        val mockClientJpaRepository = mockk<ClientJpaRepository>()
        val mockCurrentUserProvider = mockk<CurrentUserProvider>()

        val service =
            DeleteOAuthScopeServiceImpl(
                mockOauthScopeJpaRepository,
                mockClientJpaRepository,
                mockCurrentUserProvider,
            )

        afterEach {
            clearAllMocks()
        }

        describe("DeleteOAuthScopeService 클래스의") {
            describe("execute 메서드는") {

                val applicationId = "app-uuid-1234"
                val scopeId = 10L

                val ownerAccount =
                    AccountJpaEntity().apply {
                        id = 1L
                        email = "owner@gsm.hs.kr"
                        role = AccountRole.USER
                    }

                val application =
                    ApplicationJpaEntity().apply {
                        id = applicationId
                        name = "My Application"
                        account = ownerAccount
                    }

                val scope =
                    OAuthScopeJpaEntity().apply {
                        id = scopeId
                        scopeName = "profile"
                        description = "사용자 프로필 정보 조회"
                        this.application = application
                    }

                context("소유자가 권한 범위를 삭제할 때") {
                    beforeEach {
                        every { mockOauthScopeJpaRepository.findById(scopeId) } returns Optional.of(scope)
                        every { mockCurrentUserProvider.getCurrentAccount() } returns ownerAccount
                        every { mockClientJpaRepository.removeScopeFromClients("$applicationId:profile") } returns Unit
                        every { mockOauthScopeJpaRepository.delete(scope) } returns Unit
                    }

                    it("권한 범위가 성공적으로 삭제되어야 한다") {
                        service.execute(applicationId, scopeId)

                        verify(exactly = 1) { mockOauthScopeJpaRepository.findById(scopeId) }
                        verify(exactly = 1) { mockCurrentUserProvider.getCurrentAccount() }
                        verify(exactly = 1) { mockClientJpaRepository.removeScopeFromClients("$applicationId:profile") }
                        verify(exactly = 1) { mockOauthScopeJpaRepository.delete(scope) }
                    }
                }

                context("ADMIN이 다른 사용자의 권한 범위를 삭제할 때") {
                    val adminAccount =
                        AccountJpaEntity().apply {
                            id = 99L
                            email = "admin@gsm.hs.kr"
                            role = AccountRole.ADMIN
                        }

                    beforeEach {
                        every { mockOauthScopeJpaRepository.findById(scopeId) } returns Optional.of(scope)
                        every { mockCurrentUserProvider.getCurrentAccount() } returns adminAccount
                        every { mockClientJpaRepository.removeScopeFromClients("$applicationId:profile") } returns Unit
                        every { mockOauthScopeJpaRepository.delete(scope) } returns Unit
                    }

                    it("성공적으로 삭제되어야 한다") {
                        service.execute(applicationId, scopeId)

                        verify(exactly = 1) { mockClientJpaRepository.removeScopeFromClients("$applicationId:profile") }
                        verify(exactly = 1) { mockOauthScopeJpaRepository.delete(scope) }
                    }
                }

                context("ROOT가 다른 사용자의 권한 범위를 삭제할 때") {
                    val rootAccount =
                        AccountJpaEntity().apply {
                            id = 100L
                            email = "root@gsm.hs.kr"
                            role = AccountRole.ROOT
                        }

                    beforeEach {
                        every { mockOauthScopeJpaRepository.findById(scopeId) } returns Optional.of(scope)
                        every { mockCurrentUserProvider.getCurrentAccount() } returns rootAccount
                        every { mockClientJpaRepository.removeScopeFromClients("$applicationId:profile") } returns Unit
                        every { mockOauthScopeJpaRepository.delete(scope) } returns Unit
                    }

                    it("성공적으로 삭제되어야 한다") {
                        service.execute(applicationId, scopeId)

                        verify(exactly = 1) { mockClientJpaRepository.removeScopeFromClients("$applicationId:profile") }
                        verify(exactly = 1) { mockOauthScopeJpaRepository.delete(scope) }
                    }
                }

                context("소유자가 아닌 일반 사용자가 삭제를 시도할 때") {
                    val otherAccount =
                        AccountJpaEntity().apply {
                            id = 2L
                            email = "other@gsm.hs.kr"
                            role = AccountRole.USER
                        }

                    beforeEach {
                        every { mockOauthScopeJpaRepository.findById(scopeId) } returns Optional.of(scope)
                        every { mockCurrentUserProvider.getCurrentAccount() } returns otherAccount
                    }

                    it("403 FORBIDDEN 예외가 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                service.execute(applicationId, scopeId)
                            }

                        exception.statusCode shouldBe HttpStatus.FORBIDDEN
                        exception.message shouldBe "OAuth 권한 범위 삭제 권한이 없습니다."

                        verify(exactly = 0) { mockOauthScopeJpaRepository.delete(any()) }
                    }
                }

                context("존재하지 않는 scopeId로 삭제를 시도할 때") {
                    val nonExistingScopeId = 999L

                    beforeEach {
                        every { mockOauthScopeJpaRepository.findById(nonExistingScopeId) } returns Optional.empty()
                    }

                    it("404 NOT_FOUND 예외가 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                service.execute(applicationId, nonExistingScopeId)
                            }

                        exception.statusCode shouldBe HttpStatus.NOT_FOUND
                        exception.message shouldBe "OAuth 권한 범위를 찾을 수 없습니다."

                        verify(exactly = 0) { mockOauthScopeJpaRepository.delete(any()) }
                    }
                }

                context("scopeId는 존재하지만 다른 Application의 권한 범위일 때") {
                    val otherApplication =
                        ApplicationJpaEntity().apply {
                            id = "other-app-id"
                            name = "Other Application"
                            account = ownerAccount
                        }

                    val scopeOfOtherApp =
                        OAuthScopeJpaEntity().apply {
                            id = scopeId
                            scopeName = "profile"
                            description = "사용자 프로필 정보 조회"
                            this.application = otherApplication
                        }

                    beforeEach {
                        every { mockOauthScopeJpaRepository.findById(scopeId) } returns Optional.of(scopeOfOtherApp)
                    }

                    it("404 NOT_FOUND 예외가 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                service.execute(applicationId, scopeId)
                            }

                        exception.statusCode shouldBe HttpStatus.NOT_FOUND
                        exception.message shouldBe "OAuth 권한 범위를 찾을 수 없습니다."

                        verify(exactly = 0) { mockOauthScopeJpaRepository.delete(any()) }
                    }
                }
            }
        }
    })
