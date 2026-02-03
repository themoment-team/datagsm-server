package team.themoment.datagsm.web.domain.student.service.impl

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import team.themoment.datagsm.common.domain.student.entity.DormitoryRoomNumber
import team.themoment.datagsm.common.domain.student.entity.EnrolledStudent
import team.themoment.datagsm.common.domain.student.entity.StudentNumber
import team.themoment.datagsm.common.domain.student.entity.constant.Major
import team.themoment.datagsm.common.domain.student.entity.constant.Sex
import team.themoment.datagsm.common.domain.student.entity.constant.StudentRole
import team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository
import team.themoment.sdk.exception.ExpectedException
import java.util.Optional

class GraduateStudentServiceImplTest :
    BehaviorSpec({
        val studentJpaRepository = mockk<StudentJpaRepository>()
        val graduateStudentService = GraduateStudentServiceImpl(studentJpaRepository)

        Given("3학년 학생이 존재하는 경우") {
            val studentId = 1L
            val student =
                EnrolledStudent().apply {
                    id = studentId
                    name = "홍길동"
                    email = "hong@gsm.hs.kr"
                    sex = Sex.MAN
                    role = StudentRole.GENERAL_STUDENT
                    studentNumber = StudentNumber(3, 1, 1)
                    major = Major.SW_DEVELOPMENT
                    dormitoryRoomNumber = DormitoryRoomNumber(301)
                }

            every { studentJpaRepository.findById(studentId) } returns Optional.of(student)
            every { studentJpaRepository.graduateStudentById(studentId) } returns 1

            When("해당 학생을 졸업 처리하면") {
                Then("네이티브 쿼리로 졸업 처리가 된다") {
                    graduateStudentService.execute(studentId)

                    verify(exactly = 1) { studentJpaRepository.findById(studentId) }
                    verify(exactly = 1) { studentJpaRepository.graduateStudentById(studentId) }
                }
            }
        }

        Given("존재하지 않는 학생 ID가 주어진 경우") {
            val studentId = 999L

            every { studentJpaRepository.findById(studentId) } returns Optional.empty()

            When("졸업 처리를 시도하면") {
                Then("ExpectedException이 발생한다") {
                    val exception =
                        shouldThrow<ExpectedException> {
                            graduateStudentService.execute(studentId)
                        }

                    exception.message shouldBe "학생을 찾을 수 없습니다. ID: $studentId"
                }
            }
        }
    })
