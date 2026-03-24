package team.themoment.datagsm.web.domain.club.service

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
import team.themoment.datagsm.web.domain.club.service.impl.ModifyClubServiceImpl
import team.themoment.sdk.exception.ExpectedException
import java.util.Optional

class ModifyClubServiceTest :
    DescribeSpec({

        lateinit var mockClubRepository: ClubJpaRepository
        lateinit var mockStudentRepository: StudentJpaRepository
        lateinit var modifyClubService: ModifyClubServiceImpl

        beforeEach {
            mockClubRepository = mockk<ClubJpaRepository>()
            mockStudentRepository = mockk<StudentJpaRepository>()
            modifyClubService = ModifyClubServiceImpl(mockClubRepository, mockStudentRepository)
        }

        describe("ModifyClubService 클래스의") {
            describe("execute 메서드는") {
                val clubId = 1L
                lateinit var existing: ClubJpaEntity
                lateinit var oldLeader: StudentJpaEntity

                beforeEach {
                    oldLeader =
                        StudentJpaEntity().apply {
                            this.id = 10L
                            this.name = "기존부장"
                            this.email = "old@gsm.hs.kr"
                            this.studentNumber = StudentNumber(1, 1, 1)
                            this.major = Major.SW_DEVELOPMENT
                            this.sex = Sex.MAN
                        }
                    existing =
                        ClubJpaEntity().apply {
                            this.id = clubId
                            name = "기존동아리"
                            type = ClubType.MAJOR_CLUB
                            this.leader = oldLeader
                            foundedYear = 2022
                            status = ClubStatus.ACTIVE
                        }
                }

                context("이름을 다른 값으로 변경할 때") {
                    val req =
                        ClubReqDto(
                            name = "새이름",
                            type = ClubType.MAJOR_CLUB,
                            leaderId = 20L,
                            participantIds = listOf(30L, 40L),
                            foundedYear = 2022,
                            status = ClubStatus.ACTIVE,
                        )
                    lateinit var newLeader: StudentJpaEntity

                    beforeEach {
                        newLeader =
                            StudentJpaEntity().apply {
                                this.id = 20L
                                this.name = "새부장"
                                this.email = "new@gsm.hs.kr"
                                this.studentNumber = StudentNumber(2, 2, 2)
                                this.major = Major.AI
                                this.sex = Sex.WOMAN
                            }
                        every { mockClubRepository.findById(clubId) } returns Optional.of(existing)
                        every { mockClubRepository.existsByNameAndIdNot(req.name, clubId) } returns false
                        every { mockStudentRepository.findById(req.leaderId!!) } returns Optional.of(newLeader)
                        every { mockStudentRepository.findAllById(any<Iterable<Long>>()) } returns emptyList()
                        every { mockClubRepository.findAllByLeader(any()) } returns emptyList()
                        every { mockStudentRepository.clearClubReferencesByType(any(), any()) } just Runs
                        every { mockStudentRepository.bulkAssignClub(any(), any(), any()) } just Runs
                    }

                    it("업데이트된 정보가 반환되어야 한다") {
                        val res = modifyClubService.execute(clubId, req)

                        res.name shouldBe req.name
                        res.type shouldBe req.type
                        res.leader?.id shouldBe 20L
                        res.leader?.name shouldBe "새부장"

                        verify(exactly = 1) { mockClubRepository.findById(clubId) }
                        verify(exactly = 1) { mockClubRepository.existsByNameAndIdNot(req.name, clubId) }
                        verify(exactly = 1) { mockStudentRepository.findById(req.leaderId!!) }
                    }
                }

                context("이름을 기존과 동일하게 두고 타입만 변경할 때") {
                    val req =
                        ClubReqDto(
                            name = "기존동아리",
                            type = ClubType.AUTONOMOUS_CLUB,
                            leaderId = 10L,
                            participantIds = listOf(30L),
                            foundedYear = 2022,
                            status = ClubStatus.ACTIVE,
                        )

                    beforeEach {
                        every { mockClubRepository.findById(clubId) } returns Optional.of(existing)
                        every { mockClubRepository.existsByNameAndIdNot(req.name, clubId) } returns false
                        every { mockStudentRepository.findById(req.leaderId!!) } returns Optional.of(oldLeader)
                        every { mockStudentRepository.findAllById(any<Iterable<Long>>()) } returns emptyList()
                        every { mockClubRepository.findAllByLeader(any()) } returns emptyList()
                        every { mockStudentRepository.clearClubReferencesByType(any(), any()) } just Runs
                        every { mockStudentRepository.bulkAssignClub(any(), any(), any()) } just Runs
                    }

                    it("중복 이름 검사를 수행하고 저장되어야 한다") {
                        val res = modifyClubService.execute(clubId, req)

                        res.name shouldBe req.name
                        res.type shouldBe req.type
                        res.leader?.id shouldBe 10L

                        verify(exactly = 1) { mockClubRepository.findById(clubId) }
                        verify(exactly = 1) { mockClubRepository.existsByNameAndIdNot(req.name, clubId) }
                        verify(exactly = 1) { mockStudentRepository.findById(req.leaderId!!) }
                    }
                }

                context("중복된 이름으로 변경 시도할 때") {
                    val req =
                        ClubReqDto(
                            name = "기존있는이름",
                            type = ClubType.MAJOR_CLUB,
                            leaderId = 10L,
                            participantIds = listOf(10L),
                            foundedYear = 2022,
                            status = ClubStatus.ACTIVE,
                        )

                    beforeEach {
                        every { mockClubRepository.findById(clubId) } returns Optional.of(existing)
                        every { mockClubRepository.existsByNameAndIdNot(req.name, clubId) } returns true
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val ex =
                            shouldThrow<ExpectedException> {
                                modifyClubService.execute(clubId, req)
                            }
                        ex.message shouldBe "이미 존재하는 동아리 이름입니다."

                        verify(exactly = 1) { mockClubRepository.findById(clubId) }
                        verify(exactly = 1) { mockClubRepository.existsByNameAndIdNot(req.name, clubId) }
                    }
                }

                context("존재하지 않는 동아리 ID로 수정할 때") {
                    val req =
                        ClubReqDto(
                            name = "아무이름",
                            type = ClubType.MAJOR_CLUB,
                            leaderId = 10L,
                            participantIds = listOf(10L),
                            foundedYear = 2022,
                            status = ClubStatus.ACTIVE,
                        )

                    beforeEach {
                        every { mockClubRepository.findById(clubId) } returns Optional.empty()
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val ex =
                            shouldThrow<ExpectedException> {
                                modifyClubService.execute(clubId, req)
                            }
                        ex.message shouldBe "동아리를 찾을 수 없습니다."

                        verify(exactly = 1) { mockClubRepository.findById(clubId) }
                    }
                }

                context("타입이 변경될 때 (MAJOR_CLUB → AUTONOMOUS_CLUB)") {
                    val req =
                        ClubReqDto(
                            name = "새이름",
                            type = ClubType.AUTONOMOUS_CLUB,
                            leaderId = 20L,
                            participantIds = listOf(30L),
                            foundedYear = 2022,
                            status = ClubStatus.ACTIVE,
                        )
                    lateinit var newLeader: StudentJpaEntity

                    beforeEach {
                        newLeader =
                            StudentJpaEntity().apply {
                                this.id = 20L
                                this.name = "새부장"
                                this.email = "new@gsm.hs.kr"
                                this.studentNumber = StudentNumber(2, 2, 2)
                                this.major = Major.AI
                                this.sex = Sex.WOMAN
                            }
                        every { mockClubRepository.findById(clubId) } returns Optional.of(existing)
                        every { mockClubRepository.existsByNameAndIdNot(req.name, clubId) } returns false
                        every { mockStudentRepository.findById(req.leaderId!!) } returns Optional.of(newLeader)
                        every { mockStudentRepository.findAllById(listOf(30L)) } returns emptyList()
                        every { mockClubRepository.findAllByLeader(any()) } returns emptyList()
                        every { mockStudentRepository.clearClubReferencesByType(any(), any()) } just Runs
                        every { mockStudentRepository.bulkAssignClub(any(), any(), any()) } just Runs
                    }

                    it("구 타입의 club 참조가 해제되고 새 타입으로 bulk 할당되어야 한다") {
                        modifyClubService.execute(clubId, req)

                        verify { mockStudentRepository.clearClubReferencesByType(existing, ClubType.MAJOR_CLUB) }
                        verify { mockStudentRepository.bulkAssignClub(listOf(20L, 30L), existing, ClubType.AUTONOMOUS_CLUB) }
                    }
                }

                context("participantIds에 leaderId가 포함될 때") {
                    val req =
                        ClubReqDto(
                            name = "새이름",
                            type = ClubType.MAJOR_CLUB,
                            leaderId = 20L,
                            participantIds = listOf(20L, 30L),
                            foundedYear = 2022,
                            status = ClubStatus.ACTIVE,
                        )
                    lateinit var newLeader: StudentJpaEntity
                    lateinit var participant: StudentJpaEntity

                    beforeEach {
                        newLeader =
                            StudentJpaEntity().apply {
                                this.id = 20L
                                this.name = "새부장"
                                this.email = "new@gsm.hs.kr"
                                this.studentNumber = StudentNumber(2, 2, 2)
                                this.major = Major.AI
                                this.sex = Sex.WOMAN
                            }
                        participant =
                            StudentJpaEntity().apply {
                                this.id = 30L
                                this.name = "부원"
                                this.email = "p@gsm.hs.kr"
                                this.studentNumber = StudentNumber(2, 2, 3)
                                this.major = Major.AI
                                this.sex = Sex.MAN
                            }
                        every { mockClubRepository.findById(clubId) } returns Optional.of(existing)
                        every { mockClubRepository.existsByNameAndIdNot(req.name, clubId) } returns false
                        every { mockStudentRepository.findById(req.leaderId!!) } returns Optional.of(newLeader)
                        every { mockStudentRepository.findAllById(listOf(30L)) } returns listOf(participant)
                        every { mockClubRepository.findAllByLeader(any()) } returns emptyList()
                        every { mockStudentRepository.clearClubReferencesByType(any(), any()) } just Runs
                        every { mockStudentRepository.bulkAssignClub(any(), any(), any()) } just Runs
                    }

                    it("participants에 leader가 포함되지 않아야 한다") {
                        val res = modifyClubService.execute(clubId, req)

                        res.participants.none { it.id == 20L } shouldBe true
                        res.participants.size shouldBe 1
                        res.participants[0].id shouldBe 30L
                    }
                }

                context("부장과 부원이 배정될 때") {
                    val req =
                        ClubReqDto(
                            name = "새이름",
                            type = ClubType.MAJOR_CLUB,
                            leaderId = 20L,
                            participantIds = listOf(30L),
                            foundedYear = 2022,
                            status = ClubStatus.ACTIVE,
                        )
                    lateinit var newLeader: StudentJpaEntity
                    lateinit var participant: StudentJpaEntity

                    beforeEach {
                        newLeader =
                            StudentJpaEntity().apply {
                                this.id = 20L
                                this.name = "새부장"
                                this.email = "new@gsm.hs.kr"
                                this.studentNumber = StudentNumber(2, 2, 2)
                                this.major = Major.AI
                                this.sex = Sex.WOMAN
                            }
                        participant =
                            StudentJpaEntity().apply {
                                this.id = 30L
                                this.name = "부원"
                                this.email = "p@gsm.hs.kr"
                                this.studentNumber = StudentNumber(2, 2, 3)
                                this.major = Major.AI
                                this.sex = Sex.MAN
                            }
                        every { mockClubRepository.findById(clubId) } returns Optional.of(existing)
                        every { mockClubRepository.existsByNameAndIdNot(req.name, clubId) } returns false
                        every { mockStudentRepository.findById(req.leaderId!!) } returns Optional.of(newLeader)
                        every { mockStudentRepository.findAllById(listOf(30L)) } returns listOf(participant)
                        every { mockClubRepository.findAllByLeader(any()) } returns emptyList()
                        every { mockStudentRepository.clearClubReferencesByType(any(), any()) } just Runs
                        every { mockStudentRepository.bulkAssignClub(any(), any(), any()) } just Runs
                    }

                    it("부장과 부원이 bulk 할당되어야 한다") {
                        modifyClubService.execute(clubId, req)

                        verify { mockStudentRepository.bulkAssignClub(listOf(20L, 30L), existing, ClubType.MAJOR_CLUB) }
                    }
                }

                context("새 부장이 같은 타입의 다른 동아리 부장인 경우") {
                    val req =
                        ClubReqDto(
                            name = "새이름",
                            type = ClubType.MAJOR_CLUB,
                            leaderId = 20L,
                            participantIds = listOf(30L),
                            foundedYear = 2022,
                            status = ClubStatus.ACTIVE,
                        )
                    lateinit var newLeader: StudentJpaEntity
                    lateinit var otherClub: ClubJpaEntity

                    beforeEach {
                        newLeader =
                            StudentJpaEntity().apply {
                                this.id = 20L
                                this.name = "새부장"
                                this.email = "new@gsm.hs.kr"
                                this.studentNumber = StudentNumber(2, 2, 2)
                                this.major = Major.AI
                                this.sex = Sex.WOMAN
                            }
                        otherClub =
                            ClubJpaEntity().apply {
                                this.id = 99L
                                this.name = "다른동아리"
                                this.type = ClubType.MAJOR_CLUB
                                this.leader = newLeader
                                this.foundedYear = 2022
                                this.status = ClubStatus.ACTIVE
                            }
                        every { mockClubRepository.findById(clubId) } returns Optional.of(existing)
                        every { mockClubRepository.existsByNameAndIdNot(req.name, clubId) } returns false
                        every { mockStudentRepository.findById(req.leaderId!!) } returns Optional.of(newLeader)
                        every { mockStudentRepository.findAllById(listOf(30L)) } returns emptyList()
                        every { mockClubRepository.findAllByLeader(newLeader) } returns listOf(otherClub)
                        every { mockStudentRepository.clearClubReferencesByType(any(), any()) } just Runs
                        every { mockStudentRepository.bulkAssignClub(any(), any(), any()) } just Runs
                    }

                    it("다른 동아리의 부장 직위가 해제되어야 한다") {
                        modifyClubService.execute(clubId, req)

                        otherClub.leader shouldBe null
                        verify { mockClubRepository.findAllByLeader(newLeader) }
                    }
                }

                context("ACTIVE → ABOLISHED 변경 시 leaderId=null인 경우") {
                    val req =
                        ClubReqDto(
                            name = "기존동아리",
                            type = ClubType.MAJOR_CLUB,
                            leaderId = null,
                            participantIds = emptyList(),
                            foundedYear = 2022,
                            status = ClubStatus.ABOLISHED,
                            abolishedYear = 2024,
                        )

                    beforeEach {
                        every { mockClubRepository.findById(clubId) } returns Optional.of(existing)
                        every { mockClubRepository.existsByNameAndIdNot(req.name, clubId) } returns false
                        every { mockStudentRepository.findAllById(emptyList<Long>()) } returns emptyList()
                        every { mockStudentRepository.clearClubReferencesByType(any(), any()) } just Runs
                    }

                    it("club.leader가 null이 되고 clearClubReferencesByType이 호출되어야 한다") {
                        val res = modifyClubService.execute(clubId, req)

                        res.leader shouldBe null
                        existing.leader shouldBe null
                        verify { mockStudentRepository.clearClubReferencesByType(existing, ClubType.MAJOR_CLUB) }
                        verify(exactly = 0) { mockStudentRepository.bulkAssignClub(any(), any(), any()) }
                    }
                }

                context("ACTIVE 상태이고 leaderId가 null일 때") {
                    val req =
                        ClubReqDto(
                            name = "기존동아리",
                            type = ClubType.MAJOR_CLUB,
                            leaderId = null,
                            participantIds = listOf(30L),
                            foundedYear = 2022,
                            status = ClubStatus.ACTIVE,
                        )
                    lateinit var participant: StudentJpaEntity

                    beforeEach {
                        participant =
                            StudentJpaEntity().apply {
                                this.id = 30L
                                this.name = "부원"
                                this.email = "p@gsm.hs.kr"
                                this.studentNumber = StudentNumber(2, 2, 3)
                                this.major = Major.AI
                                this.sex = Sex.MAN
                            }
                        every { mockClubRepository.findById(clubId) } returns Optional.of(existing)
                        every { mockClubRepository.existsByNameAndIdNot(req.name, clubId) } returns false
                        every { mockStudentRepository.findAllById(listOf(30L)) } returns listOf(participant)
                        every { mockStudentRepository.clearClubReferencesByType(any(), any()) } just Runs
                        every { mockStudentRepository.bulkAssignClub(any(), any(), any()) } just Runs
                    }

                    it("leader=null로 저장되어야 하고 findById가 호출되지 않아야 한다") {
                        val res = modifyClubService.execute(clubId, req)

                        res.leader shouldBe null
                        existing.leader shouldBe null
                        verify(exactly = 0) { mockStudentRepository.findById(any()) }
                    }
                }

                context("ACTIVE 상태이고 leaderId가 null이며 participantIds도 비어있을 때") {
                    val req =
                        ClubReqDto(
                            name = "기존동아리",
                            type = ClubType.MAJOR_CLUB,
                            leaderId = null,
                            participantIds = emptyList(),
                            foundedYear = 2022,
                            status = ClubStatus.ACTIVE,
                        )

                    it("ExpectedException이 발생해야 한다") {
                        val ex =
                            shouldThrow<ExpectedException> {
                                modifyClubService.execute(clubId, req)
                            }
                        ex.message shouldBe "운영 중인 동아리에는 부장 또는 부원이 최소 1명 이상 있어야 합니다."
                    }
                }

                context("ABOLISHED 상태이고 participantIds가 비어있지 않을 때") {
                    val req =
                        ClubReqDto(
                            name = "기존동아리",
                            type = ClubType.MAJOR_CLUB,
                            leaderId = null,
                            participantIds = listOf(30L),
                            foundedYear = 2022,
                            status = ClubStatus.ABOLISHED,
                        )

                    it("ExpectedException이 발생해야 한다") {
                        val ex =
                            shouldThrow<ExpectedException> {
                                modifyClubService.execute(clubId, req)
                            }
                        ex.message shouldBe "폐지된 동아리에는 구성원을 지정할 수 없습니다."
                    }
                }

                context("ABOLISHED 상태인데 leaderId가 null이 아닐 때") {
                    val req =
                        ClubReqDto(
                            name = "기존동아리",
                            type = ClubType.MAJOR_CLUB,
                            leaderId = 20L,
                            participantIds = listOf(30L),
                            foundedYear = 2022,
                            status = ClubStatus.ABOLISHED,
                        )

                    it("ExpectedException이 발생해야 한다") {
                        val ex =
                            shouldThrow<ExpectedException> {
                                modifyClubService.execute(clubId, req)
                            }
                        ex.message shouldBe "폐지된 동아리에는 부장을 지정할 수 없습니다."
                    }
                }
            }
        }
    })
