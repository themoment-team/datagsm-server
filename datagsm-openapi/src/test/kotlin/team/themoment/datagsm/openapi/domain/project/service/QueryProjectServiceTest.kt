package team.themoment.datagsm.openapi.domain.project.service

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
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.common.domain.student.entity.StudentNumber
import team.themoment.datagsm.common.domain.student.entity.constant.Major
import team.themoment.datagsm.common.domain.student.entity.constant.Sex
import team.themoment.datagsm.openapi.domain.project.service.impl.QueryProjectServiceImpl

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
                                pageable = PageRequest.of(0, 100),
                                sortBy = any(),
                                sortDirection = any(),
                            )
                        } returns PageImpl(listOf(testProject), PageRequest.of(0, 100), 1L)
                    }

                    it("해당 프로젝트 정보가 반환되어야 한다") {
                        val queryReq = QueryProjectReqDto(projectId = 1L)
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
                                pageable = PageRequest.of(0, 100),
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
                                pageable = PageRequest.of(0, 100),
                                sortBy = any(),
                                sortDirection = any(),
                            )
                        } returns PageImpl(listOf(testProject), PageRequest.of(0, 100), 1L)
                    }

                    it("이름에 해당하는 프로젝트가 반환되어야 한다") {
                        val queryReq = QueryProjectReqDto(projectName = "DataGSM")
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
                                pageable = PageRequest.of(0, 100),
                                sortBy = any(),
                                sortDirection = any(),
                            )
                        } returns PageImpl(listOf(testProject), PageRequest.of(0, 100), 1L)
                    }

                    it("해당 동아리의 프로젝트가 반환되어야 한다") {
                        val queryReq = QueryProjectReqDto(clubId = 1L)
                        val result = queryProjectService.execute(queryReq)

                        result.totalElements shouldBe 1L
                        result.projects[0].club?.id shouldBe 1L
                        result.projects[0].club?.name shouldBe "SW개발동아리"
                    }
                }

                context("status=ENDED 필터로 종료된 프로젝트를 검색할 때") {
                    val endedProject =
                        ProjectJpaEntity().apply {
                            id = 5L
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
                                pageable = PageRequest.of(0, 100),
                                sortBy = any(),
                                sortDirection = any(),
                            )
                        } returns PageImpl(listOf(endedProject), PageRequest.of(0, 100), 1L)
                    }

                    it("종료된 프로젝트가 반환되어야 한다") {
                        val queryReq = QueryProjectReqDto(status = ProjectStatus.ENDED)
                        val result = queryProjectService.execute(queryReq)

                        result.totalElements shouldBe 1L
                        result.projects[0].status shouldBe ProjectStatus.ENDED
                        result.projects[0].endYear shouldBe 2023
                    }
                }

                context("참여 학생이 있는 프로젝트를 조회할 때") {
                    val student =
                        StudentJpaEntity().apply {
                            id = 100L
                            name = "홍길동"
                            email = "hong@gsm.hs.kr"
                            studentNumber = StudentNumber(1, 1, 1)
                            major = Major.SW_DEVELOPMENT
                            sex = Sex.MAN
                        }
                    val projectWithParticipants =
                        ProjectJpaEntity().apply {
                            id = 2L
                            name = "참여자있는프로젝트"
                            description = "학생들이 참여하는 프로젝트"
                            startYear = 2024
                            status = ProjectStatus.ACTIVE
                            club = testClub
                            participants = mutableSetOf(student)
                        }

                    beforeEach {
                        every {
                            mockProjectRepository.searchProjectWithPaging(
                                id = 2L,
                                name = null,
                                clubId = null,
                                status = ProjectStatus.ACTIVE,
                                pageable = PageRequest.of(0, 100),
                                sortBy = any(),
                                sortDirection = any(),
                            )
                        } returns PageImpl(listOf(projectWithParticipants), PageRequest.of(0, 100), 1L)
                    }

                    it("참여 학생 정보가 DTO로 매핑되어야 한다") {
                        val queryReq = QueryProjectReqDto(projectId = 2L)
                        val result = queryProjectService.execute(queryReq)

                        result.projects.size shouldBe 1
                        val project = result.projects[0]
                        project.participants.size shouldBe 1
                        project.participants[0].id shouldBe 100L
                        project.participants[0].name shouldBe "홍길동"
                        project.participants[0].email shouldBe "hong@gsm.hs.kr"
                        project.participants[0].major shouldBe Major.SW_DEVELOPMENT
                        project.participants[0].sex shouldBe Sex.MAN
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
                                pageable = PageRequest.of(0, 100),
                                sortBy = any(),
                                sortDirection = any(),
                            )
                        } returns PageImpl(emptyList(), PageRequest.of(0, 100), 0L)
                    }

                    it("빈 결과가 반환되어야 한다") {
                        val queryReq = QueryProjectReqDto(projectId = 999L)
                        val result = queryProjectService.execute(queryReq)

                        result.totalElements shouldBe 0L
                        result.totalPages shouldBe 0
                        result.projects.size shouldBe 0
                    }
                }

                context("동아리가 없는 프로젝트를 조회할 때") {
                    val projectWithoutClub =
                        ProjectJpaEntity().apply {
                            id = 3L
                            name = "독립프로젝트"
                            description = "동아리에 속하지 않은 프로젝트"
                            startYear = 2024
                            status = ProjectStatus.ACTIVE
                            club = null
                        }

                    beforeEach {
                        every {
                            mockProjectRepository.searchProjectWithPaging(
                                id = 3L,
                                name = null,
                                clubId = null,
                                status = ProjectStatus.ACTIVE,
                                pageable = PageRequest.of(0, 100),
                                sortBy = any(),
                                sortDirection = any(),
                            )
                        } returns PageImpl(listOf(projectWithoutClub), PageRequest.of(0, 100), 1L)
                    }

                    it("club 필드가 null인 프로젝트 정보가 반환되어야 한다") {
                        val queryReq = QueryProjectReqDto(projectId = 3L)
                        val result = queryProjectService.execute(queryReq)

                        result.projects.size shouldBe 1
                        result.projects[0].club shouldBe null
                    }
                }
            }
        }
    })
