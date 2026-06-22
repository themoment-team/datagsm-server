package team.themoment.datagsm.web.domain.student.service.impl

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import team.themoment.datagsm.common.domain.event.entity.constant.EventType
import team.themoment.datagsm.common.domain.event.service.EventPublisher
import team.themoment.datagsm.common.domain.student.dto.internal.BatchOperationFilter
import team.themoment.datagsm.common.domain.student.dto.internal.BatchOperationType
import team.themoment.datagsm.common.domain.student.dto.request.BatchOperationReqDto
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.common.domain.student.entity.constant.Sex
import team.themoment.datagsm.common.domain.student.entity.constant.StudentRole
import team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository

class BatchOperationServiceImplTest :
    BehaviorSpec({
        val studentJpaRepository = mockk<StudentJpaRepository>()
        val eventPublisher = mockk<EventPublisher>()
        val service = BatchOperationServiceImpl(studentJpaRepository, eventPublisher)

        Given("3학년 학생들이 10명 있을 때") {
            val thirdGradeStudents =
                List(10) { index ->
                    StudentJpaEntity().apply {
                        id = index.toLong()
                        name = "학생$index"
                        email = "student$index@gsm.hs.kr"
                        sex = Sex.MAN
                        role = StudentRole.GENERAL_STUDENT
                    }
                }

            every { studentJpaRepository.findStudentsByGrade(3) } returns thirdGradeStudents
            justRun { eventPublisher.dispatch(any(), any()) }

            When("3학년 일괄 졸업을 요청하면") {
                val reqDto =
                    BatchOperationReqDto(
                        operation = BatchOperationType.GRADUATE,
                        filter = BatchOperationFilter(grade = 3),
                    )

                val result = service.execute(reqDto)

                Then("모든 3학년 학생이 졸업 처리된다") {
                    result.graduatedCount shouldBe 10

                    thirdGradeStudents.forEach { student ->
                        student.role shouldBe StudentRole.GRADUATE
                    }
                }

                Then("STUDENT_CHANGED 이벤트가 1회 발행된다") {
                    verify(exactly = 1) { eventPublisher.dispatch(EventType.STUDENT_CHANGED, any()) }
                }
            }
        }

        Given("3학년 학생이 없을 때") {
            every { studentJpaRepository.findStudentsByGrade(3) } returns emptyList()

            When("3학년 일괄 졸업을 요청하면") {
                val reqDto =
                    BatchOperationReqDto(
                        operation = BatchOperationType.GRADUATE,
                        filter = null,
                    )

                val result = service.execute(reqDto)

                Then("졸업 처리된 학생 수가 0이다") {
                    result.graduatedCount shouldBe 0
                }
            }
        }

        Given("filter가 없을 때") {
            val defaultThirdGradeStudents =
                List(5) { index ->
                    StudentJpaEntity().apply {
                        id = index.toLong()
                        name = "학생$index"
                        email = "student$index@gsm.hs.kr"
                        sex = Sex.MAN
                        role = StudentRole.GENERAL_STUDENT
                    }
                }

            every { studentJpaRepository.findStudentsByGrade(3) } returns defaultThirdGradeStudents
            justRun { eventPublisher.dispatch(any(), any()) }

            When("일괄 졸업을 요청하면") {
                val reqDto =
                    BatchOperationReqDto(
                        operation = BatchOperationType.GRADUATE,
                        filter = null,
                    )

                val result = service.execute(reqDto)

                Then("기본값 3학년으로 처리된다") {
                    result.graduatedCount shouldBe 5
                }
            }
        }
    })
