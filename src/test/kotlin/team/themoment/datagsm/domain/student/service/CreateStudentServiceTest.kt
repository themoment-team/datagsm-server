package team.themoment.datagsm.domain.student.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import team.themoment.datagsm.domain.auth.entity.constant.Role
import team.themoment.datagsm.domain.student.dto.request.StudentCreateReqDto
import team.themoment.datagsm.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.domain.student.entity.constant.DormitoryRoomNumber
import team.themoment.datagsm.domain.student.entity.constant.Major
import team.themoment.datagsm.domain.student.entity.constant.Sex
import team.themoment.datagsm.domain.student.entity.constant.StudentNumber
import team.themoment.datagsm.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.domain.student.service.impl.CreateStudentServiceImpl

class CreateStudentServiceTest :
    DescribeSpec({

        val mockStudentRepository = mockk<StudentJpaRepository>()

        val createStudentService = CreateStudentServiceImpl(mockStudentRepository)

        afterEach {
            clearAllMocks()
        }

        describe("CreateStudentService 클래스의") {
            describe("createStudent 메서드는") {

                context("유효한 1반 학생 정보로 생성 요청할 때") {
                    val createRequest =
                        StudentCreateReqDto(
                            name = "김학생",
                            sex = Sex.WOMAN,
                            email = "kim@gsm.hs.kr",
                            grade = 2,
                            classNum = 1,
                            number = 15,
                            role = Role.GENERAL_STUDENT,
                            dormitoryRoomNumber = 205,
                        )

                    val savedStudent =
                        StudentJpaEntity().apply {
                            studentId = 1L
                            studentName = createRequest.name
                            studentSex = createRequest.sex
                            studentEmail = createRequest.email
                            studentNumber =
                                StudentNumber(createRequest.grade, createRequest.classNum, createRequest.number)
                            studentMajor = Major.SW_DEVELOPMENT
                            studentRole = createRequest.role
                            studentDormitoryRoomNumber = DormitoryRoomNumber(createRequest.dormitoryRoomNumber)
                            studentIsLeaveSchool = false
                        }

                    beforeEach {
                        every { mockStudentRepository.existsByStudentEmail(createRequest.email) } returns false
                        every {
                            mockStudentRepository.existsByStudentNumber(
                                createRequest.grade,
                                createRequest.classNum,
                                createRequest.number,
                            )
                        } returns false
                        every { mockStudentRepository.save(any()) } returns savedStudent
                    }

                    it("새로운 학생을 생성하고 저장 후 결과를 반환한다") {
                        val result = createStudentService.createStudent(createRequest)

                        result.totalElements shouldBe 1L
                        result.totalPages shouldBe 1
                        result.students.size shouldBe 1

                        val student = result.students[0]
                        student.name shouldBe "김학생"
                        student.sex shouldBe Sex.WOMAN
                        student.email shouldBe "kim@gsm.hs.kr"
                        student.grade shouldBe 2
                        student.classNum shouldBe 1
                        student.number shouldBe 15
                        student.studentNumber shouldBe 2115
                        student.major shouldBe Major.SW_DEVELOPMENT
                        student.role shouldBe Role.GENERAL_STUDENT
                        student.dormitoryFloor shouldBe 2
                        student.dormitoryRoom shouldBe 205
                        student.isLeaveSchool shouldBe false

                        verify(exactly = 1) { mockStudentRepository.existsByStudentEmail(createRequest.email) }
                        verify(exactly = 1) {
                            mockStudentRepository.existsByStudentNumber(
                                createRequest.grade,
                                createRequest.classNum,
                                createRequest.number,
                            )
                        }
                        verify(exactly = 1) { mockStudentRepository.save(any()) }
                    }
                }

                context("유효한 3반 학생 정보로 생성 요청할 때") {
                    val createRequest =
                        StudentCreateReqDto(
                            name = "이학생",
                            sex = Sex.MAN,
                            email = "lee@gsm.hs.kr",
                            grade = 1,
                            classNum = 3,
                            number = 5,
                            role = Role.GENERAL_STUDENT,
                            dormitoryRoomNumber = 301,
                        )

                    val savedStudent =
                        StudentJpaEntity().apply {
                            studentId = 2L
                            studentName = createRequest.name
                            studentSex = createRequest.sex
                            studentEmail = createRequest.email
                            studentNumber =
                                StudentNumber(createRequest.grade, createRequest.classNum, createRequest.number)
                            studentMajor = Major.SMART_IOT
                            studentRole = createRequest.role
                            studentDormitoryRoomNumber = DormitoryRoomNumber(createRequest.dormitoryRoomNumber)
                            studentIsLeaveSchool = false
                        }

                    beforeEach {
                        every { mockStudentRepository.existsByStudentEmail(createRequest.email) } returns false
                        every {
                            mockStudentRepository.existsByStudentNumber(
                                createRequest.grade,
                                createRequest.classNum,
                                createRequest.number,
                            )
                        } returns false
                        every { mockStudentRepository.save(any()) } returns savedStudent
                    }

                    it("3반 학생이 SMART_IOT 전공으로 생성되어야 한다") {
                        val result = createStudentService.createStudent(createRequest)

                        val student = result.students[0]
                        student.major shouldBe Major.SMART_IOT
                        student.classNum shouldBe 3
                        student.studentNumber shouldBe 1305
                        student.dormitoryFloor shouldBe 3
                    }
                }

                context("이미 존재하는 이메일로 생성 요청할 때") {
                    val createRequest =
                        StudentCreateReqDto(
                            name = "중복학생",
                            sex = Sex.WOMAN,
                            email = "duplicate@gsm.hs.kr",
                            grade = 1,
                            classNum = 1,
                            number = 1,
                            role = Role.GENERAL_STUDENT,
                            dormitoryRoomNumber = 201,
                        )

                    beforeEach {
                        every { mockStudentRepository.existsByStudentEmail(createRequest.email) } returns true
                    }

                    it("IllegalArgumentException이 발생해야 한다") {
                        val exception =
                            shouldThrow<IllegalArgumentException> {
                                createStudentService.createStudent(createRequest)
                            }

                        exception.message shouldBe "이미 존재하는 이메일입니다: ${createRequest.email}"

                        verify(exactly = 1) { mockStudentRepository.existsByStudentEmail(createRequest.email) }
                        verify(exactly = 0) { mockStudentRepository.save(any()) }
                    }
                }

                context("이미 존재하는 학번으로 생성 요청할 때") {
                    val createRequest =
                        StudentCreateReqDto(
                            name = "학번중복",
                            sex = Sex.MAN,
                            email = "unique@gsm.hs.kr",
                            grade = 2,
                            classNum = 2,
                            number = 10,
                            role = Role.GENERAL_STUDENT,
                            dormitoryRoomNumber = 410,
                        )

                    beforeEach {
                        every { mockStudentRepository.existsByStudentEmail(createRequest.email) } returns false
                        every {
                            mockStudentRepository.existsByStudentNumber(
                                createRequest.grade,
                                createRequest.classNum,
                                createRequest.number,
                            )
                        } returns true
                    }

                    it("IllegalArgumentException이 발생해야 한다") {
                        val exception =
                            shouldThrow<IllegalArgumentException> {
                                createStudentService.createStudent(createRequest)
                            }

                        exception.message shouldBe
                            "이미 존재하는 학번입니다: ${createRequest.grade}학년 ${createRequest.classNum}반" +
                            " ${createRequest.number}번"

                        verify(exactly = 1) { mockStudentRepository.existsByStudentEmail(createRequest.email) }
                        verify(exactly = 1) {
                            mockStudentRepository.existsByStudentNumber(
                                createRequest.grade,
                                createRequest.classNum,
                                createRequest.number,
                            )
                        }
                        verify(exactly = 0) { mockStudentRepository.save(any()) }
                    }
                }

                context("유효하지 않은 학급으로 생성 요청할 때") {
                    val createRequest =
                        StudentCreateReqDto(
                            name = "무효학급",
                            sex = Sex.WOMAN,
                            email = "invalid@gsm.hs.kr",
                            grade = 1,
                            classNum = 5,
                            number = 1,
                            role = Role.GENERAL_STUDENT,
                            dormitoryRoomNumber = 201,
                        )

                    beforeEach {
                        every { mockStudentRepository.existsByStudentEmail(createRequest.email) } returns false
                        every {
                            mockStudentRepository.existsByStudentNumber(
                                createRequest.grade,
                                createRequest.classNum,
                                createRequest.number,
                            )
                        } returns false
                    }

                    it("IllegalArgumentException이 발생해야 한다") {
                        val exception =
                            shouldThrow<IllegalArgumentException> {
                                createStudentService.createStudent(createRequest)
                            }

                        exception.message shouldBe "유효하지 않은 학급입니다: ${createRequest.classNum}"
                    }
                }

                context("1반 학생 생성 시 SW_DEVELOPMENT 전공이 할당될 때") {
                    val createRequest =
                        StudentCreateReqDto(
                            name = "1반학생",
                            sex = Sex.WOMAN,
                            email = "class1@gsm.hs.kr",
                            grade = 3,
                            classNum = 1,
                            number = 1,
                            role = Role.GENERAL_STUDENT,
                            dormitoryRoomNumber = 211,
                        )

                    val savedStudent =
                        StudentJpaEntity().apply {
                            studentId = 1L
                            studentName = createRequest.name
                            studentSex = createRequest.sex
                            studentEmail = createRequest.email
                            studentNumber =
                                StudentNumber(createRequest.grade, createRequest.classNum, createRequest.number)
                            studentMajor = Major.SW_DEVELOPMENT
                            studentRole = createRequest.role
                            studentDormitoryRoomNumber = DormitoryRoomNumber(createRequest.dormitoryRoomNumber)
                            studentIsLeaveSchool = false
                        }

                    beforeEach {
                        every { mockStudentRepository.existsByStudentEmail(createRequest.email) } returns false
                        every {
                            mockStudentRepository.existsByStudentNumber(
                                createRequest.grade,
                                createRequest.classNum,
                                createRequest.number,
                            )
                        } returns false
                        every { mockStudentRepository.save(any()) } returns savedStudent
                    }

                    it("1반 학생이 SW_DEVELOPMENT 전공으로 생성되어야 한다") {
                        val result = createStudentService.createStudent(createRequest)

                        val student = result.students[0]
                        student.major shouldBe Major.SW_DEVELOPMENT
                        student.classNum shouldBe 1
                        student.name shouldBe "1반학생"
                    }
                }

                context("2반 학생 생성 시 SW_DEVELOPMENT 전공이 할당될 때") {
                    val createRequest =
                        StudentCreateReqDto(
                            name = "2반학생",
                            sex = Sex.MAN,
                            email = "class2@gsm.hs.kr",
                            grade = 2,
                            classNum = 2,
                            number = 2,
                            role = Role.GENERAL_STUDENT,
                            dormitoryRoomNumber = 222,
                        )

                    val savedStudent =
                        StudentJpaEntity().apply {
                            studentId = 2L
                            studentName = createRequest.name
                            studentSex = createRequest.sex
                            studentEmail = createRequest.email
                            studentNumber =
                                StudentNumber(createRequest.grade, createRequest.classNum, createRequest.number)
                            studentMajor = Major.SW_DEVELOPMENT
                            studentRole = createRequest.role
                            studentDormitoryRoomNumber = DormitoryRoomNumber(createRequest.dormitoryRoomNumber)
                            studentIsLeaveSchool = false
                        }

                    beforeEach {
                        every { mockStudentRepository.existsByStudentEmail(createRequest.email) } returns false
                        every {
                            mockStudentRepository.existsByStudentNumber(
                                createRequest.grade,
                                createRequest.classNum,
                                createRequest.number,
                            )
                        } returns false
                        every { mockStudentRepository.save(any()) } returns savedStudent
                    }

                    it("2반 학생이 SW_DEVELOPMENT 전공으로 생성되어야 한다") {
                        val result = createStudentService.createStudent(createRequest)

                        val student = result.students[0]
                        student.major shouldBe Major.SW_DEVELOPMENT
                        student.classNum shouldBe 2
                        student.name shouldBe "2반학생"
                    }
                }

                context("4반 학생 생성 시 AI 전공이 할당될 때") {
                    val createRequest =
                        StudentCreateReqDto(
                            name = "4반학생",
                            sex = Sex.MAN,
                            email = "class4@gsm.hs.kr",
                            grade = 1,
                            classNum = 4,
                            number = 4,
                            role = Role.GENERAL_STUDENT,
                            dormitoryRoomNumber = 241,
                        )

                    val savedStudent =
                        StudentJpaEntity().apply {
                            studentId = 4L
                            studentName = createRequest.name
                            studentSex = createRequest.sex
                            studentEmail = createRequest.email
                            studentNumber =
                                StudentNumber(createRequest.grade, createRequest.classNum, createRequest.number)
                            studentMajor = Major.AI
                            studentRole = createRequest.role
                            studentDormitoryRoomNumber = DormitoryRoomNumber(createRequest.dormitoryRoomNumber)
                            studentIsLeaveSchool = false
                        }

                    beforeEach {
                        every { mockStudentRepository.existsByStudentEmail(createRequest.email) } returns false
                        every {
                            mockStudentRepository.existsByStudentNumber(
                                createRequest.grade,
                                createRequest.classNum,
                                createRequest.number,
                            )
                        } returns false
                        every { mockStudentRepository.save(any()) } returns savedStudent
                    }

                    it("4반 학생이 AI 전공으로 생성되어야 한다") {
                        val result = createStudentService.createStudent(createRequest)

                        val student = result.students[0]
                        student.major shouldBe Major.AI
                        student.classNum shouldBe 4
                        student.name shouldBe "4반학생"
                    }
                }
            }
        }
    })
