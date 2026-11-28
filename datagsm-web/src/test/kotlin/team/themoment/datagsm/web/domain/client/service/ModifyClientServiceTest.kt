package team.themoment.datagsm.web.domain.client.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.common.domain.application.entity.ApplicationJpaEntity
import team.themoment.datagsm.common.domain.application.entity.OAuthScopeJpaEntity
import team.themoment.datagsm.common.domain.application.repository.OAuthScopeJpaRepository
import team.themoment.datagsm.common.domain.client.dto.request.ModifyClientReqDto
import team.themoment.datagsm.common.domain.client.entity.ClientJpaEntity
import team.themoment.datagsm.common.domain.client.repository.ClientJpaRepository
import team.themoment.datagsm.web.domain.client.service.impl.ModifyClientServiceImpl
import team.themoment.datagsm.web.domain.client.validator.OauthScopeValidator
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException

class ModifyClientServiceTest :
    DescribeSpec({

        lateinit var mockClientRepository: ClientJpaRepository
        lateinit var mockCurrentUserProvider: CurrentUserProvider
        lateinit var mockOAuthScopeJpaRepository: OAuthScopeJpaRepository
        lateinit var modifyClientService: ModifyClientService

        fun buildScopeList(vararg scopes: String): List<OAuthScopeJpaEntity> =
            scopes.map {
                OAuthScopeJpaEntity().apply {
                    scopeName = it.substringAfter(':')
                    description = ""
                    application = ApplicationJpaEntity().apply { id = it.substringBefore(':') }
                }
            }

        beforeEach {
            mockClientRepository = mockk<ClientJpaRepository>()
            mockCurrentUserProvider = mockk<CurrentUserProvider>()
            mockOAuthScopeJpaRepository = mockk<OAuthScopeJpaRepository>()
            modifyClientService =
                ModifyClientServiceImpl(
                    mockClientRepository,
                    mockCurrentUserProvider,
                    OauthScopeValidator(mockOAuthScopeJpaRepository),
                )
        }

        describe("ModifyClientService 클래스의") {
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
                            clientName = "기존 클라이언트"
                            serviceName = "기존 서비스"
                            account = ownerAccount
                            redirectUrls = setOf("https://example.com")
                            scopes.add("self:read")
                        }
                }

                context("소유자가 클라이언트 이름을 수정할 때") {
                    val updateRequest =
                        ModifyClientReqDto(
                            clientName = "수정된 클라이언트",
                            redirectUrls = null,
                        )

                    beforeEach {
                        every { mockClientRepository.findByIdForUpdate(clientId) } returns existingClient
                        every { mockCurrentUserProvider.getCurrentAccount() } returns ownerAccount
                    }

                    it("클라이언트 이름이 성공적으로 수정되어야 한다") {
                        val result = modifyClientService.execute(clientId, updateRequest)

                        result.id shouldBe clientId
                        result.clientName shouldBe "수정된 클라이언트"
                        result.redirectUrl shouldBe listOf("https://example.com")

                        verify(exactly = 1) { mockClientRepository.findByIdForUpdate(clientId) }
                        verify(exactly = 1) { mockCurrentUserProvider.getCurrentAccount() }
                    }

                    it("scopes를 보내지 않으면 기존 scope가 유지되고 허용 scope를 조회하지 않는다") {
                        val result = modifyClientService.execute(clientId, updateRequest)

                        result.scopes shouldBe setOf("self:read")
                        verify(exactly = 0) { mockOAuthScopeJpaRepository.findAllByApplicationIdIn(any()) }
                    }
                }

                context("소유자가 허용된 scope로 수정할 때") {
                    val updateRequest =
                        ModifyClientReqDto(
                            scopes = setOf("self:write", "other:read"),
                        )

                    beforeEach {
                        every { mockClientRepository.findByIdForUpdate(clientId) } returns existingClient
                        every { mockCurrentUserProvider.getCurrentAccount() } returns ownerAccount
                        every { mockOAuthScopeJpaRepository.findAllByApplicationIdIn(any()) } returns
                            buildScopeList("self:read", "self:write", "other:read")
                    }

                    it("기존 scope가 요청한 scope로 교체되어야 한다") {
                        val result = modifyClientService.execute(clientId, updateRequest)

                        result.scopes shouldBe setOf("self:write", "other:read")
                        existingClient.scopes shouldBe setOf("self:write", "other:read")
                        result.clientName shouldBe "기존 클라이언트"

                        verify(exactly = 1) { mockOAuthScopeJpaRepository.findAllByApplicationIdIn(any()) }
                    }
                }

                context("허용되지 않은 scope가 포함된 요청으로 수정할 때") {
                    val updateRequest =
                        ModifyClientReqDto(
                            clientName = "수정 시도",
                            scopes = setOf("self:read", "unknown:scope"),
                        )

                    beforeEach {
                        every { mockClientRepository.findByIdForUpdate(clientId) } returns existingClient
                        every { mockCurrentUserProvider.getCurrentAccount() } returns ownerAccount
                        every { mockOAuthScopeJpaRepository.findAllByApplicationIdIn(any()) } returns buildScopeList("self:read")
                    }

                    it("ExpectedException이 발생하고 클라이언트는 변경되지 않아야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                modifyClientService.execute(clientId, updateRequest)
                            }

                        exception.message shouldBe "허용되지 않는 OAuth 권한이 포함되어 있습니다."
                        existingClient.scopes shouldBe setOf("self:read")
                        existingClient.clientName shouldBe "기존 클라이언트"
                    }
                }

                context("허용 목록에서 빠진 scope를 이미 가진 클라이언트가 그 scope를 유지한 채 수정할 때") {
                    val updateRequest =
                        ModifyClientReqDto(
                            scopes = setOf("self:read", "self:write"),
                        )

                    beforeEach {
                        every { mockClientRepository.findByIdForUpdate(clientId) } returns existingClient
                        every { mockCurrentUserProvider.getCurrentAccount() } returns ownerAccount
                        every { mockOAuthScopeJpaRepository.findAllByApplicationIdIn(any()) } returns buildScopeList("self:write")
                    }

                    it("ExpectedException이 발생하고 기존 scope는 변경되지 않아야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                modifyClientService.execute(clientId, updateRequest)
                            }

                        exception.message shouldBe "허용되지 않는 OAuth 권한이 포함되어 있습니다."
                        existingClient.scopes shouldBe setOf("self:read")
                    }
                }

                context("소유자가 아닌 ADMIN 사용자가 scope 수정을 시도할 때") {
                    val adminAccount =
                        AccountJpaEntity().apply {
                            id = 3L
                            email = "admin@gsm.hs.kr"
                            role = AccountRole.ADMIN
                        }

                    val updateRequest =
                        ModifyClientReqDto(
                            scopes = setOf("self:write"),
                        )

                    beforeEach {
                        every { mockClientRepository.findByIdForUpdate(clientId) } returns existingClient
                        every { mockCurrentUserProvider.getCurrentAccount() } returns adminAccount
                        every { mockOAuthScopeJpaRepository.findAllByApplicationIdIn(any()) } returns
                            buildScopeList("self:read", "self:write")
                    }

                    it("scope가 성공적으로 교체되어야 한다") {
                        val result = modifyClientService.execute(clientId, updateRequest)

                        result.scopes shouldBe setOf("self:write")
                        existingClient.scopes shouldBe setOf("self:write")
                    }
                }

                context("소유자가 아닌 일반 사용자가 scope 수정을 시도할 때") {
                    val otherAccount =
                        AccountJpaEntity().apply {
                            id = 2L
                            email = "other@gsm.hs.kr"
                            role = AccountRole.USER
                        }

                    val updateRequest =
                        ModifyClientReqDto(
                            scopes = setOf("self:write"),
                        )

                    beforeEach {
                        every { mockClientRepository.findByIdForUpdate(clientId) } returns existingClient
                        every { mockCurrentUserProvider.getCurrentAccount() } returns otherAccount
                    }

                    it("ExpectedException이 발생하고 scope는 변경되지 않아야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                modifyClientService.execute(clientId, updateRequest)
                            }

                        exception.message shouldBe "Client 변경 권한이 없습니다."
                        existingClient.scopes shouldBe setOf("self:read")
                        verify(exactly = 0) { mockOAuthScopeJpaRepository.findAllByApplicationIdIn(any()) }
                    }
                }

                context("소유자가 redirect URL을 수정할 때") {
                    val updateRequest =
                        ModifyClientReqDto(
                            clientName = null,
                            redirectUrls = setOf("https://new-url.com", "https://another-url.com"),
                        )

                    beforeEach {
                        every { mockClientRepository.findByIdForUpdate(clientId) } returns existingClient
                        every { mockCurrentUserProvider.getCurrentAccount() } returns ownerAccount
                    }

                    it("redirect URL이 성공적으로 수정되어야 한다") {
                        val result = modifyClientService.execute(clientId, updateRequest)

                        result.clientName shouldBe "기존 클라이언트"
                        result.redirectUrl shouldBe listOf("https://new-url.com", "https://another-url.com")
                    }
                }

                context("소유자가 이름과 redirect URL을 모두 수정할 때") {
                    val updateRequest =
                        ModifyClientReqDto(
                            clientName = "전체 수정 클라이언트",
                            redirectUrls = setOf("https://updated.com"),
                        )

                    beforeEach {
                        every { mockClientRepository.findByIdForUpdate(clientId) } returns existingClient
                        every { mockCurrentUserProvider.getCurrentAccount() } returns ownerAccount
                    }

                    it("모든 정보가 성공적으로 수정되어야 한다") {
                        val result = modifyClientService.execute(clientId, updateRequest)

                        result.id shouldBe clientId
                        result.clientName shouldBe "전체 수정 클라이언트"
                        result.redirectUrl shouldBe listOf("https://updated.com")
                    }
                }

                context("존재하지 않는 클라이언트 ID로 수정을 시도할 때") {
                    val nonExistingId = "non-existing-id"
                    val updateRequest =
                        ModifyClientReqDto(
                            clientName = "수정 시도",
                        )

                    beforeEach {
                        every { mockClientRepository.findByIdForUpdate(nonExistingId) } returns null
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                modifyClientService.execute(nonExistingId, updateRequest)
                            }

                        exception.message shouldBe "Id에 해당하는 Client를 찾지 못했습니다."
                    }
                }

                context("소유자가 아닌 일반 사용자가 수정을 시도할 때") {
                    val otherAccount =
                        AccountJpaEntity().apply {
                            id = 2L
                            email = "other@gsm.hs.kr"
                            role = AccountRole.USER
                        }

                    val updateRequest =
                        ModifyClientReqDto(
                            clientName = "무단 수정 시도",
                        )

                    beforeEach {
                        every { mockClientRepository.findByIdForUpdate(clientId) } returns existingClient
                        every { mockCurrentUserProvider.getCurrentAccount() } returns otherAccount
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                modifyClientService.execute(clientId, updateRequest)
                            }

                        exception.message shouldBe "Client 변경 권한이 없습니다."

                        verify(exactly = 1) { mockCurrentUserProvider.getCurrentAccount() }
                    }
                }

                context("소유자가 아닌 ADMIN 사용자가 수정을 시도할 때") {
                    val adminAccount =
                        AccountJpaEntity().apply {
                            id = 3L
                            email = "admin@gsm.hs.kr"
                            role = AccountRole.ADMIN
                        }

                    val updateRequest =
                        ModifyClientReqDto(
                            clientName = "관리자 수정",
                        )

                    beforeEach {
                        every { mockClientRepository.findByIdForUpdate(clientId) } returns existingClient
                        every { mockCurrentUserProvider.getCurrentAccount() } returns adminAccount
                    }

                    it("클라이언트가 성공적으로 수정되어야 한다") {
                        val result = modifyClientService.execute(clientId, updateRequest)

                        result.id shouldBe clientId
                        result.clientName shouldBe "관리자 수정"
                    }
                }

                context("소유자가 아닌 ROOT 사용자가 수정을 시도할 때") {
                    val rootAccount =
                        AccountJpaEntity().apply {
                            id = 4L
                            email = "root@gsm.hs.kr"
                            role = AccountRole.ROOT
                        }

                    val updateRequest =
                        ModifyClientReqDto(
                            clientName = "루트 수정",
                        )

                    beforeEach {
                        every { mockClientRepository.findByIdForUpdate(clientId) } returns existingClient
                        every { mockCurrentUserProvider.getCurrentAccount() } returns rootAccount
                    }

                    it("클라이언트가 성공적으로 수정되어야 한다") {
                        val result = modifyClientService.execute(clientId, updateRequest)

                        result.id shouldBe clientId
                        result.clientName shouldBe "루트 수정"
                    }
                }
            }
        }
    })
