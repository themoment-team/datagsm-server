package team.themoment.datagsm.web.domain.application.service

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.common.domain.application.dto.request.SearchApplicationReqDto
import team.themoment.datagsm.common.domain.application.entity.ApplicationJpaEntity
import team.themoment.datagsm.common.domain.application.repository.ApplicationJpaRepository
import team.themoment.datagsm.web.domain.application.service.impl.SearchApplicationServiceImpl

class SearchApplicationServiceTest :
    DescribeSpec({

        val mockApplicationJpaRepository = mockk<ApplicationJpaRepository>()

        val service = SearchApplicationServiceImpl(mockApplicationJpaRepository)

        afterEach {
            clearAllMocks()
        }

        describe("SearchApplicationService 클래스의") {
            describe("execute 메서드는") {

                val ownerAccount =
                    AccountJpaEntity().apply {
                        id = 1L
                        email = "owner@gsm.hs.kr"
                        role = AccountRole.USER
                    }

                val app1 =
                    ApplicationJpaEntity().apply {
                        id = "app-uuid-0001"
                        name = "My Application"
                        account = ownerAccount
                    }

                val app2 =
                    ApplicationJpaEntity().apply {
                        id = "app-uuid-0002"
                        name = "Another Application"
                        account = ownerAccount
                    }

                context("검색 조건 없이 전체 조회할 때") {
                    val queryReq = SearchApplicationReqDto(page = 0, size = 100)
                    val pageResult = PageImpl(listOf(app1, app2), PageRequest.of(0, 100), 2)

                    beforeEach {
                        every {
                            mockApplicationJpaRepository.searchApplicationWithPaging(
                                name = null,
                                id = null,
                                pageable = PageRequest.of(0, 100),
                            )
                        } returns pageResult
                    }

                    it("모든 Application 목록이 반환되어야 한다") {
                        val result = service.execute(queryReq)

                        result.totalElements shouldBe 2
                        result.totalPages shouldBe 1
                        result.applications.size shouldBe 2
                        result.applications[0].name shouldBe "My Application"
                        result.applications[1].name shouldBe "Another Application"

                        verify(exactly = 1) {
                            mockApplicationJpaRepository.searchApplicationWithPaging(null, null, PageRequest.of(0, 100))
                        }
                    }
                }

                context("이름으로 검색할 때") {
                    val queryReq = SearchApplicationReqDto(name = "My", page = 0, size = 100)
                    val pageResult = PageImpl(listOf(app1), PageRequest.of(0, 100), 1)

                    beforeEach {
                        every {
                            mockApplicationJpaRepository.searchApplicationWithPaging(
                                name = "My",
                                id = null,
                                pageable = PageRequest.of(0, 100),
                            )
                        } returns pageResult
                    }

                    it("이름이 일치하는 Application 목록이 반환되어야 한다") {
                        val result = service.execute(queryReq)

                        result.totalElements shouldBe 1
                        result.applications.size shouldBe 1
                        result.applications[0].name shouldBe "My Application"

                        verify(exactly = 1) {
                            mockApplicationJpaRepository.searchApplicationWithPaging("My", null, PageRequest.of(0, 100))
                        }
                    }
                }

                context("ID로 검색할 때") {
                    val queryReq = SearchApplicationReqDto(id = "app-uuid-0001", page = 0, size = 100)
                    val pageResult = PageImpl(listOf(app1), PageRequest.of(0, 100), 1)

                    beforeEach {
                        every {
                            mockApplicationJpaRepository.searchApplicationWithPaging(
                                name = null,
                                id = "app-uuid-0001",
                                pageable = PageRequest.of(0, 100),
                            )
                        } returns pageResult
                    }

                    it("ID가 일치하는 Application이 반환되어야 한다") {
                        val result = service.execute(queryReq)

                        result.totalElements shouldBe 1
                        result.applications[0].id shouldBe "app-uuid-0001"
                    }
                }

                context("검색 결과가 없을 때") {
                    val queryReq = SearchApplicationReqDto(name = "nonexistent", page = 0, size = 100)
                    val pageResult = PageImpl(emptyList<ApplicationJpaEntity>(), PageRequest.of(0, 100), 0)

                    beforeEach {
                        every {
                            mockApplicationJpaRepository.searchApplicationWithPaging(
                                name = "nonexistent",
                                id = null,
                                pageable = PageRequest.of(0, 100),
                            )
                        } returns pageResult
                    }

                    it("빈 목록이 반환되어야 한다") {
                        val result = service.execute(queryReq)

                        result.totalElements shouldBe 0
                        result.totalPages shouldBe 0
                        result.applications shouldBe emptyList()
                    }
                }

                context("페이지네이션 파라미터가 적용될 때") {
                    // page=0, size=1 사용 시: offset=0, 0+1=1 <= 2 이므로 PageImpl의 total 보정 없음
                    val queryReq = SearchApplicationReqDto(page = 0, size = 1)
                    val pageResult = PageImpl(listOf(app1), PageRequest.of(0, 1), 2)

                    beforeEach {
                        every {
                            mockApplicationJpaRepository.searchApplicationWithPaging(
                                name = null,
                                id = null,
                                pageable = PageRequest.of(0, 1),
                            )
                        } returns pageResult
                    }

                    it("페이지 정보가 올바르게 반환되어야 한다") {
                        val result = service.execute(queryReq)

                        result.totalElements shouldBe 2
                        result.totalPages shouldBe 2
                        result.applications.size shouldBe 1

                        verify(exactly = 1) {
                            mockApplicationJpaRepository.searchApplicationWithPaging(null, null, PageRequest.of(0, 1))
                        }
                    }
                }
            }
        }
    })
