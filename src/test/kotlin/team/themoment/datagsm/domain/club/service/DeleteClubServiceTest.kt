package team.themoment.datagsm.domain.club.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import team.themoment.datagsm.domain.club.entity.ClubJpaEntity
import team.themoment.datagsm.domain.club.entity.constant.ClubType
import team.themoment.datagsm.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.domain.club.service.impl.DeleteClubServiceImpl
import team.themoment.datagsm.global.exception.error.ExpectedException
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
                val clubId = 7L

                context("존재하는 동아리를 삭제할 때") {
                    lateinit var existing: ClubJpaEntity

                    beforeEach {
                        existing =
                            ClubJpaEntity().apply {
                                this.clubId = clubId
                                clubName = "삭제대상"
                                clubDescription = "desc"
                                clubType = ClubType.JOB_CLUB
                            }
                        every { mockClubRepository.findById(clubId) } returns Optional.of(existing)
                        every { mockClubRepository.delete(existing) } just runs
                    }

                    it("delete가 1회 호출되어야 한다") {
                        deleteClubService.execute(clubId)

                        verify(exactly = 1) { mockClubRepository.findById(clubId) }
                        verify(exactly = 1) { mockClubRepository.delete(existing) }
                    }
                }

                context("존재하지 않는 동아리 ID일 때") {
                    beforeEach {
                        every { mockClubRepository.findById(clubId) } returns Optional.empty()
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val ex =
                            shouldThrow<ExpectedException> {
                                deleteClubService.execute(clubId)
                            }
                        ex.message shouldBe "동아리를 찾을 수 없습니다. clubId: $clubId"

                        verify(exactly = 1) { mockClubRepository.findById(clubId) }
                        verify(exactly = 0) { mockClubRepository.delete(any()) }
                    }
                }
            }
        }
    })
