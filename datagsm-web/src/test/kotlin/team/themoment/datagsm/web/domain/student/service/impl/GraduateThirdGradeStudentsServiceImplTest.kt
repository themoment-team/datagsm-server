package team.themoment.datagsm.web.domain.student.service.impl

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

class GraduateThirdGradeStudentsServiceImplTest :
    BehaviorSpec({
        val studentJpaRepository = mockk<StudentJpaRepository>(relaxed = true)
        val graduateThirdGradeStudentsService = GraduateThirdGradeStudentsServiceImpl(studentJpaRepository)

        Given("3학년 학생들이 존재하는 경우") {
            val student1 =
                EnrolledStudent().apply {
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
                EnrolledStudent().apply {
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
                EnrolledStudent().apply {
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
            every { studentJpaRepository.graduateStudentsByIds(listOf(1L, 2L, 3L)) } returns 3

            When("모든 3학년 학생을 졸업 처리하면") {
                Then("졸업 처리된 학생 수가 반환되고 네이티브 쿼리가 호출된다") {
                    val result = graduateThirdGradeStudentsService.execute()

                    result.graduatedCount shouldBe 3
                    verify(exactly = 1) { studentJpaRepository.findStudentsByGrade(3) }
                    verify(exactly = 1) { studentJpaRepository.graduateStudentsByIds(listOf(1L, 2L, 3L)) }
                }
            }
        }

        Given("3학년 학생이 없는 경우") {
            every { studentJpaRepository.findStudentsByGrade(3) } returns emptyList()
            every { studentJpaRepository.graduateStudentsByIds(emptyList()) } returns 0

            When("모든 3학년 학생을 졸업 처리하면") {
                val result = graduateThirdGradeStudentsService.execute()

                Then("졸업 처리된 학생 수가 0이다") {
                    result.graduatedCount shouldBe 0
                }
            }
        }
    })
