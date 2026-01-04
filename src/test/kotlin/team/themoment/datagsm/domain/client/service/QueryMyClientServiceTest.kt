package team.themoment.datagsm.domain.client.service

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import team.themoment.datagsm.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.domain.client.entity.ClientJpaEntity
import team.themoment.datagsm.domain.client.repository.ClientJpaRepository
import team.themoment.datagsm.domain.client.service.impl.QueryMyClientServiceImpl
import team.themoment.datagsm.global.security.provider.CurrentUserProvider

class QueryMyClientServiceTest :
    DescribeSpec({

        val mockClientRepository = mockk<ClientJpaRepository>()
        val mockCurrentUserProvider = mockk<CurrentUserProvider>()

        val queryMyClientService = QueryMyClientServiceImpl(mockClientRepository, mockCurrentUserProvider)

        afterEach {
            clearAllMocks()
        }

        describe("QueryMyClientService 클래스의") {
            describe("execute 메서드는") {

                val currentAccount =
                    AccountJpaEntity().apply {
                        id = 1L
                        email = "user@gsm.hs.kr"
                        role = AccountRole.USER
                    }

                context("현재 사용자가 클라이언트를 하나 소유하고 있을 때") {
                    val client =
                        ClientJpaEntity().apply {
                            id = "client-1"
                            secret = "secret-1"
                            name = "나의 클라이언트"
                            account = currentAccount
                            redirectUrls = setOf("https://example.com")
                        }

                    beforeEach {
                        every { mockCurrentUserProvider.getCurrentAccount() } returns currentAccount
                        every { mockClientRepository.findAllByAccount(currentAccount) } returns listOf(client)
                    }

                    it("소유한 클라이언트 목록이 반환되어야 한다") {
                        val result = queryMyClientService.execute()

                        result.totalElements shouldBe 1L
                        result.totalPages shouldBe 1
                        result.clients.size shouldBe 1

                        val clientRes = result.clients[0]
                        clientRes.id shouldBe "client-1"
                        clientRes.name shouldBe "나의 클라이언트"
                        clientRes.redirectUrl shouldBe listOf("https://example.com")

                        verify(exactly = 1) { mockCurrentUserProvider.getCurrentAccount() }
                        verify(exactly = 1) { mockClientRepository.findAllByAccount(currentAccount) }
                    }
                }

                context("현재 사용자가 여러 개의 클라이언트를 소유하고 있을 때") {
                    val clients =
                        (1..5).map { index ->
                            ClientJpaEntity().apply {
                                id = "client-$index"
                                secret = "secret-$index"
                                name = "클라이언트$index"
                                account = currentAccount
                                redirectUrls = setOf("https://example$index.com")
                            }
                        }

                    beforeEach {
                        every { mockCurrentUserProvider.getCurrentAccount() } returns currentAccount
                        every { mockClientRepository.findAllByAccount(currentAccount) } returns clients
                    }

                    it("모든 클라이언트가 반환되어야 한다") {
                        val result = queryMyClientService.execute()

                        result.totalElements shouldBe 5L
                        result.totalPages shouldBe 1
                        result.clients.size shouldBe 5
                        result.clients[0].name shouldBe "클라이언트1"
                        result.clients[4].name shouldBe "클라이언트5"
                    }
                }

                context("현재 사용자가 소유한 클라이언트가 없을 때") {
                    beforeEach {
                        every { mockCurrentUserProvider.getCurrentAccount() } returns currentAccount
                        every { mockClientRepository.findAllByAccount(currentAccount) } returns emptyList()
                    }

                    it("빈 결과가 반환되어야 한다") {
                        val result = queryMyClientService.execute()

                        result.totalElements shouldBe 0L
                        result.totalPages shouldBe 1
                        result.clients.size shouldBe 0

                        verify(exactly = 1) { mockCurrentUserProvider.getCurrentAccount() }
                        verify(exactly = 1) { mockClientRepository.findAllByAccount(currentAccount) }
                    }
                }

                context("다양한 redirect URL을 가진 클라이언트들을 조회할 때") {
                    val client1 =
                        ClientJpaEntity().apply {
                            id = "client-1"
                            secret = "secret-1"
                            name = "멀티 리다이렉트 클라이언트"
                            account = currentAccount
                            redirectUrls = setOf("https://url1.com", "https://url2.com", "https://url3.com")
                        }

                    val client2 =
                        ClientJpaEntity().apply {
                            id = "client-2"
                            secret = "secret-2"
                            name = "단일 리다이렉트 클라이언트"
                            account = currentAccount
                            redirectUrls = setOf("https://single.com")
                        }

                    val client3 =
                        ClientJpaEntity().apply {
                            id = "client-3"
                            secret = "secret-3"
                            name = "리다이렉트 없는 클라이언트"
                            account = currentAccount
                            redirectUrls = emptySet()
                        }

                    beforeEach {
                        every { mockCurrentUserProvider.getCurrentAccount() } returns currentAccount
                        every { mockClientRepository.findAllByAccount(currentAccount) } returns listOf(client1, client2, client3)
                    }

                    it("각 클라이언트의 redirect URL이 올바르게 반환되어야 한다") {
                        val result = queryMyClientService.execute()

                        result.clients.size shouldBe 3
                        result.clients[0].redirectUrl.size shouldBe 3
                        result.clients[1].redirectUrl.size shouldBe 1
                        result.clients[2].redirectUrl.size shouldBe 0
                    }
                }

                context("관리자 계정으로 클라이언트를 조회할 때") {
                    val adminAccount =
                        AccountJpaEntity().apply {
                            id = 2L
                            email = "admin@gsm.hs.kr"
                            role = AccountRole.ADMIN
                        }

                    val adminClient =
                        ClientJpaEntity().apply {
                            id = "admin-client"
                            secret = "admin-secret"
                            name = "관리자 클라이언트"
                            account = adminAccount
                            redirectUrls = emptySet()
                        }

                    beforeEach {
                        every { mockCurrentUserProvider.getCurrentAccount() } returns adminAccount
                        every { mockClientRepository.findAllByAccount(adminAccount) } returns listOf(adminClient)
                    }

                    it("관리자가 소유한 클라이언트가 반환되어야 한다") {
                        val result = queryMyClientService.execute()

                        result.totalElements shouldBe 1L
                        result.clients[0].name shouldBe "관리자 클라이언트"
                    }
                }
            }
        }
    })
