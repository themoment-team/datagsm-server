package team.themoment.datagsm.web.domain.club.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import team.themoment.datagsm.common.domain.club.entity.ClubJpaEntity
import team.themoment.datagsm.common.domain.club.entity.constant.ClubType
import team.themoment.datagsm.common.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.web.domain.club.service.impl.DeleteClubServiceImpl
import team.themoment.sdk.exception.ExpectedException
import java.util.Optional

class DeleteClubServiceTest :
    DescribeSpec({

        lateinit var mockClubRepository: ClubJpaRepository
        lateinit var mockStudentRepository: StudentJpaRepository
        lateinit var deleteClubService: DeleteClubService

        beforeEach {
            mockClubRepository = mockk<ClubJpaRepository>()
            mockStudentRepository = mockk<StudentJpaRepository>()
            deleteClubService = DeleteClubServiceImpl(mockClubRepository, mockStudentRepository)
        }

        describe("DeleteClubService 클래스의") {
            describe("execute 메서드는") {
                val clubId = 7L

                context("존재하는 동아리를 삭제할 때") {
                    lateinit var existing: ClubJpaEntity

                    beforeEach {
                        existing =
                            ClubJpaEntity().apply {
                                this.id = clubId
                                name = "삭제대상"
                                type = ClubType.MAJOR_CLUB
                            }
                        every { mockClubRepository.findById(clubId) } returns Optional.of(existing)
                        every { mockStudentRepository.bulkClearClubReferences(listOf(existing)) } just runs
                        every { mockClubRepository.deleteAllByIdInBatch(listOf(clubId)) } just runs
                    }

                    it("bulkClearClubReferences 후 deleteAllByIdInBatch가 각 1회 호출되어야 한다") {
                        deleteClubService.execute(clubId)

                        verify(exactly = 1) { mockClubRepository.findById(clubId) }
                        verify(exactly = 1) { mockStudentRepository.bulkClearClubReferences(listOf(existing)) }
                        verify(exactly = 1) { mockClubRepository.deleteAllByIdInBatch(listOf(clubId)) }
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
                        ex.message shouldBe "동아리를 찾을 수 없습니다."

                        verify(exactly = 1) { mockClubRepository.findById(clubId) }
                        verify(exactly = 0) { mockStudentRepository.bulkClearClubReferences(any()) }
                        verify(exactly = 0) { mockClubRepository.deleteAllByIdInBatch(any()) }
                    }
                }
            }
        }
    })
