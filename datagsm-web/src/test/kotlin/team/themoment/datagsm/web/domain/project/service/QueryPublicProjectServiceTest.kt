package team.themoment.datagsm.web.domain.project.service

import io.kotest.assertions.throwables.shouldThrow
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
import team.themoment.datagsm.common.domain.project.dto.request.QueryPublicProjectReqDto
import team.themoment.datagsm.common.domain.project.entity.ProjectJpaEntity
import team.themoment.datagsm.common.domain.project.entity.constant.ProjectStatus
import team.themoment.datagsm.common.domain.project.repository.ProjectJpaRepository
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.common.domain.student.entity.constant.Major
import team.themoment.datagsm.common.domain.student.entity.constant.Sex
import team.themoment.datagsm.web.domain.project.mapper.ProjectEditRequestMapper
import team.themoment.datagsm.web.domain.project.service.impl.QueryPublicProjectServiceImpl
import team.themoment.datagsm.web.global.storage.ProjectIconStorage
import team.themoment.sdk.exception.ExpectedException
import java.util.Optional

class QueryPublicProjectServiceTest :
    DescribeSpec({

        val mockProjectRepository = mockk<ProjectJpaRepository>()
        val mockIconStorage = mockk<ProjectIconStorage>()

        val mapper = ProjectEditRequestMapper(mockIconStorage)

        val queryPublicProjectService =
            QueryPublicProjectServiceImpl(mockProjectRepository, mapper, mockIconStorage)

        val ownerClub =
            ClubJpaEntity().apply {
                id = 1L
                name = "SW개발동아리"
                type = ClubType.MAJOR_CLUB
            }

        val participant =
            StudentJpaEntity().apply {
                id = 1L
                name = "홍길동"
                email = "s24080@gsm.hs.kr"
                major = Major.SW_DEVELOPMENT
                sex = Sex.MAN
            }

        val testProject =
            ProjectJpaEntity().apply {
                id = 10L
                name = "DataGSM 프로젝트"
                description = "학교 데이터를 제공하는 API 서비스"
                startYear = 2024
                status = ProjectStatus.ACTIVE
                iconKey = "project-icons/abc.png"
                club = ownerClub
                participants = mutableSetOf(participant)
                repositories = mutableSetOf("https://github.com/team/repo")
                techStacks = mutableSetOf("Kotlin")
            }

        beforeEach {
            every { mockIconStorage.toIconUrl("project-icons/abc.png") } returns
                "https://cdn.datagsm.kr/project-icons/abc.png"
            every { mockIconStorage.toIconUrl(null) } returns null
        }

        afterEach {
            clearAllMocks()
        }

        describe("QueryPublicProjectService 클래스의") {

            describe("execute 메서드는") {

                context("조회 조건이 주어질 때") {
                    beforeEach {
                        every {
                            mockProjectRepository.searchProjectWithPaging(
                                id = null,
                                name = null,
                                clubId = null,
                                status = null,
                                pageable = PageRequest.of(0, 20),
                                sortBy = null,
                                sortDirection = any(),
                            )
                        } returns PageImpl(listOf(testProject), PageRequest.of(0, 20), 1)
                    }

                    it("프로젝트 목록이 반환되어야 한다") {
                        val result = queryPublicProjectService.execute(QueryPublicProjectReqDto())

                        result.totalElements shouldBe 1
                        result.projects[0].id shouldBe 10L
                        result.projects[0].name shouldBe "DataGSM 프로젝트"
                        result.projects[0].iconUrl shouldBe "https://cdn.datagsm.kr/project-icons/abc.png"
                    }

                    it("참여자 이메일은 응답에 포함되지 않아야 한다") {
                        val result = queryPublicProjectService.execute(QueryPublicProjectReqDto())

                        result.projects[0].participants[0].name shouldBe "홍길동"
                        result.projects[0].participants[0].major shouldBe Major.SW_DEVELOPMENT
                    }

                    it("상태 필터를 지정하지 않으면 전체 상태를 조회해야 한다") {
                        queryPublicProjectService.execute(QueryPublicProjectReqDto())

                        verify(exactly = 1) {
                            mockProjectRepository.searchProjectWithPaging(
                                id = null,
                                name = null,
                                clubId = null,
                                status = null,
                                pageable = any(),
                                sortBy = null,
                                sortDirection = any(),
                            )
                        }
                    }
                }
            }

            describe("executeById 메서드는") {

                context("존재하는 프로젝트 ID가 주어질 때") {
                    beforeEach {
                        every { mockProjectRepository.findById(10L) } returns Optional.of(testProject)
                    }

                    it("프로젝트 상세 정보가 반환되어야 한다") {
                        val result = queryPublicProjectService.executeById(10L)

                        result.id shouldBe 10L
                        result.club?.name shouldBe "SW개발동아리"
                    }
                }

                context("존재하지 않는 프로젝트 ID가 주어질 때") {
                    beforeEach {
                        every { mockProjectRepository.findById(999L) } returns Optional.empty()
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                queryPublicProjectService.executeById(999L)
                            }

                        exception.message shouldBe "프로젝트를 찾을 수 없습니다."
                    }
                }
            }
        }
    })
