package team.themoment.datagsm.web.domain.student.service.impl

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.springframework.context.ApplicationEventPublisher
import team.themoment.datagsm.common.domain.event.dto.internal.EventDispatchRequested
import team.themoment.datagsm.common.domain.student.dto.request.RequestStudentDataEditReqDto
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.common.domain.student.entity.constant.Sex
import team.themoment.datagsm.common.domain.student.entity.constant.StudentDataEditField
import team.themoment.datagsm.common.domain.student.entity.constant.StudentRole
import team.themoment.datagsm.common.domain.student.repository.StudentDataEditRequestJpaRepository
import team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.web.domain.student.dto.internal.StudentLoginRevocationRequested

class RequestStudentDataEditServiceImplTest :
    DescribeSpec({

        lateinit var mockStudentJpaRepository: StudentJpaRepository
        lateinit var mockStudentDataEditRequestJpaRepository: StudentDataEditRequestJpaRepository
        lateinit var mockApplicationEventPublisher: ApplicationEventPublisher
        lateinit var service: RequestStudentDataEditServiceImpl

        val studentId = 210L

        fun student() =
            StudentJpaEntity().apply {
                id = studentId
                name = "홍길동"
                email = "s25012@gsm.hs.kr"
                sex = Sex.MAN
                role = StudentRole.GENERAL_STUDENT
            }

        beforeEach {
            mockStudentJpaRepository = mockk()
            mockStudentDataEditRequestJpaRepository = mockk()
            mockApplicationEventPublisher = mockk()
            service =
                RequestStudentDataEditServiceImpl(
                    mockStudentJpaRepository,
                    mockStudentDataEditRequestJpaRepository,
                    mockApplicationEventPublisher,
                )

            every { mockStudentDataEditRequestJpaRepository.findAllByStudentIdIn(any()) } returns emptyList()
            every { mockStudentDataEditRequestJpaRepository.save(any()) } answers { firstArg() }
            justRun { mockApplicationEventPublisher.publishEvent(any<Any>()) }
        }

        describe("RequestStudentDataEditServiceImpl 클래스의") {
            describe("execute 메서드는") {
                val reqDto =
                    RequestStudentDataEditReqDto(
                        studentIds = listOf(studentId),
                        fields = setOf(StudentDataEditField.MAJOR_CLUB),
                    )

                context("존재하는 학생에게 수정 요청을 보낼 때") {
                    beforeEach {
                        every { mockStudentJpaRepository.findAllById(listOf(studentId)) } returns listOf(student())
                    }

                    it("정보 수정 요청을 저장하고 대상 학생의 로그인 폐기를 요청한다") {
                        service.execute(reqDto)

                        verify(exactly = 1) {
                            mockStudentDataEditRequestJpaRepository.save(match { it.studentId == studentId && it.requestMajorClub })
                        }
                        verify(exactly = 1) {
                            mockApplicationEventPublisher.publishEvent(StudentLoginRevocationRequested(listOf(studentId)))
                        }
                        verify(exactly = 1) { mockApplicationEventPublisher.publishEvent(any<EventDispatchRequested>()) }
                    }

                    it("요청한 필드를 비운다") {
                        val target = student()
                        every { mockStudentJpaRepository.findAllById(listOf(studentId)) } returns listOf(target)

                        service.execute(reqDto)

                        target.majorClub shouldBe null
                    }
                }
            }
        }
    })
