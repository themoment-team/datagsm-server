package team.themoment.datagsm.web.domain.project.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import team.themoment.datagsm.common.domain.club.ClubJpaEntity
import team.themoment.datagsm.common.domain.club.ClubType
import team.themoment.datagsm.common.domain.project.ProjectJpaEntity
import team.themoment.datagsm.web.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.web.domain.project.dto.request.ProjectReqDto
import team.themoment.datagsm.web.domain.project.repository.ProjectJpaRepository
import team.themoment.datagsm.web.domain.project.service.impl.CreateProjectServiceImpl
import team.themoment.sdk.exception.ExpectedException
import java.util.Optional

class CreateProjectServiceTest :
    DescribeSpec({

        val mockProjectRepository = mockk<ProjectJpaRepository>()
        val mockClubRepository = mockk<ClubJpaRepository>()
        val mockStudentRepository = mockk<team.themoment.datagsm.web.domain.student.repository.StudentJpaRepository>()

        val createProjectService = CreateProjectServiceImpl(mockProjectRepository, mockClubRepository, mockStudentRepository)

        afterEach {
            clearAllMocks()
        }

        describe("CreateProjectService 클래스의") {
            describe("execute 메서드는") {

                context("유효한 프로젝트 정보로 생성 요청할 때") {
                    val createRequest =
                        ProjectReqDto(
                            name = "DataGSM 프로젝트",
                            description = "학교 데이터를 제공하는 API 서비스",
                            clubId = 1L,
                        )

                    val ownerClub =
                        ClubJpaEntity().apply {
                            id = 1L
                            name = "SW개발동아리"
                            type = ClubType.MAJOR_CLUB
                        }

                    val savedProject =
                        ProjectJpaEntity().apply {
                            id = 1L
                            name = createRequest.name
                            description = createRequest.description
                            this.club = ownerClub
                        }

                    beforeEach {
                        every { mockProjectRepository.existsByName(createRequest.name) } returns false
                        every { mockClubRepository.findById(createRequest.clubId) } returns Optional.of(ownerClub)
                        every { mockProjectRepository.save(any()) } returns savedProject
                    }

                    it("새로운 프로젝트를 생성하고 저장 후 결과를 반환한다") {
                        val result = createProjectService.execute(createRequest)

                        result.id shouldBe 1L
                        result.name shouldBe "DataGSM 프로젝트"
                        result.description shouldBe "학교 데이터를 제공하는 API 서비스"
                        result.club?.id shouldBe 1L
                        result.club?.name shouldBe "SW개발동아리"
                        result.club?.type shouldBe ClubType.MAJOR_CLUB
                        result.participants shouldBe emptyList()

                        verify(exactly = 1) { mockProjectRepository.existsByName(createRequest.name) }
                        verify(exactly = 1) { mockClubRepository.findById(createRequest.clubId) }
                        verify(exactly = 1) { mockProjectRepository.save(any()) }
                    }
                }

                context("이미 존재하는 프로젝트 이름으로 생성 요청할 때") {
                    val createRequest =
                        ProjectReqDto(
                            name = "중복프로젝트",
                            description = "중복된 프로젝트입니다",
                            clubId = 1L,
                        )

                    beforeEach {
                        every { mockProjectRepository.existsByName(createRequest.name) } returns true
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                createProjectService.execute(createRequest)
                            }

                        exception.message shouldBe "이미 존재하는 프로젝트 이름입니다: ${createRequest.name}"

                        verify(exactly = 1) { mockProjectRepository.existsByName(createRequest.name) }
                        verify(exactly = 0) { mockClubRepository.findById(any()) }
                        verify(exactly = 0) { mockProjectRepository.save(any()) }
                    }
                }

                context("존재하지 않는 동아리 ID로 생성 요청할 때") {
                    val createRequest =
                        ProjectReqDto(
                            name = "신규프로젝트",
                            description = "신규 프로젝트입니다",
                            clubId = 999L,
                        )

                    beforeEach {
                        every { mockProjectRepository.existsByName(createRequest.name) } returns false
                        every { mockClubRepository.findById(createRequest.clubId) } returns Optional.empty()
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                createProjectService.execute(createRequest)
                            }

                        exception.message shouldBe "동아리를 찾을 수 없습니다. clubId: ${createRequest.clubId}"

                        verify(exactly = 1) { mockProjectRepository.existsByName(createRequest.name) }
                        verify(exactly = 1) { mockClubRepository.findById(createRequest.clubId) }
                        verify(exactly = 0) { mockProjectRepository.save(any()) }
                    }
                }

                context("여러 타입의 동아리로 프로젝트를 생성할 때") {
                    val createRequest =
                        ProjectReqDto(
                            name = "자율동아리 프로젝트",
                            description = "자율동아리 프로젝트입니다",
                            clubId = 2L,
                        )

                    val ownerClub =
                        ClubJpaEntity().apply {
                            id = 2L
                            name = "자율동아리"
                            type = ClubType.AUTONOMOUS_CLUB
                        }

                    val savedProject =
                        ProjectJpaEntity().apply {
                            id = 2L
                            name = createRequest.name
                            description = createRequest.description
                            this.club = ownerClub
                        }

                    beforeEach {
                        every { mockProjectRepository.existsByName(createRequest.name) } returns false
                        every { mockClubRepository.findById(createRequest.clubId) } returns Optional.of(ownerClub)
                        every { mockProjectRepository.save(any()) } returns savedProject
                    }

                    it("자율동아리 타입으로 프로젝트가 생성되어야 한다") {
                        val result = createProjectService.execute(createRequest)

                        result.id shouldBe 2L
                        result.club?.type shouldBe ClubType.AUTONOMOUS_CLUB
                        result.club?.name shouldBe "자율동아리"
                    }
                }
            }
        }
    })
