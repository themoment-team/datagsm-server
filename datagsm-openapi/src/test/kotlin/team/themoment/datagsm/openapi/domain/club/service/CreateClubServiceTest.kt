package team.themoment.datagsm.openapi.domain.club.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import team.themoment.datagsm.common.domain.club.dto.request.ClubReqDto
import team.themoment.datagsm.common.domain.club.entity.ClubJpaEntity
import team.themoment.datagsm.common.domain.club.entity.constant.ClubStatus
import team.themoment.datagsm.common.domain.club.entity.constant.ClubType
import team.themoment.datagsm.common.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.common.domain.student.entity.StudentNumber
import team.themoment.datagsm.common.domain.student.entity.constant.Major
import team.themoment.datagsm.common.domain.student.entity.constant.Sex
import team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.openapi.domain.club.service.impl.CreateClubServiceImpl
import team.themoment.sdk.exception.ExpectedException

class CreateClubServiceTest :
    DescribeSpec({

        lateinit var mockClubRepository: ClubJpaRepository
        lateinit var mockStudentRepository: StudentJpaRepository
        lateinit var createClubService: CreateClubService

        beforeEach {
            mockClubRepository = mockk<ClubJpaRepository>()
            mockStudentRepository = mockk<StudentJpaRepository>()
            createClubService = CreateClubServiceImpl(mockClubRepository, mockStudentRepository)
        }

        describe("CreateClubService 클래스의") {
            describe("execute 메서드는") {
                context("중복된 동아리 이름으로 생성할 때") {
                    val req =
                        ClubReqDto(
                            name = "동아리A",
                            type = ClubType.MAJOR_CLUB,
                            leaderId = 1L,
                            participantIds = listOf(2L),
                            foundedYear = 2022,
                            status = ClubStatus.ACTIVE,
                        )

                    beforeEach {
                        every { mockClubRepository.existsByName(req.name) } returns true
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val ex =
                            shouldThrow<ExpectedException> {
                                createClubService.execute(req)
                            }
                        ex.message shouldBe "이미 존재하는 동아리 이름입니다."

                        verify(exactly = 1) { mockClubRepository.existsByName(req.name) }
                        verify(exactly = 0) { mockClubRepository.save(any()) }
                    }
                }

                context("정상적으로 동아리를 생성할 때") {
                    val req =
                        ClubReqDto(
                            name = "동아리B",
                            type = ClubType.AUTONOMOUS_CLUB,
                            leaderId = 100L,
                            participantIds = listOf(200L, 300L),
                            foundedYear = 2022,
                            status = ClubStatus.ACTIVE,
                        )
                    lateinit var mockLeader: StudentJpaEntity
                    lateinit var participant1: StudentJpaEntity
                    lateinit var participant2: StudentJpaEntity

                    beforeEach {
                        mockLeader =
                            StudentJpaEntity().apply {
                                this.id = 100L
                                this.name = "부장이름"
                                this.email = "leader@gsm.hs.kr"
                                this.studentNumber = StudentNumber(2, 1, 5)
                                this.major = Major.AI
                                this.sex = Sex.WOMAN
                            }
                        participant1 =
                            StudentJpaEntity().apply {
                                this.id = 200L
                                this.name = "부원1"
                                this.email = "p1@gsm.hs.kr"
                                this.studentNumber = StudentNumber(2, 1, 6)
                                this.major = Major.AI
                                this.sex = Sex.MAN
                            }
                        participant2 =
                            StudentJpaEntity().apply {
                                this.id = 300L
                                this.name = "부원2"
                                this.email = "p2@gsm.hs.kr"
                                this.studentNumber = StudentNumber(2, 1, 7)
                                this.major = Major.AI
                                this.sex = Sex.WOMAN
                            }
                        every { mockClubRepository.existsByName(req.name) } returns false
                        every { mockStudentRepository.findById(req.leaderId!!) } returns java.util.Optional.of(mockLeader)
                        every { mockClubRepository.save(any()) } answers {
                            val entity = firstArg<ClubJpaEntity>()
                            entity.apply { this.id = 10L }
                        }
                        every { mockStudentRepository.findAllById(listOf(200L, 300L)) } returns listOf(participant1, participant2)
                        every { mockClubRepository.findAllByLeaderIn(any()) } returns emptyList()
                        every { mockStudentRepository.bulkAssignClub(any(), any(), any()) } just Runs
                    }

                    it("생성된 동아리 정보와 부원 목록을 반환해야 한다") {
                        val res = createClubService.execute(req)

                        res.name shouldBe req.name
                        res.type shouldBe req.type
                        res.leader?.id shouldBe 100L
                        res.leader?.name shouldBe "부장이름"
                        res.participants.size shouldBe 2

                        verify(exactly = 1) { mockClubRepository.existsByName(req.name) }
                        verify(exactly = 1) { mockStudentRepository.findById(req.leaderId!!) }
                        verify(exactly = 1) { mockClubRepository.save(any()) }
                        verify(exactly = 1) { mockStudentRepository.findAllById(listOf(200L, 300L)) }
                        verify(exactly = 1) { mockStudentRepository.bulkAssignClub(any(), any(), any()) }
                    }
                }

                context("participantIds에 leaderId가 포함될 때") {
                    val req =
                        ClubReqDto(
                            name = "동아리C",
                            type = ClubType.MAJOR_CLUB,
                            leaderId = 100L,
                            participantIds = listOf(100L, 200L),
                            foundedYear = 2022,
                            status = ClubStatus.ACTIVE,
                        )
                    lateinit var mockLeader: StudentJpaEntity
                    lateinit var participant: StudentJpaEntity

                    beforeEach {
                        mockLeader =
                            StudentJpaEntity().apply {
                                this.id = 100L
                                this.name = "부장이름"
                                this.email = "leader@gsm.hs.kr"
                                this.studentNumber = StudentNumber(2, 1, 5)
                                this.major = Major.AI
                                this.sex = Sex.WOMAN
                            }
                        participant =
                            StudentJpaEntity().apply {
                                this.id = 200L
                                this.name = "부원"
                                this.email = "p@gsm.hs.kr"
                                this.studentNumber = StudentNumber(2, 1, 6)
                                this.major = Major.AI
                                this.sex = Sex.MAN
                            }
                        every { mockClubRepository.existsByName(req.name) } returns false
                        every { mockStudentRepository.findById(req.leaderId!!) } returns java.util.Optional.of(mockLeader)
                        every { mockClubRepository.save(any()) } answers {
                            val entity = firstArg<ClubJpaEntity>()
                            entity.apply { this.id = 10L }
                        }
                        every { mockStudentRepository.findAllById(listOf(200L)) } returns listOf(participant)
                        every { mockClubRepository.findAllByLeaderIn(any()) } returns emptyList()
                        every { mockStudentRepository.bulkAssignClub(any(), any(), any()) } just Runs
                    }

                    it("participants에 leader가 포함되지 않아야 한다") {
                        val res = createClubService.execute(req)

                        res.participants.none { it.id == 100L } shouldBe true
                        res.participants.size shouldBe 1
                        res.participants[0].id shouldBe 200L
                    }
                }

                context("동아리 생성 시 부장과 부원이 배정될 때") {
                    val req =
                        ClubReqDto(
                            name = "동아리D",
                            type = ClubType.MAJOR_CLUB,
                            leaderId = 100L,
                            participantIds = listOf(200L),
                            foundedYear = 2022,
                            status = ClubStatus.ACTIVE,
                        )
                    lateinit var mockLeader: StudentJpaEntity
                    lateinit var participant: StudentJpaEntity

                    beforeEach {
                        mockLeader =
                            StudentJpaEntity().apply {
                                this.id = 100L
                                this.name = "부장이름"
                                this.email = "leader@gsm.hs.kr"
                                this.studentNumber = StudentNumber(2, 1, 5)
                                this.major = Major.AI
                                this.sex = Sex.WOMAN
                            }
                        participant =
                            StudentJpaEntity().apply {
                                this.id = 200L
                                this.name = "부원"
                                this.email = "p@gsm.hs.kr"
                                this.studentNumber = StudentNumber(2, 1, 6)
                                this.major = Major.AI
                                this.sex = Sex.MAN
                            }
                        every { mockClubRepository.existsByName(req.name) } returns false
                        every { mockStudentRepository.findById(req.leaderId!!) } returns java.util.Optional.of(mockLeader)
                        every { mockClubRepository.save(any()) } answers {
                            val entity = firstArg<ClubJpaEntity>()
                            entity.apply { this.id = 10L }
                        }
                        every { mockStudentRepository.findAllById(listOf(200L)) } returns listOf(participant)
                        every { mockClubRepository.findAllByLeaderIn(any()) } returns emptyList()
                        every { mockStudentRepository.bulkAssignClub(any(), any(), any()) } just Runs
                    }

                    it("bulkAssignClub이 부장과 부원 ID를 포함해 호출되어야 한다") {
                        createClubService.execute(req)

                        verify(exactly = 1) {
                            mockStudentRepository.bulkAssignClub(
                                listOf(100L, 200L),
                                any(),
                                ClubType.MAJOR_CLUB,
                            )
                        }
                    }
                }

                context("부장이 같은 타입의 다른 동아리 부장인 경우") {
                    val req =
                        ClubReqDto(
                            name = "동아리E",
                            type = ClubType.AUTONOMOUS_CLUB,
                            leaderId = 100L,
                            participantIds = emptyList(),
                            foundedYear = 2022,
                            status = ClubStatus.ACTIVE,
                        )
                    lateinit var mockLeader: StudentJpaEntity
                    lateinit var otherClub: ClubJpaEntity

                    beforeEach {
                        mockLeader =
                            StudentJpaEntity().apply {
                                this.id = 100L
                                this.name = "부장이름"
                                this.email = "leader@gsm.hs.kr"
                                this.studentNumber = StudentNumber(2, 1, 5)
                                this.major = Major.AI
                                this.sex = Sex.WOMAN
                            }
                        otherClub =
                            ClubJpaEntity().apply {
                                this.id = 99L
                                this.name = "기존자율동아리"
                                this.type = ClubType.AUTONOMOUS_CLUB
                                this.leader = mockLeader
                                this.foundedYear = 2022
                                this.status = ClubStatus.ACTIVE
                            }
                        every { mockClubRepository.existsByName(req.name) } returns false
                        every { mockStudentRepository.findById(req.leaderId!!) } returns java.util.Optional.of(mockLeader)
                        every { mockClubRepository.save(any()) } answers {
                            val entity = firstArg<ClubJpaEntity>()
                            entity.apply { this.id = 10L }
                        }
                        every { mockStudentRepository.findAllById(emptyList()) } returns emptyList()
                        every { mockClubRepository.findAllByLeaderIn(any()) } returns listOf(otherClub)
                        every { mockStudentRepository.bulkAssignClub(any(), any(), any()) } just Runs
                    }

                    it("기존 동아리의 leader가 null로 해제되어야 한다") {
                        createClubService.execute(req)

                        otherClub.leader shouldBe null
                    }
                }

                context("ACTIVE 상태이고 leaderId가 null일 때") {
                    val req =
                        ClubReqDto(
                            name = "동아리E",
                            type = ClubType.MAJOR_CLUB,
                            leaderId = null,
                            participantIds = listOf(200L),
                            foundedYear = 2022,
                            status = ClubStatus.ACTIVE,
                        )
                    lateinit var participant: StudentJpaEntity

                    beforeEach {
                        participant =
                            StudentJpaEntity().apply {
                                this.id = 200L
                                this.name = "부원"
                                this.email = "p@gsm.hs.kr"
                                this.studentNumber = StudentNumber(2, 1, 6)
                                this.major = Major.AI
                                this.sex = Sex.MAN
                            }
                        every { mockClubRepository.existsByName(req.name) } returns false
                        every { mockClubRepository.save(any()) } answers {
                            val entity = firstArg<ClubJpaEntity>()
                            entity.apply { this.id = 10L }
                        }
                        every { mockStudentRepository.findAllById(listOf(200L)) } returns listOf(participant)
                        every { mockStudentRepository.bulkAssignClub(any(), any(), any()) } just Runs
                    }

                    it("leader=null로 저장되어야 하고 findById가 호출되지 않아야 한다") {
                        val res = createClubService.execute(req)

                        res.leader shouldBe null
                        verify(exactly = 0) { mockStudentRepository.findById(any()) }
                        verify(exactly = 1) { mockClubRepository.save(any()) }
                    }
                }

                context("ACTIVE 상태이고 leaderId가 null이며 participantIds도 비어있을 때") {
                    val req =
                        ClubReqDto(
                            name = "동아리F",
                            type = ClubType.MAJOR_CLUB,
                            leaderId = null,
                            participantIds = emptyList(),
                            foundedYear = 2022,
                            status = ClubStatus.ACTIVE,
                        )

                    it("ExpectedException이 발생해야 한다") {
                        val ex =
                            shouldThrow<ExpectedException> {
                                createClubService.execute(req)
                            }
                        ex.message shouldBe "운영 중인 동아리에는 부장 또는 부원이 최소 1명 이상 있어야 합니다."
                    }
                }

                context("ABOLISHED 상태인데 leaderId가 null이 아닐 때") {
                    val req =
                        ClubReqDto(
                            name = "동아리F",
                            type = ClubType.MAJOR_CLUB,
                            leaderId = 100L,
                            participantIds = listOf(200L),
                            foundedYear = 2022,
                            status = ClubStatus.ABOLISHED,
                        )

                    it("ExpectedException이 발생해야 한다") {
                        val ex =
                            shouldThrow<ExpectedException> {
                                createClubService.execute(req)
                            }
                        ex.message shouldBe "폐지된 동아리에는 부장을 지정할 수 없습니다."
                    }
                }

                context("ABOLISHED 상태이고 participantIds가 비어있지 않을 때") {
                    val req =
                        ClubReqDto(
                            name = "동아리G",
                            type = ClubType.MAJOR_CLUB,
                            leaderId = null,
                            participantIds = listOf(200L),
                            foundedYear = 2022,
                            status = ClubStatus.ABOLISHED,
                            abolishedYear = 2024,
                        )

                    it("ExpectedException이 발생해야 한다") {
                        val ex =
                            shouldThrow<ExpectedException> {
                                createClubService.execute(req)
                            }
                        ex.message shouldBe "폐지된 동아리에는 구성원을 지정할 수 없습니다."
                    }
                }
            }
        }
    })
