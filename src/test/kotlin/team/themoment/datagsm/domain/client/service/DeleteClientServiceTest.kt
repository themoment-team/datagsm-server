package team.themoment.datagsm.domain.client.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.security.core.Authentication
import team.themoment.datagsm.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.domain.auth.entity.constant.ApiScope
import team.themoment.datagsm.domain.client.entity.ClientJpaEntity
import team.themoment.datagsm.domain.client.repository.ClientJpaRepository
import team.themoment.datagsm.domain.client.service.impl.DeleteClientServiceImpl
import team.themoment.datagsm.global.exception.error.ExpectedException
import team.themoment.datagsm.global.security.checker.ScopeChecker
import team.themoment.datagsm.global.security.provider.CurrentUserProvider
import java.util.Optional

class DeleteClientServiceTest :
    DescribeSpec({

        lateinit var mockClientRepository: ClientJpaRepository
        lateinit var mockCurrentUserProvider: CurrentUserProvider
        lateinit var mockScopeChecker: ScopeChecker
        lateinit var deleteClientService: DeleteClientService

        beforeEach {
            mockClientRepository = mockk<ClientJpaRepository>()
            mockCurrentUserProvider = mockk<CurrentUserProvider>()
            mockScopeChecker = mockk<ScopeChecker>()
            deleteClientService =
                DeleteClientServiceImpl(
                    mockClientRepository,
                    mockCurrentUserProvider,
                    mockScopeChecker,
                )
        }

        describe("DeleteClientService 클래스의") {
            describe("execute 메서드는") {

                val clientId = "test-client-id"
                lateinit var existingClient: ClientJpaEntity
                lateinit var ownerAccount: AccountJpaEntity

                beforeEach {
                    ownerAccount =
                        AccountJpaEntity().apply {
                            id = 1L
                            email = "owner@gsm.hs.kr"
                            role = AccountRole.USER
                        }

                    existingClient =
                        ClientJpaEntity().apply {
                            id = clientId
                            secret = "encoded-secret"
                            name = "테스트 클라이언트"
                            account = ownerAccount
                            redirectUrl = listOf("https://example.com")
                        }
                }

                context("소유자가 자신의 클라이언트를 삭제할 때") {

                    beforeEach {
                        every { mockClientRepository.findById(clientId) } returns Optional.of(existingClient)
                        every { mockCurrentUserProvider.getCurrentAccount() } returns ownerAccount
                        every { mockClientRepository.delete(existingClient) } returns Unit
                    }

                    it("클라이언트가 성공적으로 삭제되어야 한다") {
                        deleteClientService.execute(clientId)

                        verify(exactly = 1) { mockClientRepository.findById(clientId) }
                        verify(exactly = 1) { mockCurrentUserProvider.getCurrentAccount() }
                        verify(exactly = 1) { mockClientRepository.delete(existingClient) }
                    }
                }

                context("존재하지 않는 클라이언트 ID로 삭제를 시도할 때") {

                    val nonExistingId = "non-existing-id"

                    beforeEach {
                        every { mockClientRepository.findById(nonExistingId) } returns Optional.empty()
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                deleteClientService.execute(nonExistingId)
                            }

                        exception.message shouldBe "Id에 해당하는 Client를 찾지 못했습니다."

                        verify(exactly = 1) { mockClientRepository.findById("non-existing-id") }
                        verify(exactly = 0) { mockClientRepository.delete(any()) }
                    }
                }

                context("소유자가 아닌 일반 사용자가 삭제를 시도할 때") {
                    val otherAccount =
                        AccountJpaEntity().apply {
                            id = 2L
                            email = "other@gsm.hs.kr"
                            role = AccountRole.USER
                        }

                    val mockAuthentication = mockk<Authentication>()

                    beforeEach {
                        every { mockClientRepository.findById(clientId) } returns Optional.of(existingClient)
                        every { mockCurrentUserProvider.getCurrentAccount() } returns otherAccount
                        every { mockCurrentUserProvider.getAuthentication() } returns mockAuthentication
                        every {
                            mockScopeChecker.hasScope(mockAuthentication, ApiScope.CLIENT_MANAGE.scope)
                        } returns false
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                deleteClientService.execute(clientId)
                            }

                        exception.message shouldBe "Client 삭제 권한이 없습니다."

                        verify(exactly = 1) { mockCurrentUserProvider.getCurrentAccount() }
                        verify(exactly = 1) { mockScopeChecker.hasScope(mockAuthentication, ApiScope.CLIENT_MANAGE.scope) }
                        verify(exactly = 0) { mockClientRepository.delete(any()) }
                    }
                }

                context("CLIENT_MANAGE 권한을 가진 사용자가 타인의 클라이언트를 삭제할 때") {
                    val adminAccount =
                        AccountJpaEntity().apply {
                            id = 3L
                            email = "admin@gsm.hs.kr"
                            role = AccountRole.ADMIN
                        }

                    val mockAuthentication = mockk<Authentication>()

                    beforeEach {
                        every { mockClientRepository.findById(clientId) } returns Optional.of(existingClient)
                        every { mockCurrentUserProvider.getCurrentAccount() } returns adminAccount
                        every { mockCurrentUserProvider.getAuthentication() } returns mockAuthentication
                        every {
                            mockScopeChecker.hasScope(mockAuthentication, ApiScope.CLIENT_MANAGE.scope)
                        } returns true
                        every { mockClientRepository.delete(existingClient) } returns Unit
                    }

                    it("클라이언트가 성공적으로 삭제되어야 한다") {
                        deleteClientService.execute(clientId)

                        verify(exactly = 1) { mockScopeChecker.hasScope(mockAuthentication, ApiScope.CLIENT_MANAGE.scope) }
                        verify(exactly = 1) { mockClientRepository.delete(existingClient) }
                    }
                }
            }
        }
    })
