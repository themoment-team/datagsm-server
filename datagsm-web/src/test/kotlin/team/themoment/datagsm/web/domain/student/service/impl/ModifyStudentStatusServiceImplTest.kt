package team.themoment.datagsm.web.domain.student.service.impl

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.data.repository.findByIdOrNull
import team.themoment.datagsm.common.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.common.domain.student.dto.request.UpdateStudentStatusReqDto
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.common.domain.student.entity.constant.StudentRole
import team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository
import team.themoment.sdk.exception.ExpectedException

class ModifyStudentStatusServiceImplTest :
    BehaviorSpec({
        val studentJpaRepository = mockk<StudentJpaRepository>()
        val clubJpaRepository = mockk<ClubJpaRepository>()
        val service = ModifyStudentStatusServiceImpl(studentJpaRepository, clubJpaRepository)

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
            every { clubJpaRepository.findAllByLeader(student) } returns emptyList()

            When("상태 변경을 요청하면") {
                service.execute(studentId, reqDto)

                Then("학생 정보가 초기화되고 졸업생으로 변경된다") {
                    student.role shouldBe StudentRole.GRADUATE
                    student.major.shouldBeNull()
                    student.studentNumber.shouldBeNull()
                    student.dormitoryRoomNumber.shouldBeNull()
                    student.majorClub.shouldBeNull()
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
            every { clubJpaRepository.findAllByLeader(student) } returns emptyList()

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

        Given("자퇴생을 GENERAL_STUDENT로 복원할 때") {
            val studentId = 3L
            val student =
                StudentJpaEntity().apply {
                    id = studentId
                    name = "이영희"
                    email = "lee@gsm.hs.kr"
                    role = StudentRole.WITHDRAWN
                }
            val reqDto = UpdateStudentStatusReqDto(status = StudentRole.GENERAL_STUDENT)

            every { studentJpaRepository.findByIdOrNull(studentId) } returns student

            When("상태 변경을 요청하면") {
                service.execute(studentId, reqDto)

                Then("GENERAL_STUDENT로 변경된다") {
                    student.role shouldBe StudentRole.GENERAL_STUDENT
                }
            }
        }

        Given("졸업생을 STUDENT_COUNCIL로 복원할 때") {
            val studentId = 4L
            val student =
                StudentJpaEntity().apply {
                    id = studentId
                    name = "박민수"
                    email = "park@gsm.hs.kr"
                    role = StudentRole.GRADUATE
                }
            val reqDto = UpdateStudentStatusReqDto(status = StudentRole.STUDENT_COUNCIL)

            every { studentJpaRepository.findByIdOrNull(studentId) } returns student

            When("상태 변경을 요청하면") {
                service.execute(studentId, reqDto)

                Then("STUDENT_COUNCIL로 변경된다") {
                    student.role shouldBe StudentRole.STUDENT_COUNCIL
                }
            }
        }

        Given("자퇴생을 DORMITORY_MANAGER로 복원할 때") {
            val studentId = 5L
            val student =
                StudentJpaEntity().apply {
                    id = studentId
                    name = "최지수"
                    email = "choi@gsm.hs.kr"
                    role = StudentRole.WITHDRAWN
                }
            val reqDto = UpdateStudentStatusReqDto(status = StudentRole.DORMITORY_MANAGER)

            every { studentJpaRepository.findByIdOrNull(studentId) } returns student

            When("상태 변경을 요청하면") {
                service.execute(studentId, reqDto)

                Then("DORMITORY_MANAGER로 변경된다") {
                    student.role shouldBe StudentRole.DORMITORY_MANAGER
                }
            }
        }
    })
