package team.themoment.datagsm.openapi.domain.project.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.clearAllMocks
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.springframework.context.ApplicationEventPublisher
import team.themoment.datagsm.common.domain.club.entity.ClubJpaEntity
import team.themoment.datagsm.common.domain.club.entity.constant.ClubType
import team.themoment.datagsm.common.domain.event.dto.internal.EventDispatchRequested
import team.themoment.datagsm.common.domain.event.dto.payload.EmptyEventObject
import team.themoment.datagsm.common.domain.event.dto.payload.EventChangedData
import team.themoment.datagsm.common.domain.event.dto.payload.ProjectEventObject
import team.themoment.datagsm.common.domain.event.entity.constant.EventType
import team.themoment.datagsm.common.domain.project.entity.ProjectJpaEntity
import team.themoment.datagsm.common.domain.project.entity.constant.ProjectStatus
import team.themoment.datagsm.common.domain.project.repository.ProjectJpaRepository
import team.themoment.datagsm.openapi.domain.project.service.impl.DeleteProjectServiceImpl
import team.themoment.sdk.exception.ExpectedException
import java.util.Optional

class DeleteProjectServiceTest :
    DescribeSpec({

        val mockProjectRepository = mockk<ProjectJpaRepository>()
        val applicationEventPublisher = mockk<ApplicationEventPublisher>()
        val eventSlot = slot<EventDispatchRequested>()

        val deleteProjectService = DeleteProjectServiceImpl(mockProjectRepository, applicationEventPublisher)

        beforeEach {
            clearMocks(applicationEventPublisher)
            justRun { applicationEventPublisher.publishEvent(capture(eventSlot)) }
        }

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
                            status = ProjectStatus.ACTIVE
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

                        verify(exactly = 1) {
                            applicationEventPublisher.publishEvent(
                                match<EventDispatchRequested> { it.eventType == EventType.PROJECT_UPDATED },
                            )
                        }
                        val data = eventSlot.captured.data as EventChangedData
                        data.old.size shouldBe 1
                        data.new.size shouldBe 1
                        data.old[0].obj.shouldBeInstanceOf<ProjectEventObject>()
                        data.new[0].obj.shouldBeInstanceOf<EmptyEventObject>()
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

                        exception.message shouldBe "프로젝트를 찾을 수 없습니다."

                        verify(exactly = 1) { mockProjectRepository.findById(projectId) }
                        verify(exactly = 0) { mockProjectRepository.delete(any()) }
                    }
                }
            }
        }
    })
