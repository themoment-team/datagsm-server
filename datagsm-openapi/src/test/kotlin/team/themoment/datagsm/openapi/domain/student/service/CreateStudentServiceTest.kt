package team.themoment.datagsm.openapi.domain.student.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.springframework.context.ApplicationEventPublisher
import team.themoment.datagsm.common.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.common.domain.event.dto.internal.EventDispatchRequested
import team.themoment.datagsm.common.domain.event.dto.payload.EmptyEventObject
import team.themoment.datagsm.common.domain.event.dto.payload.EventChangedData
import team.themoment.datagsm.common.domain.event.dto.payload.StudentEventObject
import team.themoment.datagsm.common.domain.event.entity.constant.EventType
import team.themoment.datagsm.common.domain.student.dto.request.CreateStudentReqDto
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.common.domain.student.entity.constant.Major
import team.themoment.datagsm.common.domain.student.entity.constant.Sex
import team.themoment.datagsm.common.domain.student.entity.constant.StudentRole
import team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.openapi.domain.student.service.impl.CreateStudentServiceImpl
import team.themoment.sdk.exception.ExpectedException

class CreateStudentServiceTest :
    DescribeSpec({

        lateinit var mockStudentRepository: StudentJpaRepository
        lateinit var mockClubRepository: ClubJpaRepository
        lateinit var createStudentService: CreateStudentService
        val applicationEventPublisher = mockk<ApplicationEventPublisher>()
        val eventSlot = slot<EventDispatchRequested>()

        beforeEach {
            mockStudentRepository = mockk<StudentJpaRepository>()
            mockClubRepository = mockk<ClubJpaRepository>()
            createStudentService = CreateStudentServiceImpl(mockStudentRepository, mockClubRepository, applicationEventPublisher)
            clearMocks(applicationEventPublisher)
            justRun { applicationEventPublisher.publishEvent(capture(eventSlot)) }
        }

        describe("CreateStudentService 클래스의") {
            describe("execute 메서드는") {
                context("중복된 이메일로 생성할 때") {
                    val req =
                        CreateStudentReqDto(
                            name = "홍길동",
                            sex = Sex.MAN,
                            email = "duplicate@gsm.hs.kr",
                            grade = 2,
                            classNum = 1,
                            number = 5,
                            role = StudentRole.GENERAL_STUDENT,
                            dormitoryRoomNumber = 301,
                        )

                    beforeEach {
                        every { mockStudentRepository.existsByEmail(req.email) } returns true
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val ex =
                            shouldThrow<ExpectedException> {
                                createStudentService.execute(req)
                            }
                        ex.message shouldBe "이미 존재하는 이메일입니다."

                        verify(exactly = 1) { mockStudentRepository.existsByEmail(req.email) }
                        verify(exactly = 0) { mockStudentRepository.save(any()) }
                    }
                }

                context("정상적으로 학생을 생성할 때") {
                    val req =
                        CreateStudentReqDto(
                            name = "김철수",
                            sex = Sex.MAN,
                            email = "student@gsm.hs.kr",
                            grade = 2,
                            classNum = 1,
                            number = 10,
                            role = StudentRole.GENERAL_STUDENT,
                            dormitoryRoomNumber = 402,
                        )

                    beforeEach {
                        every { mockStudentRepository.existsByEmail(req.email) } returns false
                        every {
                            mockStudentRepository.existsByStudentNumber(
                                req.grade!!,
                                req.classNum!!,
                                req.number!!,
                            )
                        } returns false
                        every { mockStudentRepository.save(any()) } answers {
                            val entity = firstArg<StudentJpaEntity>()
                            entity.apply { this.id = 100L }
                        }
                    }

                    it("생성된 학생 정보를 반환해야 한다") {
                        val res = createStudentService.execute(req)

                        res.id shouldBe 100L
                        res.name shouldBe "김철수"
                        res.email shouldBe "student@gsm.hs.kr"
                        res.grade shouldBe 2
                        res.classNum shouldBe 1
                        res.number shouldBe 10
                        res.major shouldBe Major.SW_DEVELOPMENT

                        verify(exactly = 1) { mockStudentRepository.existsByEmail(req.email) }
                        verify(exactly = 1) {
                            mockStudentRepository.existsByStudentNumber(
                                req.grade!!,
                                req.classNum!!,
                                req.number!!,
                            )
                        }
                        verify(exactly = 1) { mockStudentRepository.save(any()) }

                        verify(exactly = 1) {
                            applicationEventPublisher.publishEvent(
                                match<EventDispatchRequested> { it.eventType == EventType.STUDENT_UPDATED },
                            )
                        }
                        val data = eventSlot.captured.data as EventChangedData
                        data.old.size shouldBe 1
                        data.new.size shouldBe 1
                        data.old[0].obj.shouldBeInstanceOf<EmptyEventObject>()
                        data.new[0].obj.shouldBeInstanceOf<StudentEventObject>()
                    }
                }
            }
        }
    })
