package team.themoment.datagsm.openapi.domain.project.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import team.themoment.datagsm.common.domain.project.entity.ProjectJpaEntity
import team.themoment.datagsm.common.domain.project.entity.constant.ProjectStatus
import team.themoment.datagsm.common.domain.project.repository.ProjectJpaRepository
import team.themoment.datagsm.openapi.domain.project.service.impl.ReactivateProjectServiceImpl
import team.themoment.sdk.exception.ExpectedException

class ReactivateProjectServiceTest :
    DescribeSpec({

        val mockProjectRepository = mockk<ProjectJpaRepository>()

        val reactivateProjectService = ReactivateProjectServiceImpl(mockProjectRepository)

        describe("ReactivateProjectService 클래스의") {
            describe("execute 메서드는") {

                context("종료된 프로젝트를 운영 재개할 때") {
                    val project =
                        ProjectJpaEntity().apply {
                            id = 1L
                            name = "DataGSM 프로젝트"
                            description = "학교 데이터 API"
                            startYear = 2023
                            endYear = 2024
                            status = ProjectStatus.ENDED
                        }

                    beforeEach {
                        every { mockProjectRepository.findById(1L) } returns java.util.Optional.of(project)
                    }

                    it("status가 ACTIVE로 변경되고 endYear가 null로 초기화되어야 한다") {
                        reactivateProjectService.execute(1L)

                        project.status shouldBe ProjectStatus.ACTIVE
                        project.endYear shouldBe null

                        verify(exactly = 1) { mockProjectRepository.findById(1L) }
                    }
                }

                context("이미 운영 중인 프로젝트를 운영 재개 요청할 때") {
                    val project =
                        ProjectJpaEntity().apply {
                            id = 2L
                            name = "운영중 프로젝트"
                            description = "이미 운영 중인 프로젝트"
                            startYear = 2024
                            status = ProjectStatus.ACTIVE
                        }

                    beforeEach {
                        every { mockProjectRepository.findById(2L) } returns java.util.Optional.of(project)
                    }

                    it("status는 ACTIVE로 유지되고 endYear는 null로 설정되어야 한다") {
                        reactivateProjectService.execute(2L)

                        project.status shouldBe ProjectStatus.ACTIVE
                        project.endYear shouldBe null
                    }
                }

                context("존재하지 않는 프로젝트를 운영 재개 요청할 때") {
                    beforeEach {
                        every { mockProjectRepository.findById(999L) } returns java.util.Optional.empty()
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                reactivateProjectService.execute(999L)
                            }

                        exception.message shouldBe "프로젝트를 찾을 수 없습니다."

                        verify(exactly = 1) { mockProjectRepository.findById(999L) }
                    }
                }
            }
        }
    })
