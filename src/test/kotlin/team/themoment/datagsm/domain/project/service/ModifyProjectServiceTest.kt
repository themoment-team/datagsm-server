package team.themoment.datagsm.domain.project.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import team.themoment.datagsm.domain.club.entity.ClubJpaEntity
import team.themoment.datagsm.domain.club.entity.constant.ClubType
import team.themoment.datagsm.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.domain.project.dto.request.ProjectReqDto
import team.themoment.datagsm.domain.project.entity.ProjectJpaEntity
import team.themoment.datagsm.domain.project.repository.ProjectJpaRepository
import team.themoment.datagsm.domain.project.service.impl.ModifyProjectServiceImpl
import team.themoment.datagsm.global.exception.error.ExpectedException
import java.util.Optional

class ModifyProjectServiceTest :
    DescribeSpec({

        lateinit var mockProjectRepository: ProjectJpaRepository
        lateinit var mockClubRepository: ClubJpaRepository
        lateinit var modifyProjectService: ModifyProjectService

        beforeEach {
            mockProjectRepository = mockk<ProjectJpaRepository>()
            mockClubRepository = mockk<ClubJpaRepository>()
            modifyProjectService = ModifyProjectServiceImpl(mockProjectRepository, mockClubRepository)
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
                            this.ownerClub = ownerClub
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
                            mockProjectRepository.existsByProjectNameAndProjectIdNot(
                                updateRequest.name,
                                projectId,
                            )
                        } returns false
                    }

                    it("프로젝트 이름이 성공적으로 업데이트되어야 한다") {
                        val result = modifyProjectService.execute(projectId, updateRequest)

                        result.id shouldBe projectId
                        result.name shouldBe "수정된프로젝트"
                        result.description shouldBe "기존 설명"

                        verify(exactly = 1) { mockProjectRepository.findById(projectId) }
                        verify(exactly = 1) {
                            mockProjectRepository.existsByProjectNameAndProjectIdNot(
                                updateRequest.name,
                                projectId,
                            )
                        }
                    }
                }

                context("프로젝트의 설명만 변경할 때") {
                    val updateRequest =
                        ProjectReqDto(
                            name = "기존프로젝트",
                            description = "새로운 설명입니다",
                            clubId = 1L,
                        )

                    beforeEach {
                        every { mockProjectRepository.findById(projectId) } returns Optional.of(existingProject)
                    }

                    it("프로젝트 설명만 변경되어야 한다") {
                        val result = modifyProjectService.execute(projectId, updateRequest)

                        result.name shouldBe "기존프로젝트"
                        result.description shouldBe "새로운 설명입니다"
                        result.club.id shouldBe 1L

                        verify(exactly = 1) { mockProjectRepository.findById(projectId) }
                        verify(exactly = 0) {
                            mockProjectRepository.existsByProjectNameAndProjectIdNot(
                                any(),
                                any(),
                            )
                        }
                    }
                }

                context("프로젝트의 소유 동아리를 변경할 때") {
                    val newClub =
                        ClubJpaEntity().apply {
                            id = 2L
                            name = "새동아리"
                            type = ClubType.JOB_CLUB
                        }

                    val updateRequest =
                        ProjectReqDto(
                            name = "기존프로젝트",
                            description = "기존 설명",
                            clubId = 2L,
                        )

                    beforeEach {
                        every { mockProjectRepository.findById(projectId) } returns Optional.of(existingProject)
                        every { mockClubRepository.findById(2L) } returns Optional.of(newClub)
                    }

                    it("프로젝트 소유 동아리가 변경되어야 한다") {
                        val result = modifyProjectService.execute(projectId, updateRequest)

                        result.club.id shouldBe 2L
                        result.club.name shouldBe "새동아리"
                        result.club.type shouldBe ClubType.JOB_CLUB

                        verify(exactly = 1) { mockProjectRepository.findById(projectId) }
                        verify(exactly = 1) { mockClubRepository.findById(2L) }
                    }
                }

                context("프로젝트의 모든 정보를 변경할 때") {
                    val newClub =
                        ClubJpaEntity().apply {
                            id = 3L
                            name = "완전새로운동아리"
                            type = ClubType.AUTONOMOUS_CLUB
                        }

                    val updateRequest =
                        ProjectReqDto(
                            name = "완전새로운프로젝트",
                            description = "완전 새로운 설명",
                            clubId = 3L,
                        )

                    beforeEach {
                        every { mockProjectRepository.findById(projectId) } returns Optional.of(existingProject)
                        every {
                            mockProjectRepository.existsByProjectNameAndProjectIdNot(
                                updateRequest.name,
                                projectId,
                            )
                        } returns false
                        every { mockClubRepository.findById(3L) } returns Optional.of(newClub)
                    }

                    it("프로젝트의 모든 정보가 변경되어야 한다") {
                        val result = modifyProjectService.execute(projectId, updateRequest)

                        result.name shouldBe "완전새로운프로젝트"
                        result.description shouldBe "완전 새로운 설명"
                        result.club.id shouldBe 3L
                        result.club.name shouldBe "완전새로운동아리"
                        result.club.type shouldBe ClubType.AUTONOMOUS_CLUB

                        verify(exactly = 1) {
                            mockProjectRepository.existsByProjectNameAndProjectIdNot(
                                updateRequest.name,
                                projectId,
                            )
                        }
                        verify(exactly = 1) { mockClubRepository.findById(3L) }
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
                        verify(exactly = 0) { mockProjectRepository.existsByProjectNameAndProjectIdNot(any(), any()) }
                    }
                }

                context("이미 존재하는 프로젝트 이름으로 변경을 시도할 때") {
                    val updateRequest =
                        ProjectReqDto(
                            name = "중복프로젝트",
                            description = "기존 설명",
                            clubId = 1L,
                        )

                    beforeEach {
                        every { mockProjectRepository.findById(projectId) } returns Optional.of(existingProject)
                        every {
                            mockProjectRepository.existsByProjectNameAndProjectIdNot(
                                updateRequest.name,
                                projectId,
                            )
                        } returns true
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                modifyProjectService.execute(projectId, updateRequest)
                            }

                        exception.message shouldBe "이미 존재하는 프로젝트 이름입니다: ${updateRequest.name}"

                        verify(exactly = 1) {
                            mockProjectRepository.existsByProjectNameAndProjectIdNot(
                                updateRequest.name,
                                projectId,
                            )
                        }
                    }
                }

                context("존재하지 않는 동아리로 변경을 시도할 때") {
                    val updateRequest =
                        ProjectReqDto(
                            name = "기존프로젝트",
                            description = "기존 설명",
                            clubId = 999L,
                        )

                    beforeEach {
                        every { mockProjectRepository.findById(projectId) } returns Optional.of(existingProject)
                        every { mockClubRepository.findById(999L) } returns Optional.empty()
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                modifyProjectService.execute(projectId, updateRequest)
                            }

                        exception.message shouldBe "동아리를 찾을 수 없습니다. clubId: 999"

                        verify(exactly = 1) { mockClubRepository.findById(999L) }
                    }
                }
            }
        }
    })
