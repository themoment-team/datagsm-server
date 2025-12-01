package team.themoment.datagsm.domain.project.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import team.themoment.datagsm.domain.club.entity.ClubJpaEntity
import team.themoment.datagsm.domain.club.entity.constant.ClubType
import team.themoment.datagsm.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.domain.project.dto.request.ProjectReqDto
import team.themoment.datagsm.domain.project.entity.ProjectJpaEntity
import team.themoment.datagsm.domain.project.repository.ProjectJpaRepository
import team.themoment.datagsm.domain.project.service.impl.CreateProjectServiceImpl
import team.themoment.datagsm.global.exception.error.ExpectedException
import java.util.Optional

class CreateProjectServiceTest :
    DescribeSpec({

        val mockProjectRepository = mockk<ProjectJpaRepository>()
        val mockClubRepository = mockk<ClubJpaRepository>()

        val createProjectService = CreateProjectServiceImpl(mockProjectRepository, mockClubRepository)

        afterEach {
            clearAllMocks()
        }

        describe("CreateProjectService 클래스의") {
            describe("execute 메서드는") {

                context("유효한 프로젝트 정보로 생성 요청할 때") {
                    val createRequest =
                        ProjectReqDto(
                            projectName = "DataGSM 프로젝트",
                            projectDescription = "학교 데이터를 제공하는 API 서비스",
                            projectOwnerClubId = 1L,
                        )

                    val ownerClub =
                        ClubJpaEntity().apply {
                            clubId = 1L
                            clubName = "SW개발동아리"
                            clubType = ClubType.MAJOR_CLUB
                        }

                    val savedProject =
                        ProjectJpaEntity().apply {
                            projectId = 1L
                            projectName = createRequest.projectName
                            projectDescription = createRequest.projectDescription
                            projectOwnerClub = ownerClub
                        }

                    beforeEach {
                        every { mockProjectRepository.existsByProjectName(createRequest.projectName) } returns false
                        every { mockClubRepository.findById(createRequest.projectOwnerClubId) } returns Optional.of(ownerClub)
                        every { mockProjectRepository.save(any()) } returns savedProject
                    }

                    it("새로운 프로젝트를 생성하고 저장 후 결과를 반환한다") {
                        val result = createProjectService.execute(createRequest)

                        result.projectId shouldBe 1L
                        result.projectName shouldBe "DataGSM 프로젝트"
                        result.projectDescription shouldBe "학교 데이터를 제공하는 API 서비스"
                        result.projectOwnerClub.clubId shouldBe 1L
                        result.projectOwnerClub.clubName shouldBe "SW개발동아리"
                        result.projectOwnerClub.clubType shouldBe ClubType.MAJOR_CLUB

                        verify(exactly = 1) { mockProjectRepository.existsByProjectName(createRequest.projectName) }
                        verify(exactly = 1) { mockClubRepository.findById(createRequest.projectOwnerClubId) }
                        verify(exactly = 1) { mockProjectRepository.save(any()) }
                    }
                }

                context("이미 존재하는 프로젝트 이름으로 생성 요청할 때") {
                    val createRequest =
                        ProjectReqDto(
                            projectName = "중복프로젝트",
                            projectDescription = "중복된 프로젝트입니다",
                            projectOwnerClubId = 1L,
                        )

                    beforeEach {
                        every { mockProjectRepository.existsByProjectName(createRequest.projectName) } returns true
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                createProjectService.execute(createRequest)
                            }

                        exception.message shouldBe "이미 존재하는 프로젝트 이름입니다: ${createRequest.projectName}"

                        verify(exactly = 1) { mockProjectRepository.existsByProjectName(createRequest.projectName) }
                        verify(exactly = 0) { mockClubRepository.findById(any()) }
                        verify(exactly = 0) { mockProjectRepository.save(any()) }
                    }
                }

                context("존재하지 않는 동아리 ID로 생성 요청할 때") {
                    val createRequest =
                        ProjectReqDto(
                            projectName = "신규프로젝트",
                            projectDescription = "신규 프로젝트입니다",
                            projectOwnerClubId = 999L,
                        )

                    beforeEach {
                        every { mockProjectRepository.existsByProjectName(createRequest.projectName) } returns false
                        every { mockClubRepository.findById(createRequest.projectOwnerClubId) } returns Optional.empty()
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                createProjectService.execute(createRequest)
                            }

                        exception.message shouldBe "동아리를 찾을 수 없습니다. clubId: ${createRequest.projectOwnerClubId}"

                        verify(exactly = 1) { mockProjectRepository.existsByProjectName(createRequest.projectName) }
                        verify(exactly = 1) { mockClubRepository.findById(createRequest.projectOwnerClubId) }
                        verify(exactly = 0) { mockProjectRepository.save(any()) }
                    }
                }

                context("여러 타입의 동아리로 프로젝트를 생성할 때") {
                    val createRequest =
                        ProjectReqDto(
                            projectName = "자율동아리 프로젝트",
                            projectDescription = "자율동아리 프로젝트입니다",
                            projectOwnerClubId = 2L,
                        )

                    val ownerClub =
                        ClubJpaEntity().apply {
                            clubId = 2L
                            clubName = "자율동아리"
                            clubType = ClubType.AUTONOMOUS_CLUB
                        }

                    val savedProject =
                        ProjectJpaEntity().apply {
                            projectId = 2L
                            projectName = createRequest.projectName
                            projectDescription = createRequest.projectDescription
                            projectOwnerClub = ownerClub
                        }

                    beforeEach {
                        every { mockProjectRepository.existsByProjectName(createRequest.projectName) } returns false
                        every { mockClubRepository.findById(createRequest.projectOwnerClubId) } returns Optional.of(ownerClub)
                        every { mockProjectRepository.save(any()) } returns savedProject
                    }

                    it("자율동아리 타입으로 프로젝트가 생성되어야 한다") {
                        val result = createProjectService.execute(createRequest)

                        result.projectId shouldBe 2L
                        result.projectOwnerClub.clubType shouldBe ClubType.AUTONOMOUS_CLUB
                        result.projectOwnerClub.clubName shouldBe "자율동아리"
                    }
                }
            }
        }
    })
