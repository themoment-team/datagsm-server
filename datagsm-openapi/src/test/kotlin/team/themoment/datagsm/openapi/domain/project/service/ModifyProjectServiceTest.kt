package team.themoment.datagsm.openapi.domain.project.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import team.themoment.datagsm.common.domain.club.entity.ClubJpaEntity
import team.themoment.datagsm.common.domain.club.entity.constant.ClubType
import team.themoment.datagsm.common.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.common.domain.project.dto.request.ProjectReqDto
import team.themoment.datagsm.common.domain.project.entity.ProjectJpaEntity
import team.themoment.datagsm.common.domain.project.repository.ProjectJpaRepository
import team.themoment.datagsm.openapi.domain.project.service.impl.ModifyProjectServiceImpl
import team.themoment.sdk.exception.ExpectedException
import java.util.Optional

class ModifyProjectServiceTest :
    DescribeSpec({

        lateinit var mockProjectRepository: ProjectJpaRepository
        lateinit var mockClubRepository: ClubJpaRepository
        lateinit var modifyProjectService: ModifyProjectService

        lateinit var mockStudentRepository: team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository

        beforeEach {
            mockProjectRepository = mockk<ProjectJpaRepository>()
            mockClubRepository = mockk<ClubJpaRepository>()
            mockStudentRepository = mockk<team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository>()
            modifyProjectService = ModifyProjectServiceImpl(mockProjectRepository, mockClubRepository, mockStudentRepository)
        }

        describe("ModifyProjectService 클래스의") {
            describe("execute 메서드는") {

                val projectId = 1L
                lateinit var existingProject: ProjectJpaEntity
                lateinit var ownerClub: ClubJpaEntity

                beforeEach {
                    ownerClub =
                        ClubJpaEntity().apply {
                            id = 1L
                            name = "기존동아리"
                            type = ClubType.MAJOR_CLUB
                        }

                    existingProject =
                        ProjectJpaEntity().apply {
                            this.id = projectId
                            name = "기존프로젝트"
                            description = "기존 설명"
                            this.club = ownerClub
                        }
                }

                context("존재하는 프로젝트의 이름을 수정할 때") {
                    val updateRequest =
                        ProjectReqDto(
                            name = "수정된프로젝트",
                            description = "기존 설명",
                            clubId = 1L,
                        )

                    beforeEach {
                        every { mockProjectRepository.findById(projectId) } returns Optional.of(existingProject)
                        every {
                            mockProjectRepository.existsByNameAndIdNot(
                                updateRequest.name,
                                projectId,
                            )
                        } returns false
                        every { mockClubRepository.findById(1L) } returns Optional.of(ownerClub)
                    }

                    it("프로젝트 이름이 성공적으로 업데이트되어야 한다") {
                        val result = modifyProjectService.execute(projectId, updateRequest)

                        result.id shouldBe projectId
                        result.name shouldBe "수정된프로젝트"
                        result.description shouldBe "기존 설명"
                        result.participants shouldBe emptyList()

                        verify(exactly = 1) { mockProjectRepository.findById(projectId) }
                        verify(exactly = 1) {
                            mockProjectRepository.existsByNameAndIdNot(
                                updateRequest.name,
                                projectId,
                            )
                        }
                        verify(exactly = 1) { mockClubRepository.findById(1L) }
                    }
                }

                context("존재하지 않는 프로젝트를 수정하려고 할 때") {
                    val updateRequest =
                        ProjectReqDto(
                            name = "수정프로젝트",
                            description = "설명",
                            clubId = 1L,
                        )

                    beforeEach {
                        every { mockProjectRepository.findById(999L) } returns Optional.empty()
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                modifyProjectService.execute(999L, updateRequest)
                            }

                        exception.message shouldBe "프로젝트를 찾을 수 없습니다. projectId: 999"

                        verify(exactly = 1) { mockProjectRepository.findById(999L) }
                        verify(exactly = 0) { mockProjectRepository.existsByNameAndIdNot(any(), any()) }
                    }
                }
            }
        }
    })
