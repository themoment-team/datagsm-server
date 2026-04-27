package team.themoment.datagsm.openapi.domain.project.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import team.themoment.datagsm.common.domain.project.dto.request.EndProjectReqDto
import team.themoment.datagsm.common.domain.project.entity.ProjectJpaEntity
import team.themoment.datagsm.common.domain.project.entity.constant.ProjectStatus
import team.themoment.datagsm.common.domain.project.repository.ProjectJpaRepository
import team.themoment.datagsm.openapi.domain.project.service.impl.EndProjectServiceImpl
import team.themoment.sdk.exception.ExpectedException

class EndProjectServiceTest :
    DescribeSpec({

        val mockProjectRepository = mockk<ProjectJpaRepository>()

        val endProjectService = EndProjectServiceImpl(mockProjectRepository)

        describe("EndProjectService 클래스의") {
            describe("execute 메서드는") {

                context("운영 중인 프로젝트를 종료 처리할 때") {
                    val project =
                        ProjectJpaEntity().apply {
                            id = 1L
                            name = "DataGSM 프로젝트"
                            description = "학교 데이터 API"
                            startYear = 2023
                            status = ProjectStatus.ACTIVE
                        }

                    val reqDto = EndProjectReqDto(endYear = 2025)

                    beforeEach {
                        every { mockProjectRepository.findById(1L) } returns java.util.Optional.of(project)
                    }

                    it("status가 ENDED로 변경되고 endYear가 설정되어야 한다") {
                        endProjectService.execute(1L, reqDto)

                        project.status shouldBe ProjectStatus.ENDED
                        project.endYear shouldBe 2025

                        verify(exactly = 1) { mockProjectRepository.findById(1L) }
                    }
                }

                context("종료 연도가 시작 연도와 같을 때") {
                    val project =
                        ProjectJpaEntity().apply {
                            id = 2L
                            name = "단기 프로젝트"
                            description = "1년 프로젝트"
                            startYear = 2024
                            status = ProjectStatus.ACTIVE
                        }

                    val reqDto = EndProjectReqDto(endYear = 2024)

                    beforeEach {
                        every { mockProjectRepository.findById(2L) } returns java.util.Optional.of(project)
                    }

                    it("정상적으로 종료 처리되어야 한다") {
                        endProjectService.execute(2L, reqDto)

                        project.status shouldBe ProjectStatus.ENDED
                        project.endYear shouldBe 2024
                    }
                }

                context("종료 연도가 시작 연도보다 작을 때") {
                    val project =
                        ProjectJpaEntity().apply {
                            id = 3L
                            name = "DataGSM 프로젝트"
                            description = "학교 데이터 API"
                            startYear = 2025
                            status = ProjectStatus.ACTIVE
                        }

                    val reqDto = EndProjectReqDto(endYear = 2024)

                    beforeEach {
                        every { mockProjectRepository.findById(3L) } returns java.util.Optional.of(project)
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                endProjectService.execute(3L, reqDto)
                            }

                        exception.message shouldBe "종료 연도는 시작 연도보다 크거나 같아야 합니다."

                        verify(exactly = 1) { mockProjectRepository.findById(3L) }
                    }
                }

                context("존재하지 않는 프로젝트를 종료 처리할 때") {
                    val reqDto = EndProjectReqDto(endYear = 2025)

                    beforeEach {
                        every { mockProjectRepository.findById(999L) } returns java.util.Optional.empty()
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                endProjectService.execute(999L, reqDto)
                            }

                        exception.message shouldBe "프로젝트를 찾을 수 없습니다."

                        verify(exactly = 1) { mockProjectRepository.findById(999L) }
                    }
                }
            }
        }
    })
