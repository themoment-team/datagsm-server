package team.themoment.datagsm.web.domain.project.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.springframework.context.ApplicationEventPublisher
import team.themoment.datagsm.common.domain.event.dto.internal.EventDispatchRequested
import team.themoment.datagsm.common.domain.event.dto.payload.EventChangedData
import team.themoment.datagsm.common.domain.event.dto.payload.ProjectEventObject
import team.themoment.datagsm.common.domain.event.entity.constant.EventType
import team.themoment.datagsm.common.domain.project.entity.ProjectJpaEntity
import team.themoment.datagsm.common.domain.project.entity.constant.ProjectStatus
import team.themoment.datagsm.common.domain.project.repository.ProjectJpaRepository
import team.themoment.datagsm.web.domain.project.service.impl.ReactivateProjectServiceImpl
import team.themoment.sdk.exception.ExpectedException
import java.util.Optional

class ReactivateProjectServiceTest :
    DescribeSpec({

        val mockProjectRepository = mockk<ProjectJpaRepository>()
        val applicationEventPublisher = mockk<ApplicationEventPublisher>()
        val eventSlot = slot<EventDispatchRequested>()

        val reactivateProjectService = ReactivateProjectServiceImpl(mockProjectRepository, applicationEventPublisher)

        beforeEach {
            clearMocks(applicationEventPublisher)
            justRun { applicationEventPublisher.publishEvent(capture(eventSlot)) }
        }

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
                        every { mockProjectRepository.findById(1L) } returns Optional.of(project)
                    }

                    it("status가 ACTIVE로 변경되고 endYear가 null로 초기화되어야 한다") {
                        reactivateProjectService.execute(1L)

                        project.status shouldBe ProjectStatus.ACTIVE
                        project.endYear shouldBe null

                        verify(exactly = 1) { mockProjectRepository.findById(1L) }

                        verify(exactly = 1) {
                            applicationEventPublisher.publishEvent(
                                match<EventDispatchRequested> { it.eventType == EventType.PROJECT_UPDATED },
                            )
                        }
                        val data = eventSlot.captured.data as EventChangedData
                        data.old.size shouldBe 1
                        data.new.size shouldBe 1
                        (data.old[0].obj as ProjectEventObject).status shouldBe ProjectStatus.ENDED.name
                        (data.new[0].obj as ProjectEventObject).status shouldBe ProjectStatus.ACTIVE.name
                        (data.new[0].obj as ProjectEventObject).endYear shouldBe null
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
                        every { mockProjectRepository.findById(2L) } returns Optional.of(project)
                    }

                    it("status는 ACTIVE로 유지되고 endYear는 null로 설정되어야 한다") {
                        reactivateProjectService.execute(2L)

                        project.status shouldBe ProjectStatus.ACTIVE
                        project.endYear shouldBe null
                    }
                }

                context("존재하지 않는 프로젝트를 운영 재개 요청할 때") {
                    beforeEach {
                        every { mockProjectRepository.findById(999L) } returns Optional.empty()
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
