package team.themoment.datagsm.web.domain.client.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.common.domain.client.entity.ClientJpaEntity
import team.themoment.datagsm.common.domain.client.repository.ClientJpaRepository
import team.themoment.datagsm.common.dto.client.request.ModifyClientReqDto
import team.themoment.datagsm.web.domain.client.service.impl.ModifyClientServiceImpl
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException
import java.util.Optional

class ModifyClientServiceTest :
    DescribeSpec({

        lateinit var mockClientRepository: ClientJpaRepository
        lateinit var mockCurrentUserProvider: CurrentUserProvider
        lateinit var modifyClientService: ModifyClientService

        beforeEach {
            mockClientRepository = mockk<ClientJpaRepository>()
            mockCurrentUserProvider = mockk<CurrentUserProvider>()
            modifyClientService =
                ModifyClientServiceImpl(
                    mockClientRepository,
                    mockCurrentUserProvider,
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
                            name = "기존 클라이언트"
                            account = ownerAccount
                            redirectUrls = setOf("https://example.com")
                        }
                }

                context("소유자가 클라이언트 이름을 수정할 때") {
                    val updateRequest =
                        ModifyClientReqDto(
                            name = "수정된 클라이언트",
                            redirectUrls = null,
                        )

                    beforeEach {
                        every { mockClientRepository.findById(clientId) } returns Optional.of(existingClient)
                        every { mockCurrentUserProvider.getCurrentAccount() } returns ownerAccount
                    }

                    it("클라이언트 이름이 성공적으로 수정되어야 한다") {
                        val result = modifyClientService.execute(clientId, updateRequest)

                        result.id shouldBe clientId
                        result.name shouldBe "수정된 클라이언트"
                        result.redirectUrl shouldBe listOf("https://example.com")

                        verify(exactly = 1) { mockClientRepository.findById(clientId) }
                        verify(exactly = 1) { mockCurrentUserProvider.getCurrentAccount() }
                    }
                }

                context("소유자가 redirect URL을 수정할 때") {
                    val updateRequest =
                        ModifyClientReqDto(
                            name = null,
                            redirectUrls = setOf("https://new-url.com", "https://another-url.com"),
                        )

                    beforeEach {
                        every { mockClientRepository.findById(clientId) } returns Optional.of(existingClient)
                        every { mockCurrentUserProvider.getCurrentAccount() } returns ownerAccount
                    }

                    it("redirect URL이 성공적으로 수정되어야 한다") {
                        val result = modifyClientService.execute(clientId, updateRequest)

                        result.name shouldBe "기존 클라이언트"
                        result.redirectUrl shouldBe listOf("https://new-url.com", "https://another-url.com")
                    }
                }

                context("소유자가 이름과 redirect URL을 모두 수정할 때") {
                    val updateRequest =
                        ModifyClientReqDto(
                            name = "전체 수정 클라이언트",
                            redirectUrls = setOf("https://updated.com"),
                        )

                    beforeEach {
                        every { mockClientRepository.findById(clientId) } returns Optional.of(existingClient)
                        every { mockCurrentUserProvider.getCurrentAccount() } returns ownerAccount
                    }

                    it("모든 정보가 성공적으로 수정되어야 한다") {
                        val result = modifyClientService.execute(clientId, updateRequest)

                        result.id shouldBe clientId
                        result.name shouldBe "전체 수정 클라이언트"
                        result.redirectUrl shouldBe listOf("https://updated.com")
                    }
                }

                context("존재하지 않는 클라이언트 ID로 수정을 시도할 때") {
                    val nonExistingId = "non-existing-id"
                    val updateRequest =
                        ModifyClientReqDto(
                            name = "수정 시도",
                        )

                    beforeEach {
                        every { mockClientRepository.findById(nonExistingId) } returns Optional.empty()
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
                            name = "무단 수정 시도",
                        )

                    beforeEach {
                        every { mockClientRepository.findById(clientId) } returns Optional.of(existingClient)
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
            }
        }
    })
