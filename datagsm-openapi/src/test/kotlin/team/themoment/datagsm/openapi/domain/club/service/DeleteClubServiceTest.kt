package team.themoment.datagsm.openapi.domain.club.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import team.themoment.datagsm.common.domain.club.entity.ClubJpaEntity
import team.themoment.datagsm.common.domain.club.entity.constant.ClubType
import team.themoment.datagsm.common.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.common.domain.student.entity.EnrolledStudent
import team.themoment.datagsm.common.domain.student.entity.StudentNumber
import team.themoment.datagsm.common.domain.student.entity.constant.Major
import team.themoment.datagsm.common.domain.student.entity.constant.Sex
import team.themoment.datagsm.openapi.domain.club.service.impl.DeleteClubServiceImpl
import team.themoment.sdk.exception.ExpectedException
import java.util.Optional

class DeleteClubServiceTest :
    DescribeSpec({

        lateinit var mockClubRepository: ClubJpaRepository
        lateinit var deleteClubService: DeleteClubService

        beforeEach {
            mockClubRepository = mockk<ClubJpaRepository>()
            deleteClubService = DeleteClubServiceImpl(mockClubRepository)
        }

        describe("DeleteClubService 클래스의") {
            describe("execute 메서드는") {
                context("존재하지 않는 동아리를 삭제할 때") {
                    beforeEach {
                        every { mockClubRepository.findById(999L) } returns Optional.empty()
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val ex =
                            shouldThrow<ExpectedException> {
                                deleteClubService.execute(999L)
                            }
                        ex.message shouldBe "동아리를 찾을 수 없습니다. clubId: 999"

                        verify(exactly = 1) { mockClubRepository.findById(999L) }
                        verify(exactly = 0) { mockClubRepository.delete(any()) }
                    }
                }

                context("정상적으로 동아리를 삭제할 때") {
                    val clubId = 10L
                    lateinit var existingClub: ClubJpaEntity

                    beforeEach {
                        val leader =
                            EnrolledStudent().apply {
                                this.id = 100L
                                this.name = "부장"
                                this.email = "leader@gsm.hs.kr"
                                this.studentNumber = StudentNumber(1, 1, 1)
                                this.major = Major.AI
                                this.sex = Sex.MAN
                            }
                        existingClub =
                            ClubJpaEntity().apply {
                                this.id = clubId
                                this.name = "삭제할동아리"
                                this.type = ClubType.AUTONOMOUS_CLUB
                                this.leader = leader
                            }

                        every { mockClubRepository.findById(clubId) } returns Optional.of(existingClub)
                        every { mockClubRepository.delete(existingClub) } returns Unit
                    }

                    it("동아리가 삭제되어야 한다") {
                        deleteClubService.execute(clubId)

                        verify(exactly = 1) { mockClubRepository.findById(clubId) }
                        verify(exactly = 1) { mockClubRepository.delete(existingClub) }
                    }
                }
            }
        }
    })
