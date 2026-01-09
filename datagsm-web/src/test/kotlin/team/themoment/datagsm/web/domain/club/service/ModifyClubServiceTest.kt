package team.themoment.datagsm.web.domain.club.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import team.themoment.datagsm.common.domain.club.entity.ClubJpaEntity
import team.themoment.datagsm.common.domain.club.entity.constant.ClubType
import team.themoment.datagsm.common.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.common.domain.student.entity.constant.Major
import team.themoment.datagsm.common.domain.student.entity.constant.Sex
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.common.domain.student.entity.StudentNumber
import team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.common.domain.club.dto.request.ClubReqDto
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
                        }
                }

                context("이름을 다른 값으로 변경할 때") {
                    val req =
                        ClubReqDto(
                            name = "새이름",
                            type = ClubType.JOB_CLUB,
                            leaderId = 20L,
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
                        every { mockStudentRepository.findById(req.leaderId) } returns Optional.of(newLeader)
                        every { mockStudentRepository.findByJobClub(existing) } returns emptyList()
                    }

                    it("업데이트된 정보가 반환되어야 한다") {
                        val res = modifyClubService.execute(clubId, req)

                        res.name shouldBe req.name
                        res.type shouldBe req.type
                        res.leader.id shouldBe 20L
                        res.leader.name shouldBe "새부장"

                        verify(exactly = 1) { mockClubRepository.findById(clubId) }
                        verify(exactly = 1) { mockClubRepository.existsByNameAndIdNot(req.name, clubId) }
                        verify(exactly = 1) { mockStudentRepository.findById(req.leaderId) }
                    }
                }

                context("이름을 기존과 동일하게 두고 설명/타입만 변경할 때") {
                    val req =
                        ClubReqDto(
                            name = "기존동아리",
                            type = ClubType.AUTONOMOUS_CLUB,
                            leaderId = 10L,
                        )

                    beforeEach {
                        every { mockClubRepository.findById(clubId) } returns Optional.of(existing)
                        every { mockClubRepository.existsByNameAndIdNot(req.name, clubId) } returns false
                        every { mockStudentRepository.findById(req.leaderId) } returns Optional.of(oldLeader)
                        every { mockStudentRepository.findByAutonomousClub(existing) } returns emptyList()
                    }

                    it("중복 이름 검사를 수행하고 저장되어야 한다") {
                        val res = modifyClubService.execute(clubId, req)

                        res.name shouldBe req.name
                        res.type shouldBe req.type
                        res.leader.id shouldBe 10L

                        verify(exactly = 1) { mockClubRepository.findById(clubId) }
                        verify(exactly = 1) { mockClubRepository.existsByNameAndIdNot(req.name, clubId) }
                        verify(exactly = 1) { mockStudentRepository.findById(req.leaderId) }
                    }
                }

                context("중복된 이름으로 변경 시도할 때") {
                    val req =
                        ClubReqDto(
                            name = "기존있는이름",
                            type = ClubType.MAJOR_CLUB,
                            leaderId = 10L,
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
                        ex.message shouldBe "이미 존재하는 동아리 이름입니다: ${req.name}"

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
                        )

                    beforeEach {
                        every { mockClubRepository.findById(clubId) } returns Optional.empty()
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val ex =
                            shouldThrow<ExpectedException> {
                                modifyClubService.execute(clubId, req)
                            }
                        ex.message shouldBe "동아리를 찾을 수 없습니다. clubId: $clubId"

                        verify(exactly = 1) { mockClubRepository.findById(clubId) }
                    }
                }
            }
        }
    })
