package team.themoment.datagsm.openapi.domain.club.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import team.themoment.datagsm.common.domain.club.dto.request.ClubReqDto
import team.themoment.datagsm.common.domain.club.entity.ClubJpaEntity
import team.themoment.datagsm.common.domain.club.entity.constant.ClubType
import team.themoment.datagsm.common.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.common.domain.student.entity.EnrolledStudent
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
                        )

                    beforeEach {
                        every { mockClubRepository.findById(999L) } returns java.util.Optional.empty()
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val ex =
                            shouldThrow<ExpectedException> {
                                modifyClubService.execute(999L, req)
                            }
                        ex.message shouldBe "동아리를 찾을 수 없습니다. clubId: 999"

                        verify(exactly = 1) { mockClubRepository.findById(999L) }
                    }
                }

                context("정상적으로 동아리를 수정할 때") {
                    val clubId = 10L
                    val req =
                        ClubReqDto(
                            name = "수정된동아리",
                            type = ClubType.JOB_CLUB,
                            leaderId = 200L,
                        )
                    lateinit var existingClub: ClubJpaEntity
                    lateinit var newLeader: EnrolledStudent

                    beforeEach {
                        val oldLeader =
                            EnrolledStudent().apply {
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
                            }
                        newLeader =
                            EnrolledStudent().apply {
                                this.id = 200L
                                this.name = "새부장"
                                this.email = "new@gsm.hs.kr"
                                this.studentNumber = StudentNumber(2, 2, 2)
                                this.major = Major.AI
                                this.sex = Sex.WOMAN
                            }

                        every { mockClubRepository.findById(clubId) } returns java.util.Optional.of(existingClub)
                        every { mockClubRepository.existsByNameAndIdNot(req.name, clubId) } returns false
                        every { mockStudentRepository.findById(req.leaderId) } returns java.util.Optional.of(newLeader)
                        every { mockStudentRepository.findByJobClub(existingClub) } returns emptyList()
                    }

                    it("수정된 동아리 정보를 반환해야 한다") {
                        val res = modifyClubService.execute(clubId, req)

                        res.id shouldBe clubId
                        res.name shouldBe "수정된동아리"
                        res.type shouldBe ClubType.JOB_CLUB
                        res.leader.id shouldBe 200L
                        res.leader.name shouldBe "새부장"

                        verify(exactly = 1) { mockClubRepository.findById(clubId) }
                        verify(exactly = 1) { mockClubRepository.existsByNameAndIdNot(req.name, clubId) }
                        verify(exactly = 1) { mockStudentRepository.findById(req.leaderId) }
                    }
                }
            }
        }
    })
