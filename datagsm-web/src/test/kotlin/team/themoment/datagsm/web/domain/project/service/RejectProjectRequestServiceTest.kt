package team.themoment.datagsm.web.domain.project.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import team.themoment.datagsm.common.domain.project.dto.request.RejectProjectRequestReqDto
import team.themoment.datagsm.common.domain.project.entity.ProjectEditRequestJpaEntity
import team.themoment.datagsm.common.domain.project.entity.ProjectJpaEntity
import team.themoment.datagsm.common.domain.project.entity.constant.ProjectRequestStatus
import team.themoment.datagsm.common.domain.project.entity.constant.ProjectStatus
import team.themoment.datagsm.common.domain.project.repository.ProjectEditRequestJpaRepository
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.common.domain.student.entity.constant.Sex
import team.themoment.datagsm.web.domain.project.service.impl.RejectProjectRequestServiceImpl
import team.themoment.sdk.exception.ExpectedException
import java.util.Optional

class RejectProjectRequestServiceTest :
    DescribeSpec({

        val mockEditRequestRepository = mockk<ProjectEditRequestJpaRepository>()

        val rejectProjectRequestService = RejectProjectRequestServiceImpl(mockEditRequestRepository)

        val requestId = 100L
        val reqDto = RejectProjectRequestReqDto(reason = "리포지토리 링크가 유효하지 않습니다.")

        val applicant =
            StudentJpaEntity().apply {
                id = 1L
                name = "홍길동"
                email = "s24080@gsm.hs.kr"
                sex = Sex.MAN
            }

        beforeEach {
            every { mockEditRequestRepository.save(any<ProjectEditRequestJpaEntity>()) } answers { firstArg() }
        }

        afterEach {
            clearAllMocks()
        }

        describe("RejectProjectRequestService 클래스의") {
            describe("execute 메서드는") {

                context("대기 중인 신청을 거절할 때") {
                    lateinit var pendingRequest: ProjectEditRequestJpaEntity
                    lateinit var originalProject: ProjectJpaEntity

                    beforeEach {
                        originalProject =
                            ProjectJpaEntity().apply {
                                id = 1L
                                name = "기존 프로젝트"
                                description = "기존 설명"
                                startYear = 2022
                                status = ProjectStatus.ACTIVE
                            }

                        pendingRequest =
                            ProjectEditRequestJpaEntity().apply {
                                id = requestId
                                this.originalProject = originalProject
                                requestedBy = applicant
                                name = "수정된 프로젝트"
                                description = "수정된 설명"
                                startYear = 2024
                                requestStatus = ProjectRequestStatus.PENDING
                            }

                        every { mockEditRequestRepository.findById(requestId) } returns Optional.of(pendingRequest)
                    }

                    it("신청 상태가 REJECTED로 바뀌고 사유가 기록되어야 한다") {
                        rejectProjectRequestService.execute(requestId, reqDto)

                        pendingRequest.requestStatus shouldBe ProjectRequestStatus.REJECTED
                        pendingRequest.rejectReason shouldBe "리포지토리 링크가 유효하지 않습니다."
                        pendingRequest.processedAt shouldNotBe null
                        verify(exactly = 1) { mockEditRequestRepository.save(pendingRequest) }
                    }

                    it("원본 프로젝트는 변경되지 않아야 한다") {
                        rejectProjectRequestService.execute(requestId, reqDto)

                        originalProject.name shouldBe "기존 프로젝트"
                        originalProject.description shouldBe "기존 설명"
                        originalProject.startYear shouldBe 2022
                    }
                }

                context("이미 처리된 신청을 거절할 때") {
                    beforeEach {
                        val processedRequest =
                            ProjectEditRequestJpaEntity().apply {
                                id = requestId
                                requestedBy = applicant
                                name = "프로젝트"
                                description = "설명"
                                startYear = 2024
                                requestStatus = ProjectRequestStatus.REJECTED
                            }

                        every { mockEditRequestRepository.findById(requestId) } returns Optional.of(processedRequest)
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                rejectProjectRequestService.execute(requestId, reqDto)
                            }

                        exception.message shouldBe "이미 처리된 신청입니다."
                        verify(exactly = 0) { mockEditRequestRepository.save(any<ProjectEditRequestJpaEntity>()) }
                    }
                }

                context("존재하지 않는 신청 ID로 거절할 때") {
                    beforeEach {
                        every { mockEditRequestRepository.findById(999L) } returns Optional.empty()
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                rejectProjectRequestService.execute(999L, reqDto)
                            }

                        exception.message shouldBe "신청 내역을 찾을 수 없습니다."
                    }
                }
            }
        }
    })
