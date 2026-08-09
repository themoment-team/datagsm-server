package team.themoment.datagsm.openapi.domain.club.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.just
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import org.springframework.context.ApplicationEventPublisher
import team.themoment.datagsm.common.domain.club.entity.ClubJpaEntity
import team.themoment.datagsm.common.domain.club.entity.constant.ClubStatus
import team.themoment.datagsm.common.domain.club.entity.constant.ClubType
import team.themoment.datagsm.common.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.common.domain.event.dto.internal.EventDispatchRequested
import team.themoment.datagsm.common.domain.event.dto.payload.ClubEventObject
import team.themoment.datagsm.common.domain.event.dto.payload.EmptyEventObject
import team.themoment.datagsm.common.domain.event.dto.payload.EventChangedData
import team.themoment.datagsm.common.domain.event.entity.constant.EventType
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.common.domain.student.entity.StudentNumber
import team.themoment.datagsm.common.domain.student.entity.constant.Major
import team.themoment.datagsm.common.domain.student.entity.constant.Sex
import team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.openapi.domain.club.service.impl.DeleteClubServiceImpl
import team.themoment.sdk.exception.ExpectedException
import java.util.Optional

class DeleteClubServiceTest :
    DescribeSpec({

        lateinit var mockClubRepository: ClubJpaRepository
        lateinit var mockStudentRepository: StudentJpaRepository
        lateinit var deleteClubService: DeleteClubService
        val applicationEventPublisher = mockk<ApplicationEventPublisher>()
        val eventSlot = slot<EventDispatchRequested>()

        beforeEach {
            mockClubRepository = mockk<ClubJpaRepository>()
            mockStudentRepository = mockk<StudentJpaRepository>()
            deleteClubService = DeleteClubServiceImpl(mockClubRepository, mockStudentRepository, applicationEventPublisher)
            clearMocks(applicationEventPublisher)
            justRun { applicationEventPublisher.publishEvent(capture(eventSlot)) }
            every { mockStudentRepository.findByMajorClub(any()) } returns emptyList()
            every { mockStudentRepository.findByAutonomousClub(any()) } returns emptyList()
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
                        ex.message shouldBe "동아리를 찾을 수 없습니다."

                        verify(exactly = 1) { mockClubRepository.findById(999L) }
                        verify(exactly = 0) { mockStudentRepository.bulkClearClubReferences(any()) }
                        verify(exactly = 0) { mockClubRepository.deleteAllByIdInBatch(any()) }
                    }
                }

                context("정상적으로 동아리를 삭제할 때") {
                    val clubId = 10L
                    lateinit var existingClub: ClubJpaEntity

                    beforeEach {
                        val leader =
                            StudentJpaEntity().apply {
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
                                this.status = ClubStatus.ACTIVE
                                this.leader = leader
                            }

                        every { mockClubRepository.findById(clubId) } returns Optional.of(existingClub)
                        every { mockStudentRepository.bulkClearClubReferences(listOf(existingClub)) } just runs
                        every { mockClubRepository.deleteAllByIdInBatch(listOf(clubId)) } just runs
                    }

                    it("bulkClearClubReferences 후 deleteAllByIdInBatch가 각 1회 호출되어야 한다") {
                        deleteClubService.execute(clubId)

                        verify(exactly = 1) { mockClubRepository.findById(clubId) }
                        verify(exactly = 1) { mockStudentRepository.bulkClearClubReferences(listOf(existingClub)) }
                        verify(exactly = 1) { mockClubRepository.deleteAllByIdInBatch(listOf(clubId)) }

                        verify(exactly = 1) {
                            applicationEventPublisher.publishEvent(
                                match<EventDispatchRequested> { it.eventType == EventType.CLUB_UPDATED },
                            )
                        }
                        val data = eventSlot.captured.data as EventChangedData
                        data.old.size shouldBe 1
                        data.new.size shouldBe 1
                        data.old[0].obj.shouldBeInstanceOf<ClubEventObject>()
                        data.new[0].obj.shouldBeInstanceOf<EmptyEventObject>()
                    }
                }
            }
        }
    })
