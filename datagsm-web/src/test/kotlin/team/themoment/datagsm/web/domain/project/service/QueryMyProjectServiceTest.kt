package team.themoment.datagsm.web.domain.project.service

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import team.themoment.datagsm.common.domain.project.dto.request.QueryMyProjectReqDto
import team.themoment.datagsm.common.domain.project.entity.ProjectEditRequestJpaEntity
import team.themoment.datagsm.common.domain.project.entity.ProjectJpaEntity
import team.themoment.datagsm.common.domain.project.entity.constant.ProjectMemberRole
import team.themoment.datagsm.common.domain.project.entity.constant.ProjectRequestStatus
import team.themoment.datagsm.common.domain.project.entity.constant.ProjectStatus
import team.themoment.datagsm.common.domain.project.repository.ProjectEditRequestJpaRepository
import team.themoment.datagsm.common.domain.project.repository.ProjectJpaRepository
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.common.domain.student.entity.constant.Sex
import team.themoment.datagsm.web.domain.project.mapper.ProjectEditRequestMapper
import team.themoment.datagsm.web.domain.project.service.impl.QueryMyProjectServiceImpl
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider
import team.themoment.datagsm.web.global.storage.ProjectIconStorage

class QueryMyProjectServiceTest :
    DescribeSpec({

        val mockProjectRepository = mockk<ProjectJpaRepository>()
        val mockEditRequestRepository = mockk<ProjectEditRequestJpaRepository>()
        val mockIconStorage = mockk<ProjectIconStorage>()
        val mockCurrentUserProvider = mockk<CurrentUserProvider>()

        val mapper = ProjectEditRequestMapper(mockIconStorage)

        val queryMyProjectService =
            QueryMyProjectServiceImpl(
                mockProjectRepository,
                mockEditRequestRepository,
                mapper,
                mockIconStorage,
                mockCurrentUserProvider,
            )

        val me =
            StudentJpaEntity().apply {
                id = 1L
                name = "홍길동"
                email = "s24080@gsm.hs.kr"
                sex = Sex.MAN
            }

        val other =
            StudentJpaEntity().apply {
                id = 2L
                name = "김철수"
                email = "s24081@gsm.hs.kr"
                sex = Sex.MAN
            }

        beforeEach {
            every { mockCurrentUserProvider.getCurrentStudent() } returns me
            every { mockIconStorage.toIconUrl(any()) } returns null
        }

        afterEach {
            clearAllMocks()
        }

        describe("QueryMyProjectService 클래스의") {
            describe("execute 메서드는") {

                context("본인이 신청한 승인 완료 프로젝트만 있을 때") {
                    beforeEach {
                        val myProject =
                            ProjectJpaEntity().apply {
                                id = 10L
                                name = "내 프로젝트"
                                description = "설명"
                                startYear = 2024
                                status = ProjectStatus.ACTIVE
                                appliedBy = me
                            }

                        every { mockProjectRepository.findAllByParticipantOrApplicant(1L) } returns listOf(myProject)
                        every {
                            mockEditRequestRepository.findAllByOriginalProjectIdInAndRequestStatus(
                                listOf(10L),
                                ProjectRequestStatus.PENDING,
                            )
                        } returns emptyList()
                        every { mockEditRequestRepository.findAllByParticipantOrRequester(1L) } returns emptyList()
                    }

                    it("ACCEPTED 상태와 OWNER 역할로 조회되어야 한다") {
                        val result = queryMyProjectService.execute(QueryMyProjectReqDto())

                        result.totalElements shouldBe 1
                        result.projects[0].projectId shouldBe 10L
                        result.projects[0].requestStatus shouldBe ProjectRequestStatus.ACCEPTED
                        result.projects[0].role shouldBe ProjectMemberRole.OWNER
                    }
                }

                context("타인이 신청한 프로젝트에 참여자로 등록되어 있을 때") {
                    beforeEach {
                        val participatingProject =
                            ProjectJpaEntity().apply {
                                id = 20L
                                name = "참여 프로젝트"
                                description = "설명"
                                startYear = 2024
                                status = ProjectStatus.ACTIVE
                                appliedBy = other
                                participants = mutableSetOf(me)
                            }

                        every { mockProjectRepository.findAllByParticipantOrApplicant(1L) } returns listOf(participatingProject)
                        every {
                            mockEditRequestRepository.findAllByOriginalProjectIdInAndRequestStatus(
                                listOf(20L),
                                ProjectRequestStatus.PENDING,
                            )
                        } returns emptyList()
                        every { mockEditRequestRepository.findAllByParticipantOrRequester(1L) } returns emptyList()
                    }

                    it("PARTICIPANT 역할로 조회되어야 한다") {
                        val result = queryMyProjectService.execute(QueryMyProjectReqDto())

                        result.projects[0].role shouldBe ProjectMemberRole.PARTICIPANT
                    }
                }

                context("승인된 프로젝트에 대기 중인 수정 신청이 있을 때") {
                    beforeEach {
                        val approvedProject =
                            ProjectJpaEntity().apply {
                                id = 30L
                                name = "기존 이름"
                                description = "기존 설명"
                                startYear = 2022
                                status = ProjectStatus.ACTIVE
                                appliedBy = me
                            }

                        val pendingRequest =
                            ProjectEditRequestJpaEntity().apply {
                                id = 300L
                                originalProject = approvedProject
                                requestedBy = me
                                name = "수정 요청한 이름"
                                description = "수정 요청한 설명"
                                startYear = 2024
                                requestStatus = ProjectRequestStatus.PENDING
                            }

                        every { mockProjectRepository.findAllByParticipantOrApplicant(1L) } returns listOf(approvedProject)
                        every {
                            mockEditRequestRepository.findAllByOriginalProjectIdInAndRequestStatus(
                                listOf(30L),
                                ProjectRequestStatus.PENDING,
                            )
                        } returns listOf(pendingRequest)
                        every { mockEditRequestRepository.findAllByParticipantOrRequester(1L) } returns listOf(pendingRequest)
                    }

                    it("중복 없이 1건으로 PENDING 상태와 수정안 내용이 노출되어야 한다") {
                        val result = queryMyProjectService.execute(QueryMyProjectReqDto())

                        result.totalElements shouldBe 1
                        result.projects[0].projectId shouldBe 30L
                        result.projects[0].requestId shouldBe 300L
                        result.projects[0].requestStatus shouldBe ProjectRequestStatus.PENDING
                        result.projects[0].name shouldBe "수정 요청한 이름"
                    }
                }

                context("승인된 프로젝트에 과거 거절된 수정 신청 이력이 있을 때") {
                    beforeEach {
                        val approvedProject =
                            ProjectJpaEntity().apply {
                                id = 40L
                                name = "내 프로젝트"
                                description = "설명"
                                startYear = 2024
                                status = ProjectStatus.ACTIVE
                                appliedBy = me
                            }

                        val rejectedRequest =
                            ProjectEditRequestJpaEntity().apply {
                                id = 401L
                                originalProject = approvedProject
                                requestedBy = me
                                name = "거절된 수정안"
                                description = "설명"
                                startYear = 2024
                                requestStatus = ProjectRequestStatus.REJECTED
                                rejectReason = "사유"
                            }

                        every { mockProjectRepository.findAllByParticipantOrApplicant(1L) } returns listOf(approvedProject)
                        every {
                            mockEditRequestRepository.findAllByOriginalProjectIdInAndRequestStatus(
                                listOf(40L),
                                ProjectRequestStatus.PENDING,
                            )
                        } returns emptyList()
                        every { mockEditRequestRepository.findAllByParticipantOrRequester(1L) } returns listOf(rejectedRequest)
                    }

                    it("거절 이력 때문에 같은 프로젝트가 중복 노출되지 않아야 한다") {
                        val result = queryMyProjectService.execute(QueryMyProjectReqDto())

                        result.totalElements shouldBe 1
                        result.projects[0].projectId shouldBe 40L
                    }

                    it("원본 1건에 거절 사유가 실려 확인할 수 있어야 한다") {
                        val result = queryMyProjectService.execute(QueryMyProjectReqDto())

                        result.projects[0].requestStatus shouldBe ProjectRequestStatus.REJECTED
                        result.projects[0].rejectReason shouldBe "사유"
                        result.projects[0].requestId shouldBe 401L
                        result.projects[0].name shouldBe "내 프로젝트"
                    }

                    it("REJECTED 필터로도 조회되어야 한다") {
                        val result =
                            queryMyProjectService.execute(
                                QueryMyProjectReqDto(requestStatus = ProjectRequestStatus.REJECTED),
                            )

                        result.totalElements shouldBe 1
                    }
                }

                context("참여자가 올린 수정 신청이 대기 중인 프로젝트를 소유자가 조회할 때") {
                    beforeEach {
                        val approvedProject =
                            ProjectJpaEntity().apply {
                                id = 50L
                                name = "내 프로젝트"
                                description = "설명"
                                startYear = 2024
                                status = ProjectStatus.ACTIVE
                                appliedBy = me
                                participants = mutableSetOf(other)
                            }

                        val pendingByOther =
                            ProjectEditRequestJpaEntity().apply {
                                id = 501L
                                originalProject = approvedProject
                                requestedBy = other
                                name = "참여자가 올린 수정안"
                                description = "설명"
                                startYear = 2024
                                requestStatus = ProjectRequestStatus.PENDING
                            }

                        every { mockProjectRepository.findAllByParticipantOrApplicant(1L) } returns listOf(approvedProject)
                        every {
                            mockEditRequestRepository.findAllByOriginalProjectIdInAndRequestStatus(
                                listOf(50L),
                                ProjectRequestStatus.PENDING,
                            )
                        } returns listOf(pendingByOther)
                        every { mockEditRequestRepository.findAllByParticipantOrRequester(1L) } returns emptyList()
                    }

                    it("수정안 작성자가 아니어도 소유자 역할이 유지되어야 한다") {
                        val result = queryMyProjectService.execute(QueryMyProjectReqDto())

                        result.projects[0].role shouldBe ProjectMemberRole.OWNER
                    }
                }

                context("아직 승인되지 않은 신규 생성 신청이 있을 때") {
                    beforeEach {
                        val newRequest =
                            ProjectEditRequestJpaEntity().apply {
                                id = 400L
                                originalProject = null
                                requestedBy = me
                                name = "신규 신청 프로젝트"
                                description = "설명"
                                startYear = 2024
                                requestStatus = ProjectRequestStatus.PENDING
                            }

                        every { mockProjectRepository.findAllByParticipantOrApplicant(1L) } returns emptyList()
                        every {
                            mockEditRequestRepository.findAllByOriginalProjectIdInAndRequestStatus(
                                emptyList(),
                                ProjectRequestStatus.PENDING,
                            )
                        } returns emptyList()
                        every { mockEditRequestRepository.findAllByParticipantOrRequester(1L) } returns listOf(newRequest)
                    }

                    it("프로젝트 ID 없이 신청 정보만으로 조회되어야 한다") {
                        val result = queryMyProjectService.execute(QueryMyProjectReqDto())

                        result.totalElements shouldBe 1
                        result.projects[0].projectId shouldBe null
                        result.projects[0].requestId shouldBe 400L
                        result.projects[0].requestStatus shouldBe ProjectRequestStatus.PENDING
                        result.projects[0].status shouldBe null
                    }
                }

                context("거절된 신청이 있을 때") {
                    beforeEach {
                        val rejectedRequest =
                            ProjectEditRequestJpaEntity().apply {
                                id = 500L
                                originalProject = null
                                requestedBy = me
                                name = "거절된 프로젝트"
                                description = "설명"
                                startYear = 2024
                                requestStatus = ProjectRequestStatus.REJECTED
                                rejectReason = "리포지토리 링크가 유효하지 않습니다."
                            }

                        every { mockProjectRepository.findAllByParticipantOrApplicant(1L) } returns emptyList()
                        every {
                            mockEditRequestRepository.findAllByOriginalProjectIdInAndRequestStatus(
                                emptyList(),
                                ProjectRequestStatus.PENDING,
                            )
                        } returns emptyList()
                        every { mockEditRequestRepository.findAllByParticipantOrRequester(1L) } returns listOf(rejectedRequest)
                    }

                    it("거절 사유와 함께 조회되어야 한다") {
                        val result = queryMyProjectService.execute(QueryMyProjectReqDto())

                        result.projects[0].requestStatus shouldBe ProjectRequestStatus.REJECTED
                        result.projects[0].rejectReason shouldBe "리포지토리 링크가 유효하지 않습니다."
                    }

                    it("ACCEPTED 필터로 조회하면 결과에서 제외되어야 한다") {
                        val result =
                            queryMyProjectService.execute(
                                QueryMyProjectReqDto(requestStatus = ProjectRequestStatus.ACCEPTED),
                            )

                        result.totalElements shouldBe 0
                    }
                }
            }
        }
    })
