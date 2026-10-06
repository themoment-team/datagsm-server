package team.themoment.datagsm.web.domain.project.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import team.themoment.datagsm.common.domain.club.entity.ClubJpaEntity
import team.themoment.datagsm.common.domain.club.entity.constant.ClubType
import team.themoment.datagsm.common.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.common.domain.project.dto.request.ApplyProjectReqDto
import team.themoment.datagsm.common.domain.project.entity.ProjectEditRequestJpaEntity
import team.themoment.datagsm.common.domain.project.entity.constant.ProjectRequestStatus
import team.themoment.datagsm.common.domain.project.repository.ProjectEditRequestJpaRepository
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.common.domain.student.entity.constant.Sex
import team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.web.domain.project.mapper.ProjectApplicationAssembler
import team.themoment.datagsm.web.domain.project.mapper.ProjectEditRequestMapper
import team.themoment.datagsm.web.domain.project.service.impl.ApplyProjectServiceImpl
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider
import team.themoment.datagsm.web.global.storage.ProjectIconStorage
import team.themoment.sdk.exception.ExpectedException
import java.util.Optional

class ApplyProjectServiceTest :
    DescribeSpec({

        val mockEditRequestRepository = mockk<ProjectEditRequestJpaRepository>()
        val mockClubRepository = mockk<ClubJpaRepository>()
        val mockStudentRepository = mockk<StudentJpaRepository>()
        val mockIconStorage = mockk<ProjectIconStorage>()
        val mockCurrentUserProvider = mockk<CurrentUserProvider>()

        val assembler = ProjectApplicationAssembler(mockClubRepository, mockStudentRepository, mockIconStorage)
        val mapper = ProjectEditRequestMapper(mockIconStorage)

        val applyProjectService =
            ApplyProjectServiceImpl(mockEditRequestRepository, assembler, mapper, mockCurrentUserProvider)

        val applicant =
            StudentJpaEntity().apply {
                id = 1L
                name = "홍길동"
                email = "s24080@gsm.hs.kr"
                sex = Sex.MAN
            }

        val ownerClub =
            ClubJpaEntity().apply {
                id = 10L
                name = "SW개발동아리"
                type = ClubType.MAJOR_CLUB
            }

        beforeEach {
            every { mockCurrentUserProvider.getCurrentStudent() } returns applicant
            every { mockIconStorage.validateIconKey(any()) } answers { firstArg() }
            every { mockIconStorage.toIconUrl(any()) } returns null
            every { mockIconStorage.resolveIconKeyForUpdate(any(), any()) } answers {
                val requested = firstArg<String?>()
                when {
                    requested == null -> secondArg()
                    requested.isBlank() -> null
                    else -> requested
                }
            }
            every { mockEditRequestRepository.save(any<ProjectEditRequestJpaEntity>()) } answers {
                firstArg<ProjectEditRequestJpaEntity>().apply { if (id == null) id = 100L }
            }
        }

        afterEach {
            clearAllMocks()
        }

        describe("ApplyProjectService 클래스의") {
            describe("execute 메서드는") {

                context("유효한 신청 정보가 주어질 때") {
                    val reqDto =
                        ApplyProjectReqDto(
                            name = "DataGSM 프로젝트",
                            description = "학교 데이터를 제공하는 API 서비스",
                            startYear = 2024,
                            clubId = 10L,
                            participantIds = listOf(1L),
                            repositories = listOf("https://github.com/team/repo"),
                            techStacks = listOf("Kotlin"),
                        )

                    beforeEach {
                        every { mockClubRepository.findById(10L) } returns Optional.of(ownerClub)
                        every { mockStudentRepository.findAllById(listOf(1L)) } returns listOf(applicant)
                    }

                    it("PENDING 상태의 신규 생성 신청이 저장되어야 한다") {
                        val captured = slot<ProjectEditRequestJpaEntity>()

                        val result = applyProjectService.execute(reqDto)

                        verify(exactly = 1) { mockEditRequestRepository.save(capture(captured)) }
                        captured.captured.originalProject shouldBe null
                        captured.captured.requestStatus shouldBe ProjectRequestStatus.PENDING
                        captured.captured.requestedBy shouldBe applicant
                        captured.captured.repositories shouldContainExactlyInAnyOrder listOf("https://github.com/team/repo")

                        result.id shouldBe 100L
                        result.originalProjectId shouldBe null
                        result.requestStatus shouldBe ProjectRequestStatus.PENDING
                    }
                }

                context("이미 다른 신규 신청이 있을 때") {
                    val reqDto =
                        ApplyProjectReqDto(
                            name = "두 번째 프로젝트",
                            description = "다른 프로젝트 설명",
                            startYear = 2024,
                        )

                    it("기존 신청을 건드리지 않고 새 행을 만들어야 한다") {
                        val captured = slot<ProjectEditRequestJpaEntity>()

                        val result = applyProjectService.execute(reqDto)

                        verify(exactly = 1) { mockEditRequestRepository.save(capture(captured)) }
                        captured.captured.id shouldBe 100L
                        captured.captured.name shouldBe "두 번째 프로젝트"
                        result.requestStatus shouldBe ProjectRequestStatus.PENDING
                    }

                    it("아이콘을 생략해도 이전 신청의 값이 승계되지 않아야 한다") {
                        val captured = slot<ProjectEditRequestJpaEntity>()

                        applyProjectService.execute(reqDto)

                        verify(exactly = 1) { mockEditRequestRepository.save(capture(captured)) }
                        captured.captured.iconKey shouldBe null
                        captured.captured.deploymentUrl shouldBe null
                    }
                }

                context("무소속을 뜻하는 동아리 ID 0이 주어질 때") {
                    val reqDto =
                        ApplyProjectReqDto(
                            name = "개인 프로젝트",
                            description = "무소속 프로젝트",
                            startYear = 2024,
                            clubId = 0L,
                        )

                    it("동아리 조회 없이 동아리가 비어 있는 신청이 저장되어야 한다") {
                        val result = applyProjectService.execute(reqDto)

                        verify(exactly = 0) { mockClubRepository.findById(any()) }
                        result.club shouldBe null
                    }
                }

                context("존재하지 않는 동아리 ID가 주어질 때") {
                    val reqDto =
                        ApplyProjectReqDto(
                            name = "프로젝트",
                            description = "설명",
                            startYear = 2024,
                            clubId = 999L,
                        )

                    beforeEach {
                        every { mockClubRepository.findById(999L) } returns Optional.empty()
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                applyProjectService.execute(reqDto)
                            }

                        exception.message shouldBe "동아리를 찾을 수 없습니다."
                        verify(exactly = 0) { mockEditRequestRepository.save(any<ProjectEditRequestJpaEntity>()) }
                    }
                }

                context("존재하지 않는 참여자 ID가 포함될 때") {
                    val reqDto =
                        ApplyProjectReqDto(
                            name = "프로젝트",
                            description = "설명",
                            startYear = 2024,
                            participantIds = listOf(1L, 999L),
                        )

                    beforeEach {
                        every { mockStudentRepository.findAllById(listOf(1L, 999L)) } returns listOf(applicant)
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                applyProjectService.execute(reqDto)
                            }

                        exception.message shouldBe "해당 학생 데이터를 찾을 수 없습니다."
                        verify(exactly = 0) { mockEditRequestRepository.save(any<ProjectEditRequestJpaEntity>()) }
                    }
                }
            }
        }
    })
