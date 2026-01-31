package team.themoment.datagsm.openapi.domain.project.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import team.themoment.datagsm.common.domain.club.entity.ClubJpaEntity
import team.themoment.datagsm.common.domain.club.entity.constant.ClubType
import team.themoment.datagsm.common.domain.project.entity.ProjectJpaEntity
import team.themoment.datagsm.common.domain.project.repository.ProjectJpaRepository
import team.themoment.datagsm.openapi.domain.project.service.impl.DeleteProjectServiceImpl
import team.themoment.sdk.exception.ExpectedException
import java.util.Optional

class DeleteProjectServiceTest :
    DescribeSpec({

        val mockProjectRepository = mockk<ProjectJpaRepository>()

        val deleteProjectService = DeleteProjectServiceImpl(mockProjectRepository)

        afterEach {
            clearAllMocks()
        }

        describe("DeleteProjectService 클래스의") {
            describe("execute 메서드는") {

                context("존재하는 프로젝트 ID로 삭제 요청할 때") {
                    val projectId = 1L

                    val ownerClub =
                        ClubJpaEntity().apply {
                            id = 1L
                            name = "SW개발동아리"
                            type = ClubType.MAJOR_CLUB
                        }

                    val existingProject =
                        ProjectJpaEntity().apply {
                            this.id = projectId
                            name = "DataGSM 프로젝트"
                            description = "학교 데이터를 제공하는 API 서비스"
                            this.club = ownerClub
                        }

                    beforeEach {
                        every { mockProjectRepository.findById(projectId) } returns Optional.of(existingProject)
                        every { mockProjectRepository.delete(existingProject) } returns Unit
                    }

                    it("프로젝트가 성공적으로 삭제되어야 한다") {
                        deleteProjectService.execute(projectId)

                        verify(exactly = 1) { mockProjectRepository.findById(projectId) }
                        verify(exactly = 1) { mockProjectRepository.delete(existingProject) }
                    }
                }

                context("존재하지 않는 프로젝트 ID로 삭제 요청할 때") {
                    val projectId = 999L

                    beforeEach {
                        every { mockProjectRepository.findById(projectId) } returns Optional.empty()
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                deleteProjectService.execute(projectId)
                            }

                        exception.message shouldBe "프로젝트를 찾을 수 없습니다. projectId: $projectId"

                        verify(exactly = 1) { mockProjectRepository.findById(projectId) }
                        verify(exactly = 0) { mockProjectRepository.delete(any()) }
                    }
                }
            }
        }
    })
