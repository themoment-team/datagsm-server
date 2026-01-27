package team.themoment.datagsm.web.domain.student.service.impl

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import team.themoment.datagsm.common.domain.student.entity.DormitoryRoomNumber
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.common.domain.student.entity.StudentNumber
import team.themoment.datagsm.common.domain.student.entity.constant.Major
import team.themoment.datagsm.common.domain.student.entity.constant.Sex
import team.themoment.datagsm.common.domain.student.entity.constant.StudentRole
import team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository

class GraduateThirdGradeStudentsServiceImplTest :
    BehaviorSpec({
        val studentJpaRepository = mockk<StudentJpaRepository>()
        val graduateThirdGradeStudentsService = GraduateThirdGradeStudentsServiceImpl(studentJpaRepository)

        Given("3학년 학생들이 존재하는 경우") {
            val student1 =
                StudentJpaEntity().apply {
                    id = 1L
                    name = "학생1"
                    email = "student1@gsm.hs.kr"
                    sex = Sex.MAN
                    role = StudentRole.GENERAL_STUDENT
                    studentNumber = StudentNumber(3, 1, 1)
                    major = Major.SW_DEVELOPMENT
                    dormitoryRoomNumber = DormitoryRoomNumber(301)
                }

            val student2 =
                StudentJpaEntity().apply {
                    id = 2L
                    name = "학생2"
                    email = "student2@gsm.hs.kr"
                    sex = Sex.WOMAN
                    role = StudentRole.STUDENT_COUNCIL
                    studentNumber = StudentNumber(3, 1, 2)
                    major = Major.SMART_IOT
                    dormitoryRoomNumber = DormitoryRoomNumber(302)
                }

            val student3 =
                StudentJpaEntity().apply {
                    id = 3L
                    name = "학생3"
                    email = "student3@gsm.hs.kr"
                    sex = Sex.MAN
                    role = StudentRole.DORMITORY_MANAGER
                    studentNumber = StudentNumber(3, 2, 1)
                    major = Major.AI
                    dormitoryRoomNumber = DormitoryRoomNumber(401)
                }

            val thirdGradeStudents = listOf(student1, student2, student3)

            every { studentJpaRepository.findStudentsByGrade(3) } returns thirdGradeStudents

            When("모든 3학년 학생을 졸업 처리하면") {
                val result = graduateThirdGradeStudentsService.execute()

                Then("졸업 처리된 학생 수가 반환된다") {
                    result.graduatedCount shouldBe 3
                }

                Then("모든 학생의 정보가 졸업생으로 변경된다") {
                    thirdGradeStudents.forEach { student ->
                        student.role shouldBe StudentRole.GRADUATE
                        student.major shouldBe null
                        student.studentNumber shouldBe null
                        student.dormitoryRoomNumber shouldBe null
                    }
                }

                Then("Repository의 findStudentsByGrade가 호출된다") {
                    verify(exactly = 1) { studentJpaRepository.findStudentsByGrade(3) }
                }
            }
        }

        Given("3학년 학생이 없는 경우") {
            every { studentJpaRepository.findStudentsByGrade(3) } returns emptyList()

            When("모든 3학년 학생을 졸업 처리하면") {
                val result = graduateThirdGradeStudentsService.execute()

                Then("졸업 처리된 학생 수가 0이다") {
                    result.graduatedCount shouldBe 0
                }
            }
        }
    })
