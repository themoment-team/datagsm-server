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
import team.themoment.datagsm.openapi.domain.club.service.impl.ModifyClubServiceImpl
import team.themoment.sdk.exception.ExpectedException

class ModifyClubServiceTest :
    DescribeSpec({

        lateinit var mockClubRepository: ClubJpaRepository
        lateinit var mockStudentRepository: StudentJpaRepository
        lateinit var modifyClubService: ModifyClubService

        beforeEach {
            mockClubRepository = mockk<ClubJpaRepository>()
            mockStudentRepository = mockk<StudentJpaRepository>()
            modifyClubService = ModifyClubServiceImpl(mockClubRepository, mockStudentRepository)
        }

        describe("ModifyClubService 클래스의") {
            describe("execute 메서드는") {
                context("존재하지 않는 동아리를 수정할 때") {
                    val req =
                        ClubReqDto(
                            name = "수정동아리",
                            type = ClubType.MAJOR_CLUB,
                            leaderId = 1L,
                            participantIds = listOf(2L),
                            foundedYear = 2022,
                            status = ClubStatus.ACTIVE,
                        )

                    beforeEach {
                        every { mockClubRepository.findById(999L) } returns java.util.Optional.empty()
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val ex =
                            shouldThrow<ExpectedException> {
                                modifyClubService.execute(999L, req)
                            }
                        ex.message shouldBe "동아리를 찾을 수 없습니다."

                        verify(exactly = 1) { mockClubRepository.findById(999L) }
                    }
                }

                context("정상적으로 동아리를 수정할 때") {
                    val clubId = 10L
                    val req =
                        ClubReqDto(
                            name = "수정된동아리",
                            type = ClubType.MAJOR_CLUB,
                            leaderId = 200L,
                            participantIds = listOf(300L),
                            foundedYear = 2022,
                            status = ClubStatus.ACTIVE,
                        )
                    lateinit var existingClub: ClubJpaEntity
                    lateinit var newLeader: StudentJpaEntity

                    beforeEach {
                        val oldLeader =
                            StudentJpaEntity().apply {
                                this.id = 100L
                                this.name = "이전부장"
                                this.email = "old@gsm.hs.kr"
                                this.studentNumber = StudentNumber(1, 1, 1)
                                this.major = Major.AI
                                this.sex = Sex.MAN
                            }
                        existingClub =
                            ClubJpaEntity().apply {
                                this.id = clubId
                                this.name = "기존동아리"
                                this.type = ClubType.MAJOR_CLUB
                                this.leader = oldLeader
                                this.foundedYear = 2022
                                this.status = ClubStatus.ACTIVE
                            }
                        newLeader =
                            StudentJpaEntity().apply {
                                this.id = 200L
                                this.name = "새부장"
                                this.email = "new@gsm.hs.kr"
                                this.studentNumber = StudentNumber(2, 2, 2)
                                this.major = Major.AI
                                this.sex = Sex.WOMAN
                            }

                        every { mockClubRepository.findById(clubId) } returns java.util.Optional.of(existingClub)
                        every { mockClubRepository.existsByNameAndIdNot(req.name, clubId) } returns false
                        every { mockStudentRepository.findById(req.leaderId!!) } returns java.util.Optional.of(newLeader)
                        every { mockStudentRepository.clearClubReferencesByType(any(), any()) } just Runs
                        every { mockStudentRepository.findAllById(any<Iterable<Long>>()) } returns emptyList()
                        every { mockStudentRepository.bulkAssignClub(any(), any(), any()) } just Runs
                    }

                    it("수정된 동아리 정보를 반환해야 한다") {
                        val res = modifyClubService.execute(clubId, req)

                        res.id shouldBe clubId
                        res.name shouldBe "수정된동아리"
                        res.type shouldBe ClubType.MAJOR_CLUB
                        res.leader?.id shouldBe 200L
                        res.leader?.name shouldBe "새부장"

                        verify(exactly = 1) { mockClubRepository.findById(clubId) }
                        verify(exactly = 1) { mockClubRepository.existsByNameAndIdNot(req.name, clubId) }
                        verify(exactly = 1) { mockStudentRepository.findById(req.leaderId!!) }
                        verify(exactly = 1) { mockStudentRepository.clearClubReferencesByType(existingClub, ClubType.MAJOR_CLUB) }
                        verify(exactly = 1) { mockStudentRepository.bulkAssignClub(any(), any(), any()) }
                    }
                }

                context("타입이 변경될 때") {
                    val clubId = 10L
                    val req =
                        ClubReqDto(
                            name = "수정동아리",
                            type = ClubType.AUTONOMOUS_CLUB,
                            leaderId = 200L,
                            participantIds = listOf(300L),
                            foundedYear = 2022,
                            status = ClubStatus.ACTIVE,
                        )
                    lateinit var existingClub: ClubJpaEntity
                    lateinit var newLeader: StudentJpaEntity

                    beforeEach {
                        val oldLeader =
                            StudentJpaEntity().apply {
                                this.id = 100L
                                this.name = "이전부장"
                                this.email = "old@gsm.hs.kr"
                                this.studentNumber = StudentNumber(1, 1, 1)
                                this.major = Major.AI
                                this.sex = Sex.MAN
                            }
                        existingClub =
                            ClubJpaEntity().apply {
                                this.id = clubId
                                this.name = "기존동아리"
                                this.type = ClubType.MAJOR_CLUB
                                this.leader = oldLeader
                                this.foundedYear = 2022
                                this.status = ClubStatus.ACTIVE
                            }
                        newLeader =
                            StudentJpaEntity().apply {
                                this.id = 200L
                                this.name = "새부장"
                                this.email = "new@gsm.hs.kr"
                                this.studentNumber = StudentNumber(2, 2, 2)
                                this.major = Major.AI
                                this.sex = Sex.WOMAN
                            }

                        every { mockClubRepository.findById(clubId) } returns java.util.Optional.of(existingClub)
                        every { mockClubRepository.existsByNameAndIdNot(req.name, clubId) } returns false
                        every { mockStudentRepository.findById(req.leaderId!!) } returns java.util.Optional.of(newLeader)
                        every { mockStudentRepository.clearClubReferencesByType(any(), any()) } just Runs
                        every { mockStudentRepository.findAllById(any<Iterable<Long>>()) } returns emptyList()
                        every { mockStudentRepository.bulkAssignClub(any(), any(), any()) } just Runs
                    }

                    it("구 타입 기준 부원 해제가 호출되어야 한다") {
                        modifyClubService.execute(clubId, req)

                        verify(exactly = 1) { mockStudentRepository.clearClubReferencesByType(existingClub, ClubType.MAJOR_CLUB) }
                    }
                }

                context("participantIds에 leaderId가 포함될 때") {
                    val clubId = 10L
                    val req =
                        ClubReqDto(
                            name = "수정동아리",
                            type = ClubType.MAJOR_CLUB,
                            leaderId = 200L,
                            participantIds = listOf(200L, 300L),
                            foundedYear = 2022,
                            status = ClubStatus.ACTIVE,
                        )
                    lateinit var existingClub: ClubJpaEntity
                    lateinit var newLeader: StudentJpaEntity
                    lateinit var participant: StudentJpaEntity

                    beforeEach {
                        val oldLeader =
                            StudentJpaEntity().apply {
                                this.id = 100L
                                this.name = "이전부장"
                                this.email = "old@gsm.hs.kr"
                                this.studentNumber = StudentNumber(1, 1, 1)
                                this.major = Major.AI
                                this.sex = Sex.MAN
                            }
                        existingClub =
                            ClubJpaEntity().apply {
                                this.id = clubId
                                this.name = "기존동아리"
                                this.type = ClubType.MAJOR_CLUB
                                this.leader = oldLeader
                                this.foundedYear = 2022
                                this.status = ClubStatus.ACTIVE
                            }
                        newLeader =
                            StudentJpaEntity().apply {
                                this.id = 200L
                                this.name = "새부장"
                                this.email = "new@gsm.hs.kr"
                                this.studentNumber = StudentNumber(2, 2, 2)
                                this.major = Major.AI
                                this.sex = Sex.WOMAN
                            }
                        participant =
                            StudentJpaEntity().apply {
                                this.id = 300L
                                this.name = "부원"
                                this.email = "p@gsm.hs.kr"
                                this.studentNumber = StudentNumber(2, 2, 3)
                                this.major = Major.AI
                                this.sex = Sex.MAN
                            }

                        every { mockClubRepository.findById(clubId) } returns java.util.Optional.of(existingClub)
                        every { mockClubRepository.existsByNameAndIdNot(req.name, clubId) } returns false
                        every { mockStudentRepository.findById(req.leaderId!!) } returns java.util.Optional.of(newLeader)
                        every { mockStudentRepository.clearClubReferencesByType(any(), any()) } just Runs
                        every { mockStudentRepository.findAllById(listOf(300L)) } returns listOf(participant)
                        every { mockStudentRepository.bulkAssignClub(any(), any(), any()) } just Runs
                    }

                    it("participants에 leader가 포함되지 않아야 한다") {
                        val res = modifyClubService.execute(clubId, req)

                        res.participants.none { it.id == 200L } shouldBe true
                        res.participants.size shouldBe 1
                        res.participants[0].id shouldBe 300L
                    }
                }

                context("ABOLISHED 상태이고 participantIds가 비어있지 않을 때") {
                    val clubId = 10L
                    val req =
                        ClubReqDto(
                            name = "기존동아리",
                            type = ClubType.MAJOR_CLUB,
                            leaderId = null,
                            participantIds = listOf(300L),
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

                context("ACTIVE → ABOLISHED 변경 시 leaderId=null인 경우") {
                    val clubId = 10L
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
                    lateinit var existingClub: ClubJpaEntity

                    beforeEach {
                        val oldLeader =
                            StudentJpaEntity().apply {
                                this.id = 100L
                                this.name = "이전부장"
                                this.email = "old@gsm.hs.kr"
                                this.studentNumber = StudentNumber(1, 1, 1)
                                this.major = Major.AI
                                this.sex = Sex.MAN
                            }
                        existingClub =
                            ClubJpaEntity().apply {
                                this.id = clubId
                                this.name = "기존동아리"
                                this.type = ClubType.MAJOR_CLUB
                                this.leader = oldLeader
                                this.foundedYear = 2022
                                this.status = ClubStatus.ACTIVE
                            }

                        every { mockClubRepository.findById(clubId) } returns java.util.Optional.of(existingClub)
                        every { mockClubRepository.existsByNameAndIdNot(req.name, clubId) } returns false
                        every { mockStudentRepository.findAllById(emptyList<Long>()) } returns emptyList()
                        every { mockStudentRepository.clearClubReferencesByType(any(), any()) } just Runs
                    }

                    it("club.leader가 null이 되고 bulkAssignClub이 호출되지 않아야 한다") {
                        val res = modifyClubService.execute(clubId, req)

                        res.leader shouldBe null
                        existingClub.leader shouldBe null
                        verify { mockStudentRepository.clearClubReferencesByType(existingClub, ClubType.MAJOR_CLUB) }
                        verify(exactly = 0) { mockStudentRepository.bulkAssignClub(any(), any(), any()) }
                    }
                }

                context("ABOLISHED 상태인데 leaderId가 null이 아닐 때") {
                    val clubId = 10L
                    val req =
                        ClubReqDto(
                            name = "기존동아리",
                            type = ClubType.MAJOR_CLUB,
                            leaderId = 100L,
                            participantIds = listOf(300L),
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
