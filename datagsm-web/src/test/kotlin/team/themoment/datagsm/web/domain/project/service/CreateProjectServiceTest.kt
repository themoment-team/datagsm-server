package team.themoment.datagsm.web.domain.project.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import team.themoment.datagsm.common.domain.club.entity.ClubJpaEntity
import team.themoment.datagsm.common.domain.club.entity.constant.ClubType
import team.themoment.datagsm.common.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.common.domain.project.dto.request.ProjectReqDto
import team.themoment.datagsm.common.domain.project.entity.ProjectJpaEntity
import team.themoment.datagsm.common.domain.project.repository.ProjectJpaRepository
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.common.domain.student.entity.constant.Sex
import team.themoment.datagsm.web.domain.project.service.impl.CreateProjectServiceImpl
import team.themoment.sdk.exception.ExpectedException
import java.util.Optional

class CreateProjectServiceTest :
    DescribeSpec({

        val mockProjectRepository = mockk<ProjectJpaRepository>()
        val mockClubRepository = mockk<ClubJpaRepository>()
        val mockStudentRepository = mockk<team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository>()

        val createProjectService = CreateProjectServiceImpl(mockProjectRepository, mockClubRepository, mockStudentRepository)

        afterEach {
            clearAllMocks()
        }

        describe("CreateProjectService 클래스의") {
            describe("execute 메서드는") {

                context("유효한 프로젝트 정보로 생성 요청할 때") {
                    val createRequest =
                        ProjectReqDto(
                            name = "DataGSM 프로젝트",
                            description = "학교 데이터를 제공하는 API 서비스",
                            clubId = 1L,
                            participantIds = emptyList(),
                        )

                    val ownerClub =
                        ClubJpaEntity().apply {
                            id = 1L
                            name = "SW개발동아리"
                            type = ClubType.MAJOR_CLUB
                        }

                    val savedProject =
                        ProjectJpaEntity().apply {
                            id = 1L
                            name = createRequest.name
                            description = createRequest.description
                            this.club = ownerClub
                        }

                    beforeEach {
                        every { mockProjectRepository.existsByName(createRequest.name) } returns false
                        every { mockClubRepository.findById(createRequest.clubId!!) } returns Optional.of(ownerClub)
                        every { mockProjectRepository.save(any()) } returns savedProject
                    }

                    it("새로운 프로젝트를 생성하고 저장 후 결과를 반환한다") {
                        val result = createProjectService.execute(createRequest)

                        result.id shouldBe 1L
                        result.name shouldBe "DataGSM 프로젝트"
                        result.description shouldBe "학교 데이터를 제공하는 API 서비스"
                        result.club?.id shouldBe 1L
                        result.club?.name shouldBe "SW개발동아리"
                        result.club?.type shouldBe ClubType.MAJOR_CLUB
                        result.participants shouldBe emptyList()

                        verify(exactly = 1) { mockProjectRepository.existsByName(createRequest.name) }
                        verify(exactly = 1) { mockClubRepository.findById(createRequest.clubId!!) }
                        verify(exactly = 1) { mockProjectRepository.save(any()) }
                    }
                }

                context("이미 존재하는 프로젝트 이름으로 생성 요청할 때") {
                    val createRequest =
                        ProjectReqDto(
                            name = "중복프로젝트",
                            description = "중복된 프로젝트입니다",
                            clubId = 1L,
                            participantIds = emptyList(),
                        )

                    beforeEach {
                        every { mockProjectRepository.existsByName(createRequest.name) } returns true
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                createProjectService.execute(createRequest)
                            }

                        exception.message shouldBe "이미 존재하는 프로젝트 이름입니다."

                        verify(exactly = 1) { mockProjectRepository.existsByName(createRequest.name) }
                        verify(exactly = 0) { mockClubRepository.findById(any()) }
                        verify(exactly = 0) { mockProjectRepository.save(any()) }
                    }
                }

                context("존재하지 않는 동아리 ID로 생성 요청할 때") {
                    val createRequest =
                        ProjectReqDto(
                            name = "신규프로젝트",
                            description = "신규 프로젝트입니다",
                            clubId = 999L,
                            participantIds = emptyList(),
                        )

                    beforeEach {
                        every { mockProjectRepository.existsByName(createRequest.name) } returns false
                        every { mockClubRepository.findById(createRequest.clubId!!) } returns Optional.empty()
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                createProjectService.execute(createRequest)
                            }

                        exception.message shouldBe "동아리를 찾을 수 없습니다."

                        verify(exactly = 1) { mockProjectRepository.existsByName(createRequest.name) }
                        verify(exactly = 1) { mockClubRepository.findById(createRequest.clubId!!) }
                        verify(exactly = 0) { mockProjectRepository.save(any()) }
                    }
                }

                context("여러 타입의 동아리로 프로젝트를 생성할 때") {
                    val createRequest =
                        ProjectReqDto(
                            name = "자율동아리 프로젝트",
                            description = "자율동아리 프로젝트입니다",
                            clubId = 2L,
                            participantIds = emptyList(),
                        )

                    val ownerClub =
                        ClubJpaEntity().apply {
                            id = 2L
                            name = "자율동아리"
                            type = ClubType.AUTONOMOUS_CLUB
                        }

                    val savedProject =
                        ProjectJpaEntity().apply {
                            id = 2L
                            name = createRequest.name
                            description = createRequest.description
                            this.club = ownerClub
                        }

                    beforeEach {
                        every { mockProjectRepository.existsByName(createRequest.name) } returns false
                        every { mockClubRepository.findById(createRequest.clubId!!) } returns Optional.of(ownerClub)
                        every { mockProjectRepository.save(any()) } returns savedProject
                    }

                    it("자율동아리 타입으로 프로젝트가 생성되어야 한다") {
                        val result = createProjectService.execute(createRequest)

                        result.id shouldBe 2L
                        result.club?.type shouldBe ClubType.AUTONOMOUS_CLUB
                        result.club?.name shouldBe "자율동아리"
                    }
                }

                context("유효한 참여자 ID로 생성 요청할 때") {
                    val participant =
                        StudentJpaEntity().apply {
                            id = 1L
                            name = "홍길동"
                            email = "hong@gsm.hs.kr"
                            sex = Sex.MAN
                        }

                    val createRequest =
                        ProjectReqDto(
                            name = "참여자 있는 프로젝트",
                            description = "참여자가 있는 프로젝트입니다",
                            clubId = 1L,
                            participantIds = listOf(1L),
                        )

                    val ownerClub =
                        ClubJpaEntity().apply {
                            id = 1L
                            name = "SW개발동아리"
                            type = ClubType.MAJOR_CLUB
                        }

                    val savedProject =
                        ProjectJpaEntity().apply {
                            id = 3L
                            name = createRequest.name
                            description = createRequest.description
                            this.club = ownerClub
                            this.participants = mutableSetOf(participant)
                        }

                    beforeEach {
                        every { mockProjectRepository.existsByName(createRequest.name) } returns false
                        every { mockClubRepository.findById(createRequest.clubId!!) } returns Optional.of(ownerClub)
                        every { mockStudentRepository.findAllById(listOf(1L)) } returns listOf(participant)
                        every { mockProjectRepository.save(any()) } returns savedProject
                    }

                    it("참여자가 포함된 프로젝트가 생성되어야 한다") {
                        val result = createProjectService.execute(createRequest)

                        result.participants.size shouldBe 1
                        result.participants[0].id shouldBe 1L
                        result.participants[0].name shouldBe "홍길동"

                        verify(exactly = 1) { mockStudentRepository.findAllById(listOf(1L)) }
                        verify(exactly = 1) { mockProjectRepository.save(any()) }
                    }
                }

                context("동아리 없이 프로젝트를 생성할 때") {
                    val createRequest =
                        ProjectReqDto(
                            name = "동아리없는프로젝트",
                            description = "동아리가 없는 프로젝트입니다",
                            clubId = null,
                            participantIds = emptyList(),
                        )

                    val savedProject =
                        ProjectJpaEntity().apply {
                            id = 4L
                            name = createRequest.name
                            description = createRequest.description
                            this.club = null
                        }

                    beforeEach {
                        every { mockProjectRepository.existsByName(createRequest.name) } returns false
                        every { mockProjectRepository.save(any()) } returns savedProject
                    }

                    it("동아리 없이 프로젝트가 생성되어야 한다") {
                        val result = createProjectService.execute(createRequest)

                        result.id shouldBe 4L
                        result.name shouldBe "동아리없는프로젝트"
                        result.club shouldBe null

                        verify(exactly = 1) { mockProjectRepository.existsByName(createRequest.name) }
                        verify(exactly = 0) { mockClubRepository.findById(any()) }
                        verify(exactly = 1) { mockProjectRepository.save(any()) }
                    }
                }

                context("존재하지 않는 참여자 ID로 생성 요청할 때") {
                    val createRequest =
                        ProjectReqDto(
                            name = "잘못된 참여자 프로젝트",
                            description = "존재하지 않는 참여자를 포함한 프로젝트입니다",
                            clubId = 1L,
                            participantIds = listOf(999L),
                        )

                    val ownerClub =
                        ClubJpaEntity().apply {
                            id = 1L
                            name = "SW개발동아리"
                            type = ClubType.MAJOR_CLUB
                        }

                    beforeEach {
                        every { mockProjectRepository.existsByName(createRequest.name) } returns false
                        every { mockClubRepository.findById(createRequest.clubId!!) } returns Optional.of(ownerClub)
                        every { mockStudentRepository.findAllById(listOf(999L)) } returns emptyList()
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                createProjectService.execute(createRequest)
                            }

                        exception.message shouldBe "해당 학생 데이터를 찾을 수 없습니다."

                        verify(exactly = 1) { mockStudentRepository.findAllById(listOf(999L)) }
                        verify(exactly = 0) { mockProjectRepository.save(any()) }
                    }
                }
            }
        }
    })
