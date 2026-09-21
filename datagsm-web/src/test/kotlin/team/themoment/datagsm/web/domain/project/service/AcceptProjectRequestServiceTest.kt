package team.themoment.datagsm.web.domain.project.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.springframework.context.ApplicationEventPublisher
import team.themoment.datagsm.common.domain.event.dto.internal.EventDispatchRequested
import team.themoment.datagsm.common.domain.project.entity.ProjectEditRequestJpaEntity
import team.themoment.datagsm.common.domain.project.entity.ProjectJpaEntity
import team.themoment.datagsm.common.domain.project.entity.constant.ProjectRequestStatus
import team.themoment.datagsm.common.domain.project.entity.constant.ProjectStatus
import team.themoment.datagsm.common.domain.project.repository.ProjectEditRequestJpaRepository
import team.themoment.datagsm.common.domain.project.repository.ProjectJpaRepository
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.common.domain.student.entity.constant.Sex
import team.themoment.datagsm.web.domain.project.mapper.ProjectEditRequestMapper
import team.themoment.datagsm.web.domain.project.service.impl.AcceptProjectRequestServiceImpl
import team.themoment.datagsm.web.global.storage.ProjectIconStorage
import team.themoment.sdk.exception.ExpectedException
import java.util.Optional

class AcceptProjectRequestServiceTest :
    DescribeSpec({

        val mockProjectRepository = mockk<ProjectJpaRepository>()
        val mockEditRequestRepository = mockk<ProjectEditRequestJpaRepository>()
        val mockIconStorage = mockk<ProjectIconStorage>()
        val applicationEventPublisher = mockk<ApplicationEventPublisher>()

        val mapper = ProjectEditRequestMapper(mockIconStorage)

        val acceptProjectRequestService =
            AcceptProjectRequestServiceImpl(
                mockProjectRepository,
                mockEditRequestRepository,
                mapper,
                mockIconStorage,
                applicationEventPublisher,
            )

        val requestId = 100L

        val applicant =
            StudentJpaEntity().apply {
                id = 1L
                name = "홍길동"
                email = "s24080@gsm.hs.kr"
                sex = Sex.MAN
            }

        beforeEach {
            justRun { applicationEventPublisher.publishEvent(any<EventDispatchRequested>()) }
            every { mockIconStorage.toIconUrl(any()) } returns null
            every { mockEditRequestRepository.save(any<ProjectEditRequestJpaEntity>()) } answers { firstArg() }
            every { mockProjectRepository.save(any<ProjectJpaEntity>()) } answers {
                firstArg<ProjectJpaEntity>().apply { if (id == null) id = 500L }
            }
        }

        afterEach {
            clearAllMocks()
        }

        describe("AcceptProjectRequestService 클래스의") {
            describe("execute 메서드는") {

                context("신규 생성 신청을 수락할 때") {
                    lateinit var newProjectRequest: ProjectEditRequestJpaEntity

                    beforeEach {
                        newProjectRequest =
                            ProjectEditRequestJpaEntity().apply {
                                id = requestId
                                originalProject = null
                                requestedBy = applicant
                                name = "DataGSM 프로젝트"
                                description = "학교 데이터를 제공하는 API 서비스"
                                startYear = 2024
                                repositories = mutableSetOf("https://github.com/team/repo")
                                techStacks = mutableSetOf("Kotlin")
                                requestStatus = ProjectRequestStatus.PENDING
                            }

                        every { mockEditRequestRepository.findById(requestId) } returns Optional.of(newProjectRequest)
                        every { mockProjectRepository.existsByName("DataGSM 프로젝트") } returns false
                    }

                    it("새 프로젝트가 생성되고 신청자가 소유자로 기록되어야 한다") {
                        val captured = slot<ProjectJpaEntity>()

                        val result = acceptProjectRequestService.execute(requestId)

                        verify(exactly = 1) { mockProjectRepository.save(capture(captured)) }
                        captured.captured.name shouldBe "DataGSM 프로젝트"
                        captured.captured.status shouldBe ProjectStatus.ACTIVE
                        captured.captured.appliedBy shouldBe applicant

                        result.id shouldBe 500L
                        result.name shouldBe "DataGSM 프로젝트"
                    }

                    it("신청 상태가 ACCEPTED로 변경되고 처리 일시가 기록되어야 한다") {
                        acceptProjectRequestService.execute(requestId)

                        newProjectRequest.requestStatus shouldBe ProjectRequestStatus.ACCEPTED
                        newProjectRequest.processedAt shouldNotBe null
                        verify(exactly = 1) { mockEditRequestRepository.save(newProjectRequest) }
                    }

                    it("PROJECT_UPDATED 이벤트가 발행되어야 한다") {
                        acceptProjectRequestService.execute(requestId)

                        verify(exactly = 1) { applicationEventPublisher.publishEvent(any<EventDispatchRequested>()) }
                    }

                    it("이미 같은 이름의 프로젝트가 있으면 ExpectedException이 발생해야 한다") {
                        every { mockProjectRepository.existsByName("DataGSM 프로젝트") } returns true

                        val exception =
                            shouldThrow<ExpectedException> {
                                acceptProjectRequestService.execute(requestId)
                            }

                        exception.message shouldBe "이미 존재하는 프로젝트 이름입니다."
                        verify(exactly = 0) { mockProjectRepository.save(any<ProjectJpaEntity>()) }
                    }
                }

                context("기존 프로젝트의 수정 신청을 수락할 때") {
                    val projectId = 1L
                    lateinit var existingProject: ProjectJpaEntity
                    lateinit var modificationRequest: ProjectEditRequestJpaEntity

                    beforeEach {
                        existingProject =
                            ProjectJpaEntity().apply {
                                id = projectId
                                name = "기존 프로젝트"
                                description = "기존 설명"
                                startYear = 2022
                                status = ProjectStatus.ACTIVE
                                appliedBy = applicant
                            }

                        modificationRequest =
                            ProjectEditRequestJpaEntity().apply {
                                id = requestId
                                originalProject = existingProject
                                requestedBy = applicant
                                name = "수정된 프로젝트"
                                description = "수정된 설명"
                                startYear = 2024
                                requestStatus = ProjectRequestStatus.PENDING
                            }

                        every { mockEditRequestRepository.findById(requestId) } returns Optional.of(modificationRequest)
                        every { mockProjectRepository.existsByNameAndIdNot("수정된 프로젝트", projectId) } returns false
                    }

                    it("기존 프로젝트가 신청 내용으로 갱신되어야 한다") {
                        val result = acceptProjectRequestService.execute(requestId)

                        existingProject.name shouldBe "수정된 프로젝트"
                        existingProject.description shouldBe "수정된 설명"
                        existingProject.startYear shouldBe 2024
                        result.id shouldBe projectId
                    }

                    it("운영 상태는 신청 내용에 영향받지 않아야 한다") {
                        acceptProjectRequestService.execute(requestId)

                        existingProject.status shouldBe ProjectStatus.ACTIVE
                    }
                }

                context("이미 처리된 신청을 수락할 때") {
                    beforeEach {
                        val processedRequest =
                            ProjectEditRequestJpaEntity().apply {
                                id = requestId
                                requestedBy = applicant
                                name = "프로젝트"
                                description = "설명"
                                startYear = 2024
                                requestStatus = ProjectRequestStatus.ACCEPTED
                            }

                        every { mockEditRequestRepository.findById(requestId) } returns Optional.of(processedRequest)
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                acceptProjectRequestService.execute(requestId)
                            }

                        exception.message shouldBe "이미 처리된 신청입니다."
                        verify(exactly = 0) { mockProjectRepository.save(any<ProjectJpaEntity>()) }
                    }
                }

                context("존재하지 않는 신청 ID로 수락할 때") {
                    beforeEach {
                        every { mockEditRequestRepository.findById(999L) } returns Optional.empty()
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                acceptProjectRequestService.execute(999L)
                            }

                        exception.message shouldBe "신청 내역을 찾을 수 없습니다."
                    }
                }
            }
        }
    })
