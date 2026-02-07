package team.themoment.datagsm.web.domain.student.service.impl

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.data.repository.findByIdOrNull
import team.themoment.datagsm.common.domain.student.dto.request.UpdateStudentStatusReqDto
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.common.domain.student.entity.constant.StudentRole
import team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository
import team.themoment.sdk.exception.ExpectedException

class UpdateStudentStatusServiceImplTest :
    BehaviorSpec({
        val studentJpaRepository = mockk<StudentJpaRepository>()
        val service = UpdateStudentStatusServiceImpl(studentJpaRepository)

        Given("존재하지 않는 학생 ID로") {
            val studentId = 999L
            val reqDto = UpdateStudentStatusReqDto(status = StudentRole.GRADUATE)

            every { studentJpaRepository.findByIdOrNull(studentId) } returns null

            When("상태 변경을 요청하면") {
                Then("404 Not Found 예외가 발생한다") {
                    val exception =
                        shouldThrow<ExpectedException> {
                            service.execute(studentId, reqDto)
                        }

                    exception.message shouldBe "학생을 찾을 수 없습니다. ID: $studentId"
                }
            }
        }

        Given("학생을 졸업 상태로 변경할 때") {
            val studentId = 1L
            val student =
                StudentJpaEntity().apply {
                    id = studentId
                    name = "홍길동"
                    email = "hong@gsm.hs.kr"
                    role = StudentRole.GENERAL_STUDENT
                }
            val reqDto = UpdateStudentStatusReqDto(status = StudentRole.GRADUATE)

            every { studentJpaRepository.findByIdOrNull(studentId) } returns student

            When("상태 변경을 요청하면") {
                service.execute(studentId, reqDto)

                Then("학생 정보가 초기화되고 졸업생으로 변경된다") {
                    student.role shouldBe StudentRole.GRADUATE
                    student.major.shouldBeNull()
                    student.studentNumber.shouldBeNull()
                    student.dormitoryRoomNumber.shouldBeNull()
                    student.majorClub.shouldBeNull()
                    student.jobClub.shouldBeNull()
                    student.autonomousClub.shouldBeNull()
                }
            }
        }

        Given("학생을 자퇴 상태로 변경할 때") {
            val studentId = 2L
            val student =
                StudentJpaEntity().apply {
                    id = studentId
                    name = "김철수"
                    email = "kim@gsm.hs.kr"
                    role = StudentRole.GENERAL_STUDENT
                }
            val reqDto = UpdateStudentStatusReqDto(status = StudentRole.WITHDRAWN)

            every { studentJpaRepository.findByIdOrNull(studentId) } returns student

            When("상태 변경을 요청하면") {
                service.execute(studentId, reqDto)

                Then("학생 정보가 초기화되고 자퇴생으로 변경된다") {
                    student.role shouldBe StudentRole.WITHDRAWN
                    student.major.shouldBeNull()
                    student.studentNumber.shouldBeNull()
                    student.dormitoryRoomNumber.shouldBeNull()
                }
            }
        }

        Given("지원하지 않는 상태로 변경할 때") {
            val studentId = 3L
            val student =
                StudentJpaEntity().apply {
                    id = studentId
                    name = "이영희"
                    email = "lee@gsm.hs.kr"
                    role = StudentRole.GENERAL_STUDENT
                }
            val reqDto = UpdateStudentStatusReqDto(status = StudentRole.GENERAL_STUDENT)

            every { studentJpaRepository.findByIdOrNull(studentId) } returns student

            When("상태 변경을 요청하면") {
                Then("400 Bad Request 예외가 발생한다") {
                    val exception =
                        shouldThrow<ExpectedException> {
                            service.execute(studentId, reqDto)
                        }

                    exception.message shouldBe "지원하지 않는 상태입니다: ${StudentRole.GENERAL_STUDENT}"
                }
            }
        }
    })
