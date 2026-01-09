package team.themoment.datagsm.web.domain.client.service

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import team.themoment.datagsm.common.domain.account.AccountJpaEntity
import team.themoment.datagsm.common.domain.account.AccountRole
import team.themoment.datagsm.common.domain.client.ClientJpaEntity
import team.themoment.datagsm.common.domain.client.repository.ClientJpaRepository
import team.themoment.datagsm.web.domain.client.service.impl.SearchClientServiceImpl

class SearchClientServiceTest :
    DescribeSpec({

        val mockClientRepository = mockk<ClientJpaRepository>()

        val searchClientService = SearchClientServiceImpl(mockClientRepository)

        afterEach {
            clearAllMocks()
        }

        describe("SearchClientService 클래스의") {
            describe("execute 메서드는") {

                val testAccount =
                    AccountJpaEntity().apply {
                        id = 1L
                        email = "test@gsm.hs.kr"
                        role = AccountRole.USER
                    }

                val testClient =
                    ClientJpaEntity().apply {
                        id = "test-client-id"
                        secret = "test-secret"
                        name = "테스트 클라이언트"
                        account = testAccount
                        redirectUrls = setOf("https://test.com")
                    }

                context("클라이언트 이름으로 검색할 때") {
                    beforeEach {
                        every {
                            mockClientRepository.searchClientWithPaging(
                                name = "테스트",
                                pageable = PageRequest.of(0, 20),
                            )
                        } returns PageImpl(listOf(testClient), PageRequest.of(0, 20), 1L)
                    }

                    it("조건에 맞는 클라이언트가 반환되어야 한다") {
                        val result =
                            searchClientService.execute(
                                clientName = "테스트",
                                page = 0,
                                size = 20,
                            )

                        result.totalElements shouldBe 1L
                        result.totalPages shouldBe 1
                        result.clients.size shouldBe 1

                        val client = result.clients[0]
                        client.id shouldBe "test-client-id"
                        client.name shouldBe "테스트 클라이언트"
                        client.redirectUrl shouldBe listOf("https://test.com")

                        verify(exactly = 1) {
                            mockClientRepository.searchClientWithPaging(
                                name = "테스트",
                                pageable = PageRequest.of(0, 20),
                            )
                        }
                    }
                }

                context("검색 조건 없이 전체 클라이언트를 조회할 때") {
                    val clients =
                        (1..10).map { index ->
                            ClientJpaEntity().apply {
                                id = "client-$index"
                                secret = "secret-$index"
                                name = "클라이언트$index"
                                account = testAccount
                                redirectUrls = setOf("https://example$index.com")
                            }
                        }

                    beforeEach {
                        every {
                            mockClientRepository.searchClientWithPaging(
                                name = null,
                                pageable = PageRequest.of(0, 20),
                            )
                        } returns PageImpl(clients, PageRequest.of(0, 20), 10L)
                    }

                    it("모든 클라이언트가 반환되어야 한다") {
                        val result =
                            searchClientService.execute(
                                clientName = null,
                                page = 0,
                                size = 20,
                            )

                        result.totalElements shouldBe 10L
                        result.totalPages shouldBe 1
                        result.clients.size shouldBe 10
                        result.clients[0].name shouldBe "클라이언트1"
                        result.clients[9].name shouldBe "클라이언트10"
                    }
                }

                context("존재하지 않는 이름으로 검색할 때") {
                    beforeEach {
                        every {
                            mockClientRepository.searchClientWithPaging(
                                name = "존재하지않음",
                                pageable = PageRequest.of(0, 20),
                            )
                        } returns PageImpl(emptyList(), PageRequest.of(0, 20), 0L)
                    }

                    it("빈 결과가 반환되어야 한다") {
                        val result =
                            searchClientService.execute(
                                clientName = "존재하지않음",
                                page = 0,
                                size = 20,
                            )

                        result.totalElements shouldBe 0L
                        result.totalPages shouldBe 0
                        result.clients.size shouldBe 0
                    }
                }

                context("페이지네이션으로 여러 클라이언트를 조회할 때") {
                    val allClients =
                        (1..50).map { index ->
                            ClientJpaEntity().apply {
                                id = "client-$index"
                                secret = "secret-$index"
                                name = "클라이언트$index"
                                account = testAccount
                                redirectUrls = emptySet()
                            }
                        }

                    beforeEach {
                        val firstPageClients = allClients.take(20)
                        every {
                            mockClientRepository.searchClientWithPaging(
                                name = null,
                                pageable = PageRequest.of(0, 20),
                            )
                        } returns PageImpl(firstPageClients, PageRequest.of(0, 20), 50L)
                    }

                    it("첫 번째 페이지 결과가 반환되어야 한다") {
                        val result =
                            searchClientService.execute(
                                clientName = null,
                                page = 0,
                                size = 20,
                            )

                        result.totalElements shouldBe 50L
                        result.totalPages shouldBe 3
                        result.clients.size shouldBe 20
                        result.clients[0].name shouldBe "클라이언트1"
                        result.clients[19].name shouldBe "클라이언트20"
                    }
                }

                context("두 번째 페이지를 조회할 때") {
                    val allClients =
                        (1..50).map { index ->
                            ClientJpaEntity().apply {
                                id = "client-$index"
                                secret = "secret-$index"
                                name = "클라이언트$index"
                                account = testAccount
                                redirectUrls = emptySet()
                            }
                        }

                    beforeEach {
                        val secondPageClients = allClients.drop(20).take(20)
                        every {
                            mockClientRepository.searchClientWithPaging(
                                name = null,
                                pageable = PageRequest.of(1, 20),
                            )
                        } returns PageImpl(secondPageClients, PageRequest.of(1, 20), 50L)
                    }

                    it("두 번째 페이지 결과가 반환되어야 한다") {
                        val result =
                            searchClientService.execute(
                                clientName = null,
                                page = 1,
                                size = 20,
                            )

                        result.totalElements shouldBe 50L
                        result.totalPages shouldBe 3
                        result.clients.size shouldBe 20
                        result.clients[0].name shouldBe "클라이언트21"
                        result.clients[19].name shouldBe "클라이언트40"
                    }
                }

                context("부분 일치 검색을 할 때") {
                    val matchingClients =
                        listOf(
                            ClientJpaEntity().apply {
                                id = "client-1"
                                secret = "secret-1"
                                name = "API 클라이언트"
                                account = testAccount
                                redirectUrls = emptySet()
                            },
                            ClientJpaEntity().apply {
                                id = "client-2"
                                secret = "secret-2"
                                name = "API 서비스 클라이언트"
                                account = testAccount
                                redirectUrls = emptySet()
                            },
                        )

                    beforeEach {
                        every {
                            mockClientRepository.searchClientWithPaging(
                                name = "API",
                                pageable = PageRequest.of(0, 20),
                            )
                        } returns PageImpl(matchingClients, PageRequest.of(0, 20), 2L)
                    }

                    it("부분 일치하는 클라이언트들이 반환되어야 한다") {
                        val result =
                            searchClientService.execute(
                                clientName = "API",
                                page = 0,
                                size = 20,
                            )

                        result.totalElements shouldBe 2L
                        result.clients.size shouldBe 2
                        result.clients[0].name shouldBe "API 클라이언트"
                        result.clients[1].name shouldBe "API 서비스 클라이언트"
                    }
                }

                context("페이지 크기를 작게 설정하여 조회할 때") {
                    val clients =
                        (1..5).map { index ->
                            ClientJpaEntity().apply {
                                id = "client-$index"
                                secret = "secret-$index"
                                name = "클라이언트$index"
                                account = testAccount
                                redirectUrls = emptySet()
                            }
                        }

                    beforeEach {
                        every {
                            mockClientRepository.searchClientWithPaging(
                                name = null,
                                pageable = PageRequest.of(0, 5),
                            )
                        } returns PageImpl(clients, PageRequest.of(0, 5), 5L)
                    }

                    it("지정된 크기만큼의 결과가 반환되어야 한다") {
                        val result =
                            searchClientService.execute(
                                clientName = null,
                                page = 0,
                                size = 5,
                            )

                        result.totalElements shouldBe 5L
                        result.totalPages shouldBe 1
                        result.clients.size shouldBe 5
                    }
                }
            }
        }
    })
