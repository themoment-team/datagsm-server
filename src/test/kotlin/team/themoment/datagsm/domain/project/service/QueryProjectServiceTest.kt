package team.themoment.datagsm.domain.project.service

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import team.themoment.datagsm.domain.club.entity.ClubJpaEntity
import team.themoment.datagsm.domain.club.entity.constant.ClubType
import team.themoment.datagsm.domain.project.entity.ProjectJpaEntity
import team.themoment.datagsm.domain.project.repository.ProjectJpaRepository
import team.themoment.datagsm.domain.project.service.impl.QueryProjectServiceImpl

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
                        clubId = 1L
                        clubName = "SW개발동아리"
                        clubType = ClubType.MAJOR_CLUB
                    }

                val testProject =
                    ProjectJpaEntity().apply {
                        projectId = 1L
                        projectName = "DataGSM 프로젝트"
                        projectDescription = "학교 데이터를 제공하는 API 서비스"
                        projectOwnerClub = testClub
                    }

                context("존재하는 프로젝트 ID로 검색할 때") {
                    beforeEach {
                        every {
                            mockProjectRepository.searchProjectWithPaging(
                                projectId = 1L,
                                projectName = null,
                                clubId = null,
                                pageable = PageRequest.of(0, 20),
                            )
                        } returns PageImpl(listOf(testProject), PageRequest.of(0, 20), 1L)
                    }

                    it("해당 프로젝트 정보가 반환되어야 한다") {
                        val result =
                            queryProjectService.execute(
                                projectId = 1L,
                                projectName = null,
                                clubId = null,
                                page = 0,
                                size = 20,
                            )

                        result.totalElements shouldBe 1L
                        result.totalPages shouldBe 1
                        result.projects.size shouldBe 1

                        val project = result.projects[0]
                        project.projectId shouldBe 1L
                        project.projectName shouldBe "DataGSM 프로젝트"
                        project.projectDescription shouldBe "학교 데이터를 제공하는 API 서비스"
                        project.projectOwnerClub.clubId shouldBe 1L
                        project.projectOwnerClub.clubName shouldBe "SW개발동아리"
                        project.projectOwnerClub.clubType shouldBe ClubType.MAJOR_CLUB

                        verify(exactly = 1) {
                            mockProjectRepository.searchProjectWithPaging(
                                projectId = 1L,
                                projectName = null,
                                clubId = null,
                                pageable = PageRequest.of(0, 20),
                            )
                        }
                    }
                }

                context("프로젝트 이름으로 검색할 때") {
                    beforeEach {
                        every {
                            mockProjectRepository.searchProjectWithPaging(
                                projectId = null,
                                projectName = "DataGSM",
                                clubId = null,
                                pageable = PageRequest.of(0, 20),
                            )
                        } returns PageImpl(listOf(testProject), PageRequest.of(0, 20), 1L)
                    }

                    it("이름에 해당하는 프로젝트가 반환되어야 한다") {
                        val result =
                            queryProjectService.execute(
                                projectId = null,
                                projectName = "DataGSM",
                                clubId = null,
                                page = 0,
                                size = 20,
                            )

                        result.totalElements shouldBe 1L
                        result.projects[0].projectName shouldBe "DataGSM 프로젝트"
                    }
                }

                context("동아리 ID로 검색할 때") {
                    beforeEach {
                        every {
                            mockProjectRepository.searchProjectWithPaging(
                                projectId = null,
                                projectName = null,
                                clubId = 1L,
                                pageable = PageRequest.of(0, 20),
                            )
                        } returns PageImpl(listOf(testProject), PageRequest.of(0, 20), 1L)
                    }

                    it("해당 동아리의 프로젝트가 반환되어야 한다") {
                        val result =
                            queryProjectService.execute(
                                projectId = null,
                                projectName = null,
                                clubId = 1L,
                                page = 0,
                                size = 20,
                            )

                        result.totalElements shouldBe 1L
                        result.projects[0].projectOwnerClub.clubId shouldBe 1L
                        result.projects[0].projectOwnerClub.clubName shouldBe "SW개발동아리"
                    }
                }

                context("프로젝트 이름과 동아리 ID로 다중 조건 검색할 때") {
                    beforeEach {
                        every {
                            mockProjectRepository.searchProjectWithPaging(
                                projectId = null,
                                projectName = "DataGSM",
                                clubId = 1L,
                                pageable = PageRequest.of(0, 20),
                            )
                        } returns PageImpl(listOf(testProject), PageRequest.of(0, 20), 1L)
                    }

                    it("조건에 맞는 프로젝트 목록이 반환되어야 한다") {
                        val result =
                            queryProjectService.execute(
                                projectId = null,
                                projectName = "DataGSM",
                                clubId = 1L,
                                page = 0,
                                size = 20,
                            )

                        result.totalElements shouldBe 1L
                        result.projects[0].projectName shouldBe "DataGSM 프로젝트"
                        result.projects[0].projectOwnerClub.clubId shouldBe 1L
                    }
                }

                context("존재하지 않는 조건으로 검색할 때") {
                    beforeEach {
                        every {
                            mockProjectRepository.searchProjectWithPaging(
                                projectId = 999L,
                                projectName = null,
                                clubId = null,
                                pageable = PageRequest.of(0, 20),
                            )
                        } returns PageImpl(emptyList(), PageRequest.of(0, 20), 0L)
                    }

                    it("빈 결과가 반환되어야 한다") {
                        val result =
                            queryProjectService.execute(
                                projectId = 999L,
                                projectName = null,
                                clubId = null,
                                page = 0,
                                size = 20,
                            )

                        result.totalElements shouldBe 0L
                        result.totalPages shouldBe 0
                        result.projects.size shouldBe 0
                    }
                }

                context("여러 프로젝트를 페이징하여 검색할 때") {
                    val club2 =
                        ClubJpaEntity().apply {
                            clubId = 2L
                            clubName = "자율동아리"
                            clubType = ClubType.AUTONOMOUS_CLUB
                        }

                    val project2 =
                        ProjectJpaEntity().apply {
                            projectId = 2L
                            projectName = "모바일앱 프로젝트"
                            projectDescription = "학생 편의 모바일 앱"
                            projectOwnerClub = club2
                        }

                    beforeEach {
                        every {
                            mockProjectRepository.searchProjectWithPaging(
                                projectId = null,
                                projectName = null,
                                clubId = null,
                                pageable = PageRequest.of(0, 10),
                            )
                        } returns PageImpl(listOf(testProject, project2), PageRequest.of(0, 10), 2L)
                    }

                    it("페이징된 프로젝트 목록이 반환되어야 한다") {
                        val result =
                            queryProjectService.execute(
                                projectId = null,
                                projectName = null,
                                clubId = null,
                                page = 0,
                                size = 10,
                            )

                        result.totalElements shouldBe 2L
                        result.totalPages shouldBe 1
                        result.projects.size shouldBe 2
                        result.projects[0].projectName shouldBe "DataGSM 프로젝트"
                        result.projects[1].projectName shouldBe "모바일앱 프로젝트"

                        verify(exactly = 1) {
                            mockProjectRepository.searchProjectWithPaging(
                                projectId = null,
                                projectName = null,
                                clubId = null,
                                pageable = PageRequest.of(0, 10),
                            )
                        }
                    }
                }

                context("다양한 동아리 타입의 프로젝트를 검색할 때") {
                    val jobClub =
                        ClubJpaEntity().apply {
                            clubId = 3L
                            clubName = "취업동아리"
                            clubType = ClubType.JOB_CLUB
                        }

                    val jobProject =
                        ProjectJpaEntity().apply {
                            projectId = 3L
                            projectName = "취업 포트폴리오"
                            projectDescription = "취업 준비 프로젝트"
                            projectOwnerClub = jobClub
                        }

                    beforeEach {
                        every {
                            mockProjectRepository.searchProjectWithPaging(
                                projectId = null,
                                projectName = null,
                                clubId = 3L,
                                pageable = PageRequest.of(0, 20),
                            )
                        } returns PageImpl(listOf(jobProject), PageRequest.of(0, 20), 1L)
                    }

                    it("취업동아리의 프로젝트가 정상적으로 조회되어야 한다") {
                        val result =
                            queryProjectService.execute(
                                projectId = null,
                                projectName = null,
                                clubId = 3L,
                                page = 0,
                                size = 20,
                            )

                        result.totalElements shouldBe 1L
                        result.projects[0].projectOwnerClub.clubType shouldBe ClubType.JOB_CLUB
                        result.projects[0].projectOwnerClub.clubName shouldBe "취업동아리"
                    }
                }
            }
        }
    })
