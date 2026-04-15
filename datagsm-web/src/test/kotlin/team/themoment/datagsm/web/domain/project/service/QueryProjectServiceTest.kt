package team.themoment.datagsm.web.domain.project.service

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import team.themoment.datagsm.common.domain.club.entity.ClubJpaEntity
import team.themoment.datagsm.common.domain.club.entity.constant.ClubType
import team.themoment.datagsm.common.domain.project.dto.request.QueryProjectReqDto
import team.themoment.datagsm.common.domain.project.entity.ProjectJpaEntity
import team.themoment.datagsm.common.domain.project.entity.constant.ProjectStatus
import team.themoment.datagsm.common.domain.project.repository.ProjectJpaRepository
import team.themoment.datagsm.web.domain.project.service.impl.QueryProjectServiceImpl

class QueryProjectServiceTest :
    DescribeSpec({

        val mockProjectRepository = mockk<ProjectJpaRepository>()

        val queryProjectService = QueryProjectServiceImpl(mockProjectRepository)

        afterEach {
            clearAllMocks()
        }

        describe("QueryProjectService 클래스의") {
            describe("execute 메서드는") {

                val testClub =
                    ClubJpaEntity().apply {
                        id = 1L
                        name = "SW개발동아리"
                        type = ClubType.MAJOR_CLUB
                    }

                val testProject =
                    ProjectJpaEntity().apply {
                        id = 1L
                        name = "DataGSM 프로젝트"
                        description = "학교 데이터를 제공하는 API 서비스"
                        startYear = 2024
                        status = ProjectStatus.ACTIVE
                        club = testClub
                    }

                context("존재하는 프로젝트 ID로 검색할 때") {
                    beforeEach {
                        every {
                            mockProjectRepository.searchProjectWithPaging(
                                id = 1L,
                                name = null,
                                clubId = null,
                                status = ProjectStatus.ACTIVE,
                                pageable = PageRequest.of(0, 20),
                                sortBy = any(),
                                sortDirection = any(),
                            )
                        } returns PageImpl(listOf(testProject), PageRequest.of(0, 20), 1L)
                    }

                    it("해당 프로젝트 정보가 반환되어야 한다") {
                        val queryReq =
                            QueryProjectReqDto(
                                projectId = 1L,
                                page = 0,
                                size = 20,
                            )
                        val result = queryProjectService.execute(queryReq)

                        result.totalElements shouldBe 1L
                        result.totalPages shouldBe 1
                        result.projects.size shouldBe 1

                        val project = result.projects[0]
                        project.id shouldBe 1L
                        project.name shouldBe "DataGSM 프로젝트"
                        project.description shouldBe "학교 데이터를 제공하는 API 서비스"
                        project.startYear shouldBe 2024
                        project.endYear shouldBe null
                        project.status shouldBe ProjectStatus.ACTIVE
                        project.club?.id shouldBe 1L
                        project.club?.name shouldBe "SW개발동아리"
                        project.club?.type shouldBe ClubType.MAJOR_CLUB
                        project.participants shouldBe emptyList()

                        verify(exactly = 1) {
                            mockProjectRepository.searchProjectWithPaging(
                                id = 1L,
                                name = null,
                                clubId = null,
                                status = ProjectStatus.ACTIVE,
                                pageable = PageRequest.of(0, 20),
                                sortBy = any(),
                                sortDirection = any(),
                            )
                        }
                    }
                }

                context("프로젝트 이름으로 검색할 때") {
                    beforeEach {
                        every {
                            mockProjectRepository.searchProjectWithPaging(
                                id = null,
                                name = "DataGSM",
                                clubId = null,
                                status = ProjectStatus.ACTIVE,
                                pageable = PageRequest.of(0, 20),
                                sortBy = any(),
                                sortDirection = any(),
                            )
                        } returns PageImpl(listOf(testProject), PageRequest.of(0, 20), 1L)
                    }

                    it("이름에 해당하는 프로젝트가 반환되어야 한다") {
                        val queryReq =
                            QueryProjectReqDto(
                                projectName = "DataGSM",
                                page = 0,
                                size = 20,
                            )
                        val result = queryProjectService.execute(queryReq)

                        result.totalElements shouldBe 1L
                        result.projects[0].name shouldBe "DataGSM 프로젝트"
                    }
                }

                context("동아리 ID로 검색할 때") {
                    beforeEach {
                        every {
                            mockProjectRepository.searchProjectWithPaging(
                                id = null,
                                name = null,
                                clubId = 1L,
                                status = ProjectStatus.ACTIVE,
                                pageable = PageRequest.of(0, 20),
                                sortBy = any(),
                                sortDirection = any(),
                            )
                        } returns PageImpl(listOf(testProject), PageRequest.of(0, 20), 1L)
                    }

                    it("해당 동아리의 프로젝트가 반환되어야 한다") {
                        val queryReq =
                            QueryProjectReqDto(
                                clubId = 1L,
                                page = 0,
                                size = 20,
                            )
                        val result = queryProjectService.execute(queryReq)

                        result.totalElements shouldBe 1L
                        result.projects[0].club?.id shouldBe 1L
                        result.projects[0].club?.name shouldBe "SW개발동아리"
                    }
                }

                context("status=ENDED 필터로 종료된 프로젝트를 검색할 때") {
                    val endedProject =
                        ProjectJpaEntity().apply {
                            id = 2L
                            name = "종료된 프로젝트"
                            description = "운영이 종료된 프로젝트"
                            startYear = 2022
                            endYear = 2023
                            status = ProjectStatus.ENDED
                        }

                    beforeEach {
                        every {
                            mockProjectRepository.searchProjectWithPaging(
                                id = null,
                                name = null,
                                clubId = null,
                                status = ProjectStatus.ENDED,
                                pageable = PageRequest.of(0, 20),
                                sortBy = any(),
                                sortDirection = any(),
                            )
                        } returns PageImpl(listOf(endedProject), PageRequest.of(0, 20), 1L)
                    }

                    it("종료된 프로젝트가 반환되어야 한다") {
                        val queryReq =
                            QueryProjectReqDto(
                                status = ProjectStatus.ENDED,
                                page = 0,
                                size = 20,
                            )
                        val result = queryProjectService.execute(queryReq)

                        result.totalElements shouldBe 1L
                        result.projects[0].status shouldBe ProjectStatus.ENDED
                        result.projects[0].endYear shouldBe 2023
                    }
                }

                context("프로젝트 이름과 동아리 ID로 다중 조건 검색할 때") {
                    beforeEach {
                        every {
                            mockProjectRepository.searchProjectWithPaging(
                                id = null,
                                name = "DataGSM",
                                clubId = 1L,
                                status = ProjectStatus.ACTIVE,
                                pageable = PageRequest.of(0, 20),
                                sortBy = any(),
                                sortDirection = any(),
                            )
                        } returns PageImpl(listOf(testProject), PageRequest.of(0, 20), 1L)
                    }

                    it("조건에 맞는 프로젝트 목록이 반환되어야 한다") {
                        val queryReq =
                            QueryProjectReqDto(
                                projectName = "DataGSM",
                                clubId = 1L,
                                page = 0,
                                size = 20,
                            )
                        val result = queryProjectService.execute(queryReq)

                        result.totalElements shouldBe 1L
                        result.projects[0].name shouldBe "DataGSM 프로젝트"
                        result.projects[0].club?.id shouldBe 1L
                    }
                }

                context("존재하지 않는 조건으로 검색할 때") {
                    beforeEach {
                        every {
                            mockProjectRepository.searchProjectWithPaging(
                                id = 999L,
                                name = null,
                                clubId = null,
                                status = ProjectStatus.ACTIVE,
                                pageable = PageRequest.of(0, 20),
                                sortBy = any(),
                                sortDirection = any(),
                            )
                        } returns PageImpl(emptyList(), PageRequest.of(0, 20), 0L)
                    }

                    it("빈 결과가 반환되어야 한다") {
                        val queryReq =
                            QueryProjectReqDto(
                                projectId = 999L,
                                page = 0,
                                size = 20,
                            )
                        val result = queryProjectService.execute(queryReq)

                        result.totalElements shouldBe 0L
                        result.totalPages shouldBe 0
                        result.projects.size shouldBe 0
                    }
                }

                context("여러 프로젝트를 페이징하여 검색할 때") {
                    val club2 =
                        ClubJpaEntity().apply {
                            id = 2L
                            name = "자율동아리"
                            type = ClubType.AUTONOMOUS_CLUB
                        }

                    val project2 =
                        ProjectJpaEntity().apply {
                            id = 2L
                            name = "모바일앱 프로젝트"
                            description = "학생 편의 모바일 앱"
                            startYear = 2025
                            status = ProjectStatus.ACTIVE
                            club = club2
                        }

                    beforeEach {
                        every {
                            mockProjectRepository.searchProjectWithPaging(
                                id = null,
                                name = null,
                                clubId = null,
                                status = ProjectStatus.ACTIVE,
                                pageable = PageRequest.of(0, 10),
                                sortBy = any(),
                                sortDirection = any(),
                            )
                        } returns PageImpl(listOf(testProject, project2), PageRequest.of(0, 10), 2L)
                    }

                    it("페이징된 프로젝트 목록이 반환되어야 한다") {
                        val queryReq =
                            QueryProjectReqDto(
                                page = 0,
                                size = 10,
                            )
                        val result = queryProjectService.execute(queryReq)

                        result.totalElements shouldBe 2L
                        result.totalPages shouldBe 1
                        result.projects.size shouldBe 2
                        result.projects[0].name shouldBe "DataGSM 프로젝트"
                        result.projects[1].name shouldBe "모바일앱 프로젝트"

                        verify(exactly = 1) {
                            mockProjectRepository.searchProjectWithPaging(
                                id = null,
                                name = null,
                                clubId = null,
                                status = ProjectStatus.ACTIVE,
                                pageable = PageRequest.of(0, 10),
                                sortBy = any(),
                                sortDirection = any(),
                            )
                        }
                    }
                }

                context("다양한 동아리 타입의 프로젝트를 검색할 때") {
                    val majorClub =
                        ClubJpaEntity().apply {
                            id = 3L
                            name = "전공동아리"
                            type = ClubType.MAJOR_CLUB
                        }

                    val majorProject =
                        ProjectJpaEntity().apply {
                            id = 3L
                            name = "취업 포트폴리오"
                            description = "전공 동아리 프로젝트"
                            startYear = 2024
                            status = ProjectStatus.ACTIVE
                            club = majorClub
                        }

                    beforeEach {
                        every {
                            mockProjectRepository.searchProjectWithPaging(
                                id = null,
                                name = null,
                                clubId = 3L,
                                status = ProjectStatus.ACTIVE,
                                pageable = PageRequest.of(0, 20),
                                sortBy = any(),
                                sortDirection = any(),
                            )
                        } returns PageImpl(listOf(majorProject), PageRequest.of(0, 20), 1L)
                    }

                    it("전공동아리의 프로젝트가 정상적으로 조회되어야 한다") {
                        val queryReq =
                            QueryProjectReqDto(
                                clubId = 3L,
                                page = 0,
                                size = 20,
                            )
                        val result = queryProjectService.execute(queryReq)

                        result.totalElements shouldBe 1L
                        result.projects[0].club?.type shouldBe ClubType.MAJOR_CLUB
                        result.projects[0].club?.name shouldBe "전공동아리"
                    }
                }
            }
        }
    })
