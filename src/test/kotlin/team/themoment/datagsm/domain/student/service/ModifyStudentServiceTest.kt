package team.themoment.datagsm.domain.student.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import team.themoment.datagsm.domain.auth.entity.constant.Role
import team.themoment.datagsm.domain.student.dto.request.StudentUpdateReqDto
import team.themoment.datagsm.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.domain.student.entity.constant.DormitoryRoomNumber
import team.themoment.datagsm.domain.student.entity.constant.Major
import team.themoment.datagsm.domain.student.entity.constant.Sex
import team.themoment.datagsm.domain.student.entity.constant.StudentNumber
import team.themoment.datagsm.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.domain.student.service.impl.ModifyStudentServiceImpl
import java.util.*

class ModifyStudentServiceTest : DescribeSpec({

    val mockStudentRepository = mockk<StudentJpaRepository>()

    val modifyStudentService = ModifyStudentServiceImpl(mockStudentRepository)

    afterEach {
        clearAllMocks()
    }

    describe("ModifyStudentService 클래스의") {
        describe("execute 메서드는") {

            val studentId = 1L
            val existingStudent = StudentJpaEntity().apply {
                this.studentId = studentId
                studentName = "기존학생"
                studentSex = Sex.MAN
                studentEmail = "existing@gsm.hs.kr"
                studentNumber = StudentNumber(2, 1, 5)
                studentMajor = Major.SW_DEVELOPMENT
                studentRole = Role.GENERAL_STUDENT
                studentDormitoryRoomNumber = DormitoryRoomNumber(201)
                studentIsLeaveSchool = false
            }

            context("존재하는 학생의 이름을 수정할 때") {
                val updateRequest = StudentUpdateReqDto(
                    name = "수정된이름",
                    sex = null,
                    email = null,
                    grade = null,
                    classNum = null,
                    number = null,
                    role = null,
                    dormitoryRoomNumber = null
                )

                beforeEach {
                    every { mockStudentRepository.findById(studentId) } returns Optional.of(existingStudent)
                    every { mockStudentRepository.save(any()) } returns existingStudent.apply {
                        studentName = updateRequest.name!!
                    }
                }

                it("학생 이름이 성공적으로 업데이트되어야 한다") {
                    val result = modifyStudentService.execute(studentId, updateRequest)

                    result.students.size shouldBe 1
                    result.students[0].studentId shouldBe studentId
                    result.students[0].name shouldBe "수정된이름"

                    verify(exactly = 1) { mockStudentRepository.findById(studentId) }
                    verify(exactly = 1) { mockStudentRepository.save(any()) }
                }
            }

            context("학생의 학년만 변경할 때 (반은 변경하지 않음)") {
                val updateRequest = StudentUpdateReqDto(
                    name = null,
                    sex = null,
                    email = null,
                    grade = 3,
                    classNum = null,
                    number = null,
                    role = null,
                    dormitoryRoomNumber = null
                )

                beforeEach {
                    every { mockStudentRepository.findById(studentId) } returns Optional.of(existingStudent)
                    every {
                        mockStudentRepository.existsByStudentNumberAndNotStudentId(
                            3,
                            1,
                            5,
                            studentId
                        )
                    } returns false
                    every { mockStudentRepository.save(any()) } returns existingStudent.apply {
                        studentNumber = StudentNumber(3, 1, 5)
                    }
                }

                it("학년만 변경하고 반이 변경되지 않으면 전공은 유지되어야 한다") {
                    val result = modifyStudentService.execute(studentId, updateRequest)

                    val student = result.students[0]
                    student.grade shouldBe 3
                    student.classNum shouldBe 1
                    student.major shouldBe Major.SW_DEVELOPMENT
                    student.studentNumber shouldBe 3105

                    verify(exactly = 1) {
                        mockStudentRepository.existsByStudentNumberAndNotStudentId(
                            3,
                            1,
                            5,
                            studentId
                        )
                    }
                }
            }

            context("학생의 반만 변경할 때") {
                val updateRequest = StudentUpdateReqDto(
                    name = null,
                    sex = null,
                    email = null,
                    grade = null,
                    classNum = 3,
                    number = null,
                    role = null,
                    dormitoryRoomNumber = null
                )

                beforeEach {
                    every { mockStudentRepository.findById(studentId) } returns Optional.of(existingStudent)
                    every {
                        mockStudentRepository.existsByStudentNumberAndNotStudentId(
                            2,
                            3,
                            5,
                            studentId
                        )
                    } returns false
                    every { mockStudentRepository.save(any()) } returns existingStudent.apply {
                        studentNumber = StudentNumber(2, 3, 5)
                        studentMajor = Major.SMART_IOT
                    }
                }

                it("반 변경 시 새로운 반에 따라 전공이 변경되어야 한다") {
                    val result = modifyStudentService.execute(studentId, updateRequest)

                    val student = result.students[0]
                    student.grade shouldBe 2
                    student.classNum shouldBe 3
                    student.major shouldBe Major.SMART_IOT
                    student.studentNumber shouldBe 2305
                }
            }

            context("학생의 학년과 반을 동시에 변경할 때") {
                val updateRequest = StudentUpdateReqDto(
                    name = null,
                    sex = null,
                    email = null,
                    grade = 1,
                    classNum = 4,
                    number = null,
                    role = null,
                    dormitoryRoomNumber = null
                )

                beforeEach {
                    every { mockStudentRepository.findById(studentId) } returns Optional.of(existingStudent)
                    every {
                        mockStudentRepository.existsByStudentNumberAndNotStudentId(
                            1,
                            4,
                            5,
                            studentId
                        )
                    } returns false
                    every { mockStudentRepository.save(any()) } returns existingStudent.apply {
                        studentNumber = StudentNumber(1, 4, 5)
                        studentMajor = Major.AI
                    }
                }

                it("반 변경 시 새로운 반에 따라 전공이 변경되어야 한다") {
                    val result = modifyStudentService.execute(studentId, updateRequest)

                    val student = result.students[0]
                    student.grade shouldBe 1
                    student.classNum shouldBe 4
                    student.major shouldBe Major.AI
                    student.studentNumber shouldBe 1405
                }
            }

            context("이미 존재하는 이메일로 변경을 시도할 때") {
                val updateRequest = StudentUpdateReqDto(
                    name = null,
                    sex = null,
                    email = "duplicate@gsm.hs.kr",
                    grade = null,
                    classNum = null,
                    number = null,
                    role = null,
                    dormitoryRoomNumber = null
                )

                beforeEach {
                    every { mockStudentRepository.findById(studentId) } returns Optional.of(existingStudent)
                    every {
                        mockStudentRepository.existsByStudentEmailAndNotStudentId(
                            updateRequest.email!!,
                            studentId
                        )
                    } returns true
                }

                it("IllegalArgumentException이 발생해야 한다") {
                    val exception = shouldThrow<IllegalArgumentException> {
                        modifyStudentService.execute(studentId, updateRequest)
                    }

                    exception.message shouldBe "이미 존재하는 이메일입니다: ${updateRequest.email}"

                    verify(exactly = 1) { mockStudentRepository.findById(studentId) }
                    verify(exactly = 1) {
                        mockStudentRepository.existsByStudentEmailAndNotStudentId(
                            updateRequest.email!!,
                            studentId
                        )
                    }
                    verify(exactly = 0) { mockStudentRepository.save(any()) }
                }
            }

            context("이미 존재하는 학번으로 변경을 시도할 때") {
                val updateRequest = StudentUpdateReqDto(
                    name = null,
                    sex = null,
                    email = null,
                    grade = 2,
                    classNum = 2,
                    number = 10,
                    role = null,
                    dormitoryRoomNumber = null
                )

                beforeEach {
                    every { mockStudentRepository.findById(studentId) } returns Optional.of(existingStudent)
                    every {
                        mockStudentRepository.existsByStudentNumberAndNotStudentId(
                            2,
                            2,
                            10,
                            studentId
                        )
                    } returns true
                }

                it("IllegalArgumentException이 발생해야 한다") {
                    val exception = shouldThrow<IllegalArgumentException> {
                        modifyStudentService.execute(studentId, updateRequest)
                    }

                    exception.message shouldBe "이미 존재하는 학번입니다: 2학년 2반 10번"

                    verify(exactly = 1) {
                        mockStudentRepository.existsByStudentNumberAndNotStudentId(
                            2,
                            2,
                            10,
                            studentId
                        )
                    }
                    verify(exactly = 0) { mockStudentRepository.save(any()) }
                }
            }

            context("존재하지 않는 학생 ID로 수정을 시도할 때") {
                val updateRequest = StudentUpdateReqDto(
                    name = "수정이름",
                    sex = null,
                    email = null,
                    grade = null,
                    classNum = null,
                    number = null,
                    role = null,
                    dormitoryRoomNumber = null
                )

                beforeEach {
                    every { mockStudentRepository.findById(studentId) } returns Optional.empty()
                }

                it("IllegalArgumentException이 발생해야 한다") {
                    val exception = shouldThrow<IllegalArgumentException> {
                        modifyStudentService.execute(studentId, updateRequest)
                    }

                    exception.message shouldBe "학생을 찾을 수 없습니다. studentId: $studentId"

                    verify(exactly = 1) { mockStudentRepository.findById(studentId) }
                    verify(exactly = 0) { mockStudentRepository.save(any()) }
                }
            }

            context("유효하지 않은 학급으로 반을 변경할 때") {
                val updateRequest = StudentUpdateReqDto(
                    name = null,
                    sex = null,
                    email = null,
                    grade = null,
                    classNum = 5,
                    number = null,
                    role = null,
                    dormitoryRoomNumber = null
                )

                beforeEach {
                    every { mockStudentRepository.findById(studentId) } returns Optional.of(existingStudent)
                    every {
                        mockStudentRepository.existsByStudentNumberAndNotStudentId(
                            2,
                            5,
                            5,
                            studentId
                        )
                    } returns false
                }

                it("IllegalArgumentException이 발생해야 한다") {
                    val exception = shouldThrow<IllegalArgumentException> {
                        modifyStudentService.execute(studentId, updateRequest)
                    }

                    exception.message shouldBe "유효하지 않은 학급입니다: 5"

                    verify(exactly = 1) { mockStudentRepository.findById(studentId) }
                    verify(exactly = 0) { mockStudentRepository.save(any()) }
                }
            }

            context("기숙사 방 번호를 변경할 때") {
                val updateRequest = StudentUpdateReqDto(
                    name = null,
                    sex = null,
                    email = null,
                    grade = null,
                    classNum = null,
                    number = null,
                    role = null,
                    dormitoryRoomNumber = 305
                )

                beforeEach {
                    every { mockStudentRepository.findById(studentId) } returns Optional.of(existingStudent)
                    every { mockStudentRepository.save(any()) } returns existingStudent.apply {
                        studentDormitoryRoomNumber = DormitoryRoomNumber(updateRequest.dormitoryRoomNumber!!)
                    }
                }

                it("기숙사 방 번호가 성공적으로 변경되어야 한다") {
                    val result = modifyStudentService.execute(studentId, updateRequest)

                    val student = result.students[0]
                    student.dormitoryRoom shouldBe 305
                    student.dormitoryFloor shouldBe 3

                    verify(exactly = 1) { mockStudentRepository.findById(studentId) }
                    verify(exactly = 1) { mockStudentRepository.save(any()) }
                }
            }

            context("학생의 성별과 역할을 동시에 변경할 때") {
                val updateRequest = StudentUpdateReqDto(
                    name = null,
                    sex = Sex.WOMAN,
                    email = null,
                    grade = null,
                    classNum = null,
                    number = null,
                    role = Role.STUDENT_COUNCIL,
                    dormitoryRoomNumber = null
                )

                beforeEach {
                    every { mockStudentRepository.findById(studentId) } returns Optional.of(existingStudent)
                    every { mockStudentRepository.save(any()) } returns existingStudent.apply {
                        studentSex = updateRequest.sex!!
                        studentRole = updateRequest.role!!
                    }
                }

                it("성별과 역할이 성공적으로 변경되어야 한다") {
                    val result = modifyStudentService.execute(studentId, updateRequest)

                    val student = result.students[0]
                    student.sex shouldBe Sex.WOMAN
                    student.role shouldBe Role.STUDENT_COUNCIL

                    verify(exactly = 1) { mockStudentRepository.findById(studentId) }
                    verify(exactly = 1) { mockStudentRepository.save(any()) }
                }
            }

            context("모든 필드를 한번에 수정할 때") {
                val updateRequest = StudentUpdateReqDto(
                    name = "완전수정학생",
                    sex = Sex.WOMAN,
                    email = "updated@gsm.hs.kr",
                    grade = 4,
                    classNum = 2,
                    number = 8,
                    role = Role.DORMITORY_MANAGER,
                    dormitoryRoomNumber = 418
                )

                beforeEach {
                    every { mockStudentRepository.findById(studentId) } returns Optional.of(existingStudent)
                    every {
                        mockStudentRepository.existsByStudentEmailAndNotStudentId(
                            updateRequest.email!!,
                            studentId
                        )
                    } returns false
                    every {
                        mockStudentRepository.existsByStudentNumberAndNotStudentId(
                            4,
                            2,
                            8,
                            studentId
                        )
                    } returns false
                    every { mockStudentRepository.save(any()) } returns existingStudent.apply {
                        studentName = updateRequest.name!!
                        studentSex = updateRequest.sex!!
                        studentEmail = updateRequest.email!!
                        studentNumber =
                            StudentNumber(updateRequest.grade!!, updateRequest.classNum!!, updateRequest.number!!)
                        studentMajor = Major.SW_DEVELOPMENT
                        studentRole = updateRequest.role!!
                        studentDormitoryRoomNumber = DormitoryRoomNumber(updateRequest.dormitoryRoomNumber!!)
                    }
                }

                it("모든 필드가 성공적으로 업데이트되어야 한다") {
                    val result = modifyStudentService.execute(studentId, updateRequest)

                    val student = result.students[0]
                    student.name shouldBe "완전수정학생"
                    student.sex shouldBe Sex.WOMAN
                    student.email shouldBe "updated@gsm.hs.kr"
                    student.grade shouldBe 4
                    student.classNum shouldBe 2
                    student.number shouldBe 8
                    student.studentNumber shouldBe 4208
                    student.major shouldBe Major.SW_DEVELOPMENT
                    student.role shouldBe Role.DORMITORY_MANAGER
                    student.dormitoryRoom shouldBe 418
                    student.dormitoryFloor shouldBe 4

                    verify(exactly = 1) { mockStudentRepository.findById(studentId) }
                    verify(exactly = 1) {
                        mockStudentRepository.existsByStudentEmailAndNotStudentId(
                            updateRequest.email!!,
                            studentId
                        )
                    }
                    verify(exactly = 1) {
                        mockStudentRepository.existsByStudentNumberAndNotStudentId(
                            4,
                            2,
                            8,
                            studentId
                        )
                    }
                    verify(exactly = 1) { mockStudentRepository.save(any()) }
                }
            }
        }
    }
})
