package team.themoment.datagsm.web.domain.project.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import team.themoment.datagsm.common.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.common.domain.project.dto.request.ApplyProjectReqDto
import team.themoment.datagsm.common.domain.project.entity.ProjectEditRequestJpaEntity
import team.themoment.datagsm.common.domain.project.entity.ProjectJpaEntity
import team.themoment.datagsm.common.domain.project.entity.constant.ProjectRequestStatus
import team.themoment.datagsm.common.domain.project.entity.constant.ProjectStatus
import team.themoment.datagsm.common.domain.project.repository.ProjectEditRequestJpaRepository
import team.themoment.datagsm.common.domain.project.repository.ProjectJpaRepository
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.common.domain.student.entity.constant.Sex
import team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.web.domain.project.mapper.ProjectApplicationAssembler
import team.themoment.datagsm.web.domain.project.mapper.ProjectEditRequestMapper
import team.themoment.datagsm.web.domain.project.service.impl.ApplyProjectModificationServiceImpl
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider
import team.themoment.datagsm.web.global.storage.ProjectIconStorage
import team.themoment.sdk.exception.ExpectedException
import java.time.LocalDateTime
import java.util.Optional

class ApplyProjectModificationServiceTest :
    DescribeSpec({

        val mockProjectRepository = mockk<ProjectJpaRepository>()
        val mockEditRequestRepository = mockk<ProjectEditRequestJpaRepository>()
        val mockClubRepository = mockk<ClubJpaRepository>()
        val mockStudentRepository = mockk<StudentJpaRepository>()
        val mockIconStorage = mockk<ProjectIconStorage>()
        val mockCurrentUserProvider = mockk<CurrentUserProvider>()

        val assembler = ProjectApplicationAssembler(mockClubRepository, mockStudentRepository, mockIconStorage)
        val mapper = ProjectEditRequestMapper(mockIconStorage)

        val applyProjectModificationService =
            ApplyProjectModificationServiceImpl(
                mockProjectRepository,
                mockEditRequestRepository,
                assembler,
                mapper,
                mockCurrentUserProvider,
            )

        val projectId = 1L

        val owner =
            StudentJpaEntity().apply {
                id = 1L
                name = "홍길동"
                email = "s24080@gsm.hs.kr"
                sex = Sex.MAN
            }

        val participant =
            StudentJpaEntity().apply {
                id = 2L
                name = "김철수"
                email = "s24081@gsm.hs.kr"
                sex = Sex.MAN
            }

        val stranger =
            StudentJpaEntity().apply {
                id = 3L
                name = "이영희"
                email = "s24082@gsm.hs.kr"
                sex = Sex.WOMAN
            }

        val reqDto =
            ApplyProjectReqDto(
                name = "수정된 프로젝트",
                description = "수정된 설명",
                startYear = 2024,
            )

        lateinit var existingProject: ProjectJpaEntity

        beforeEach {
            existingProject =
                ProjectJpaEntity().apply {
                    id = projectId
                    name = "DataGSM 프로젝트"
                    description = "학교 데이터를 제공하는 API 서비스"
                    startYear = 2022
                    status = ProjectStatus.ACTIVE
                    appliedBy = owner
                    participants = mutableSetOf(participant)
                }

            every { mockIconStorage.validateIconKey(any()) } answers { firstArg() }
            every { mockIconStorage.toIconUrl(any()) } returns null
            every { mockProjectRepository.findById(projectId) } returns Optional.of(existingProject)
            every { mockEditRequestRepository.save(any<ProjectEditRequestJpaEntity>()) } answers {
                firstArg<ProjectEditRequestJpaEntity>().apply { if (id == null) id = 100L }
            }
        }

        afterEach {
            clearAllMocks()
        }

        describe("ApplyProjectModificationService 클래스의") {
            describe("execute 메서드는") {

                context("최초 신청자가 수정 신청할 때") {
                    beforeEach {
                        every { mockCurrentUserProvider.getCurrentStudent() } returns owner
                        every {
                            mockEditRequestRepository.findByOriginalProjectId(projectId)
                        } returns Optional.empty()
                    }

                    it("원본 프로젝트를 가리키는 PENDING 신청이 생성되어야 한다") {
                        val captured = slot<ProjectEditRequestJpaEntity>()

                        val result = applyProjectModificationService.execute(projectId, reqDto)

                        verify(exactly = 1) { mockEditRequestRepository.save(capture(captured)) }
                        captured.captured.originalProject shouldBe existingProject
                        captured.captured.requestStatus shouldBe ProjectRequestStatus.PENDING
                        captured.captured.name shouldBe "수정된 프로젝트"

                        result.originalProjectId shouldBe projectId
                    }

                    it("원본 프로젝트는 변경되지 않아야 한다") {
                        applyProjectModificationService.execute(projectId, reqDto)

                        existingProject.name shouldBe "DataGSM 프로젝트"
                        existingProject.startYear shouldBe 2022
                    }
                }

                context("참여자가 수정 신청할 때") {
                    beforeEach {
                        every { mockCurrentUserProvider.getCurrentStudent() } returns participant
                        every {
                            mockEditRequestRepository.findByOriginalProjectId(projectId)
                        } returns Optional.empty()
                    }

                    it("신청자와 동일한 권한으로 수정 신청이 저장되어야 한다") {
                        val result = applyProjectModificationService.execute(projectId, reqDto)

                        result.originalProjectId shouldBe projectId
                        result.requestedBy.id shouldBe participant.id
                    }
                }

                context("신청자도 참여자도 아닌 학생이 수정 신청할 때") {
                    beforeEach {
                        every { mockCurrentUserProvider.getCurrentStudent() } returns stranger
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                applyProjectModificationService.execute(projectId, reqDto)
                            }

                        exception.message shouldBe "해당 프로젝트를 수정할 권한이 없습니다."
                        verify(exactly = 0) { mockEditRequestRepository.save(any<ProjectEditRequestJpaEntity>()) }
                    }
                }

                context("아이콘 없이 수정 신청할 때") {
                    lateinit var previousRequest: ProjectEditRequestJpaEntity

                    beforeEach {
                        previousRequest =
                            ProjectEditRequestJpaEntity().apply {
                                id = 61L
                                originalProject = existingProject
                                requestedBy = owner
                                name = "이전 수정안"
                                description = "이전 설명"
                                startYear = 2023
                                iconKey = "project-icons/3f2504e0-4f89-11d3-9a0c-0305e82c3301.png"
                                deploymentUrl = "https://datagsm.kr"
                                requestStatus = ProjectRequestStatus.PENDING
                            }

                        every { mockCurrentUserProvider.getCurrentStudent() } returns owner
                        every {
                            mockEditRequestRepository.findByOriginalProjectId(projectId)
                        } returns Optional.of(previousRequest)
                    }

                    it("기존 아이콘과 배포 URL이 유지되어야 한다") {
                        applyProjectModificationService.execute(projectId, reqDto)

                        previousRequest.name shouldBe "수정된 프로젝트"
                        previousRequest.iconKey shouldBe "project-icons/3f2504e0-4f89-11d3-9a0c-0305e82c3301.png"
                        previousRequest.deploymentUrl shouldBe "https://datagsm.kr"
                    }

                    it("새 아이콘을 보내면 교체되어야 한다") {
                        val newIconKey = "project-icons/11111111-2222-3333-4444-555555555555.webp"

                        applyProjectModificationService.execute(
                            projectId,
                            reqDto.copy(iconKey = newIconKey, deploymentUrl = "https://new.datagsm.kr"),
                        )

                        previousRequest.iconKey shouldBe newIconKey
                        previousRequest.deploymentUrl shouldBe "https://new.datagsm.kr"
                    }
                }

                context("이미 대기 중인 수정 신청이 있을 때") {
                    val previousRequestedAt = LocalDateTime.now().minusDays(1)

                    lateinit var pendingRequest: ProjectEditRequestJpaEntity

                    beforeEach {
                        pendingRequest =
                            ProjectEditRequestJpaEntity().apply {
                                id = 55L
                                originalProject = existingProject
                                requestedBy = owner
                                name = "이전 수정안"
                                description = "이전 설명"
                                startYear = 2023
                                requestStatus = ProjectRequestStatus.PENDING
                                requestedAt = previousRequestedAt
                            }

                        every { mockCurrentUserProvider.getCurrentStudent() } returns participant
                        every {
                            mockEditRequestRepository.findByOriginalProjectId(projectId)
                        } returns Optional.of(pendingRequest)
                    }

                    it("새 신청을 만들지 않고 기존 신청을 최신 내용으로 덮어써야 한다") {
                        val result = applyProjectModificationService.execute(projectId, reqDto)

                        result.id shouldBe 55L
                        pendingRequest.name shouldBe "수정된 프로젝트"
                        pendingRequest.startYear shouldBe 2024
                        pendingRequest.requestedBy shouldBe participant
                        pendingRequest.requestedAt shouldNotBe previousRequestedAt
                    }
                }

                context("직전 수정 신청이 거절된 상태일 때") {
                    lateinit var rejectedRequest: ProjectEditRequestJpaEntity

                    beforeEach {
                        rejectedRequest =
                            ProjectEditRequestJpaEntity().apply {
                                id = 77L
                                originalProject = existingProject
                                requestedBy = owner
                                name = "거절된 수정안"
                                description = "거절된 설명"
                                startYear = 2023
                                requestStatus = ProjectRequestStatus.REJECTED
                                rejectReason = "설명이 부족합니다."
                                processedAt = LocalDateTime.now().minusDays(1)
                            }

                        every { mockCurrentUserProvider.getCurrentStudent() } returns owner
                        every {
                            mockEditRequestRepository.findByOriginalProjectId(projectId)
                        } returns Optional.of(rejectedRequest)
                    }

                    it("거절 건을 재사용해 PENDING으로 되돌리고 거절 정보를 비워야 한다") {
                        val result = applyProjectModificationService.execute(projectId, reqDto)

                        result.id shouldBe 77L
                        rejectedRequest.requestStatus shouldBe ProjectRequestStatus.PENDING
                        rejectedRequest.rejectReason shouldBe null
                        rejectedRequest.processedAt shouldBe null
                        rejectedRequest.name shouldBe "수정된 프로젝트"
                    }

                    it("새 신청 행을 만들지 않아야 한다") {
                        val captured = slot<ProjectEditRequestJpaEntity>()

                        applyProjectModificationService.execute(projectId, reqDto)

                        verify(exactly = 1) { mockEditRequestRepository.save(capture(captured)) }
                        captured.captured.id shouldBe 77L
                    }
                }

                context("직전 수정 신청이 승인된 상태일 때") {
                    lateinit var acceptedRequest: ProjectEditRequestJpaEntity

                    beforeEach {
                        acceptedRequest =
                            ProjectEditRequestJpaEntity().apply {
                                id = 88L
                                originalProject = existingProject
                                requestedBy = owner
                                name = "승인된 수정안"
                                description = "승인된 설명"
                                startYear = 2023
                                requestStatus = ProjectRequestStatus.ACCEPTED
                                processedAt = LocalDateTime.now().minusDays(1)
                            }

                        every { mockCurrentUserProvider.getCurrentStudent() } returns owner
                        every {
                            mockEditRequestRepository.findByOriginalProjectId(projectId)
                        } returns Optional.of(acceptedRequest)
                    }

                    it("승인 건을 재사용해 프로젝트당 한 행만 유지해야 한다") {
                        val captured = slot<ProjectEditRequestJpaEntity>()

                        val result = applyProjectModificationService.execute(projectId, reqDto)

                        verify(exactly = 1) { mockEditRequestRepository.save(capture(captured)) }
                        captured.captured.id shouldBe 88L
                        result.id shouldBe 88L
                        acceptedRequest.requestStatus shouldBe ProjectRequestStatus.PENDING
                        acceptedRequest.processedAt shouldBe null
                    }
                }

                context("존재하지 않는 프로젝트 ID로 수정 신청할 때") {
                    beforeEach {
                        every { mockCurrentUserProvider.getCurrentStudent() } returns owner
                        every { mockProjectRepository.findById(999L) } returns Optional.empty()
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                applyProjectModificationService.execute(999L, reqDto)
                            }

                        exception.message shouldBe "프로젝트를 찾을 수 없습니다."
                    }
                }
            }
        }
    })
