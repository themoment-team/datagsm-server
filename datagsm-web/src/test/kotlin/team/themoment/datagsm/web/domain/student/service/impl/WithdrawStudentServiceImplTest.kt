package team.themoment.datagsm.web.domain.student.service.impl

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import team.themoment.datagsm.common.domain.club.entity.ClubJpaEntity
import team.themoment.datagsm.common.domain.student.entity.DormitoryRoomNumber
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.common.domain.student.entity.StudentNumber
import team.themoment.datagsm.common.domain.student.entity.constant.Major
import team.themoment.datagsm.common.domain.student.entity.constant.Sex
import team.themoment.datagsm.common.domain.student.entity.constant.StudentRole
import team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository
import team.themoment.sdk.exception.ExpectedException
import java.util.Optional

class WithdrawStudentServiceImplTest :
    BehaviorSpec({
        val studentJpaRepository = mockk<StudentJpaRepository>()
        val withdrawStudentService = WithdrawStudentServiceImpl(studentJpaRepository)

        Given("일반 학생이 존재하는 경우") {
            val studentId = 1L
            val mockClub = mockk<ClubJpaEntity>()
            val student =
                StudentJpaEntity().apply {
                    id = studentId
                    name = "홍길동"
                    email = "hong@gsm.hs.kr"
                    sex = Sex.MAN
                    role = StudentRole.GENERAL_STUDENT
                    studentNumber = StudentNumber(2, 1, 1)
                    major = Major.SW_DEVELOPMENT
                    dormitoryRoomNumber = DormitoryRoomNumber(301)
                    majorClub = mockClub
                    jobClub = mockClub
                    autonomousClub = mockClub
                }

            every { studentJpaRepository.findById(studentId) } returns Optional.of(student)

            When("해당 학생을 자퇴 처리하면") {
                withdrawStudentService.execute(studentId)

                Then("학생 정보가 자퇴생으로 변경된다") {
                    student.role shouldBe StudentRole.WITHDRAWN
                    student.major shouldBe null
                    student.studentNumber shouldBe null
                    student.dormitoryRoomNumber shouldBe null
                    student.majorClub shouldBe null
                    student.jobClub shouldBe null
                    student.autonomousClub shouldBe null
                }

                Then("Repository의 findById가 호출된다") {
                    verify(exactly = 1) { studentJpaRepository.findById(studentId) }
                }
            }
        }

        Given("존재하지 않는 학생 ID가 주어진 경우") {
            val studentId = 999L

            every { studentJpaRepository.findById(studentId) } returns Optional.empty()

            When("자퇴 처리를 시도하면") {
                Then("ExpectedException이 발생한다") {
                    val exception =
                        shouldThrow<ExpectedException> {
                            withdrawStudentService.execute(studentId)
                        }

                    exception.message shouldBe "학생을 찾을 수 없습니다. ID: $studentId"
                }
            }
        }
    })
