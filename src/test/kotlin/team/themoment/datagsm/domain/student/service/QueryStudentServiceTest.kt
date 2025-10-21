package team.themoment.datagsm.domain.student.service

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import team.themoment.datagsm.domain.auth.entity.constant.Role
import team.themoment.datagsm.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.domain.student.entity.constant.DormitoryRoomNumber
import team.themoment.datagsm.domain.student.entity.constant.Major
import team.themoment.datagsm.domain.student.entity.constant.Sex
import team.themoment.datagsm.domain.student.entity.constant.StudentNumber
import team.themoment.datagsm.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.domain.student.service.impl.QueryStudentServiceImpl

class QueryStudentServiceTest :
    DescribeSpec({

        val mockStudentRepository = mockk<StudentJpaRepository>()

        val queryStudentService = QueryStudentServiceImpl(mockStudentRepository)

        afterEach {
            clearAllMocks()
        }

        describe("QueryStudentService 클래스의") {
            describe("execute 메서드는") {

                val testStudent =
                    StudentJpaEntity().apply {
                        studentId = 1L
                        studentName = "홍길동"
                        studentSex = Sex.MAN
                        studentEmail = "hong@gsm.hs.kr"
                        studentNumber = StudentNumber(1, 1, 1)
                        studentMajor = Major.SW_DEVELOPMENT
                        studentRole = Role.GENERAL_STUDENT
                        studentDormitoryRoomNumber = DormitoryRoomNumber(101)
                        studentIsLeaveSchool = false
                    }

                context("존재하는 학생 ID로 검색할 때") {
                    beforeEach {
                        every {
                            mockStudentRepository.searchStudentsWithPaging(
                                studentId = 1L,
                                name = null,
                                email = null,
                                grade = null,
                                classNum = null,
                                number = null,
                                sex = null,
                                role = null,
                                dormitoryRoom = null,
                                isLeaveSchool = false,
                                pageable = PageRequest.of(0, 20),
                            )
                        } returns PageImpl(listOf(testStudent), PageRequest.of(0, 20), 1L)
                    }

                    it("해당 학생 정보가 반환되어야 한다") {
                        val result =
                            queryStudentService.execute(
                                studentId = 1L,
                                name = null,
                                email = null,
                                grade = null,
                                classNum = null,
                                number = null,
                                sex = null,
                                role = null,
                                dormitoryRoom = null,
                                isLeaveSchool = false,
                                page = 0,
                                size = 20,
                            )

                        result.totalElements shouldBe 1L
                        result.totalPages shouldBe 1
                        result.students.size shouldBe 1

                        val student = result.students[0]
                        student.studentId shouldBe 1L
                        student.name shouldBe "홍길동"
                        student.sex shouldBe Sex.MAN
                        student.email shouldBe "hong@gsm.hs.kr"
                        student.grade shouldBe 1
                        student.classNum shouldBe 1
                        student.number shouldBe 1
                        student.studentNumber shouldBe 1101
                        student.major shouldBe Major.SW_DEVELOPMENT
                        student.role shouldBe Role.GENERAL_STUDENT
                        student.dormitoryRoom shouldBe 101
                        student.dormitoryFloor shouldBe 1
                        student.isLeaveSchool shouldBe false

                        verify(exactly = 1) {
                            mockStudentRepository.searchStudentsWithPaging(
                                studentId = 1L,
                                name = null,
                                email = null,
                                grade = null,
                                classNum = null,
                                number = null,
                                sex = null,
                                role = null,
                                dormitoryRoom = null,
                                isLeaveSchool = false,
                                pageable = PageRequest.of(0, 20),
                            )
                        }
                    }
                }

                context("이름과 성별로 다중 조건 검색할 때") {
                    beforeEach {
                        every {
                            mockStudentRepository.searchStudentsWithPaging(
                                studentId = null,
                                name = "홍길동",
                                email = null,
                                grade = null,
                                classNum = null,
                                number = null,
                                sex = Sex.MAN,
                                role = null,
                                dormitoryRoom = null,
                                isLeaveSchool = false,
                                pageable = PageRequest.of(0, 20),
                            )
                        } returns PageImpl(listOf(testStudent), PageRequest.of(0, 20), 1L)
                    }

                    it("조건에 맞는 학생 목록이 반환되어야 한다") {
                        val result =
                            queryStudentService.execute(
                                studentId = null,
                                name = "홍길동",
                                email = null,
                                grade = null,
                                classNum = null,
                                number = null,
                                sex = Sex.MAN,
                                role = null,
                                dormitoryRoom = null,
                                isLeaveSchool = false,
                                page = 0,
                                size = 20,
                            )

                        result.totalElements shouldBe 1L
                        result.students[0].name shouldBe "홍길동"
                        result.students[0].sex shouldBe Sex.MAN
                    }
                }

                context("존재하지 않는 조건으로 검색할 때") {
                    beforeEach {
                        every {
                            mockStudentRepository.searchStudentsWithPaging(
                                studentId = 999L,
                                name = null,
                                email = null,
                                grade = null,
                                classNum = null,
                                number = null,
                                sex = null,
                                role = null,
                                dormitoryRoom = null,
                                isLeaveSchool = false,
                                pageable = PageRequest.of(0, 20),
                            )
                        } returns PageImpl(emptyList(), PageRequest.of(0, 20), 0L)
                    }

                    it("빈 결과가 반환되어야 한다") {
                        val result =
                            queryStudentService.execute(
                                studentId = 999L,
                                name = null,
                                email = null,
                                grade = null,
                                classNum = null,
                                number = null,
                                sex = null,
                                role = null,
                                dormitoryRoom = null,
                                isLeaveSchool = false,
                                page = 0,
                                size = 20,
                            )

                        result.totalElements shouldBe 0L
                        result.totalPages shouldBe 0
                        result.students.size shouldBe 0
                    }
                }

                context("페이지네이션으로 여러 학생을 조회할 때") {
                    val students =
                        (1..50).map { index ->
                            StudentJpaEntity().apply {
                                studentId = index.toLong()
                                studentName = "학생$index"
                                studentSex = if (index % 2 == 0) Sex.WOMAN else Sex.MAN
                                studentEmail = "student$index@gsm.hs.kr"
                                studentNumber = StudentNumber(1, 1, index)
                                studentMajor = Major.SW_DEVELOPMENT
                                studentRole = Role.GENERAL_STUDENT
                                studentDormitoryRoomNumber = DormitoryRoomNumber(100 + index)
                                studentIsLeaveSchool = false
                            }
                        }

                    beforeEach {
                        val firstPageStudents = students.take(20)
                        every {
                            mockStudentRepository.searchStudentsWithPaging(
                                studentId = null,
                                name = null,
                                email = null,
                                grade = null,
                                classNum = null,
                                number = null,
                                sex = null,
                                role = null,
                                dormitoryRoom = null,
                                isLeaveSchool = false,
                                pageable = PageRequest.of(0, 20),
                            )
                        } returns PageImpl(firstPageStudents, PageRequest.of(0, 20), 50L)
                    }

                    it("첫 번째 페이지 결과가 반환되어야 한다") {
                        val result =
                            queryStudentService.execute(
                                studentId = null,
                                name = null,
                                email = null,
                                grade = null,
                                classNum = null,
                                number = null,
                                sex = null,
                                role = null,
                                dormitoryRoom = null,
                                isLeaveSchool = false,
                                page = 0,
                                size = 20,
                            )

                        result.totalElements shouldBe 50L
                        result.totalPages shouldBe 3
                        result.students.size shouldBe 20
                        result.students[0].name shouldBe "학생1"
                        result.students[19].name shouldBe "학생20"
                    }
                }
            }
        }
    })
