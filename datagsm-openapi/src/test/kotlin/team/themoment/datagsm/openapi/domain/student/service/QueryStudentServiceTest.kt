package team.themoment.datagsm.openapi.domain.student.service

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import team.themoment.datagsm.common.domain.club.entity.ClubJpaEntity
import team.themoment.datagsm.common.domain.club.entity.constant.ClubType
import team.themoment.datagsm.common.domain.student.dto.request.QueryStudentReqDto
import team.themoment.datagsm.common.domain.student.entity.DormitoryRoomNumber
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.common.domain.student.entity.StudentNumber
import team.themoment.datagsm.common.domain.student.entity.constant.Major
import team.themoment.datagsm.common.domain.student.entity.constant.Sex
import team.themoment.datagsm.common.domain.student.entity.constant.StudentRole
import team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.openapi.domain.student.service.impl.QueryStudentServiceImpl

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
                        id = 1L
                        name = "홍길동"
                        sex = Sex.MAN
                        email = "hong@gsm.hs.kr"
                        studentNumber = StudentNumber(1, 1, 1)
                        major = Major.SW_DEVELOPMENT
                        role = StudentRole.GENERAL_STUDENT
                        dormitoryRoomNumber = DormitoryRoomNumber(101)
                    }

                context("존재하는 학생 ID로 검색할 때") {
                    beforeEach {
                        every {
                            mockStudentRepository.searchRegisteredStudentsWithPaging(
                                id = 1L,
                                name = null,
                                email = null,
                                grade = null,
                                classNum = null,
                                number = null,
                                sex = null,
                                role = null,
                                dormitoryRoom = null,
                                includeGraduates = false,
                                pageable = PageRequest.of(0, 300),
                                sortBy = any(),
                                sortDirection = any(),
                            )
                        } returns PageImpl(listOf(testStudent), PageRequest.of(0, 300), 1L)
                    }

                    it("해당 학생 정보가 반환되어야 한다") {
                        val queryReq = QueryStudentReqDto(studentId = 1L)
                        val result = queryStudentService.execute(queryReq)

                        result.totalElements shouldBe 1L
                        result.totalPages shouldBe 1
                        result.students.size shouldBe 1

                        val student = result.students[0]
                        student.id shouldBe 1L
                        student.name shouldBe "홍길동"
                        student.sex shouldBe Sex.MAN
                        student.email shouldBe "hong@gsm.hs.kr"
                        student.grade shouldBe 1
                        student.classNum shouldBe 1
                        student.number shouldBe 1
                        student.studentNumber shouldBe 1101
                        student.major shouldBe Major.SW_DEVELOPMENT
                        student.role shouldBe StudentRole.GENERAL_STUDENT
                        student.dormitoryRoom shouldBe 101
                        student.dormitoryFloor shouldBe 1

                        verify(exactly = 1) {
                            mockStudentRepository.searchRegisteredStudentsWithPaging(
                                id = 1L,
                                name = null,
                                email = null,
                                grade = null,
                                classNum = null,
                                number = null,
                                sex = null,
                                role = null,
                                dormitoryRoom = null,
                                includeGraduates = false,
                                pageable = PageRequest.of(0, 300),
                                sortBy = any(),
                                sortDirection = any(),
                            )
                        }
                    }
                }

                context("이름과 성별로 다중 조건 검색할 때") {
                    beforeEach {
                        every {
                            mockStudentRepository.searchRegisteredStudentsWithPaging(
                                id = null,
                                name = "홍길동",
                                email = null,
                                grade = null,
                                classNum = null,
                                number = null,
                                sex = Sex.MAN,
                                role = null,
                                dormitoryRoom = null,
                                includeGraduates = false,
                                pageable = PageRequest.of(0, 300),
                                sortBy = any(),
                                sortDirection = any(),
                            )
                        } returns PageImpl(listOf(testStudent), PageRequest.of(0, 300), 1L)
                    }

                    it("조건에 맞는 학생 목록이 반환되어야 한다") {
                        val queryReq = QueryStudentReqDto(name = "홍길동", sex = Sex.MAN)
                        val result = queryStudentService.execute(queryReq)

                        result.totalElements shouldBe 1L
                        result.students[0].name shouldBe "홍길동"
                        result.students[0].sex shouldBe Sex.MAN
                    }
                }

                context("존재하지 않는 조건으로 검색할 때") {
                    beforeEach {
                        every {
                            mockStudentRepository.searchRegisteredStudentsWithPaging(
                                id = 999L,
                                name = null,
                                email = null,
                                grade = null,
                                classNum = null,
                                number = null,
                                sex = null,
                                role = null,
                                dormitoryRoom = null,
                                includeGraduates = false,
                                pageable = PageRequest.of(0, 300),
                                sortBy = any(),
                                sortDirection = any(),
                            )
                        } returns PageImpl(emptyList(), PageRequest.of(0, 300), 0L)
                    }

                    it("빈 결과가 반환되어야 한다") {
                        val queryReq = QueryStudentReqDto(studentId = 999L)
                        val result = queryStudentService.execute(queryReq)

                        result.totalElements shouldBe 0L
                        result.totalPages shouldBe 0
                        result.students.size shouldBe 0
                    }
                }

                context("졸업생 포함 옵션으로 검색할 때") {
                    beforeEach {
                        every {
                            mockStudentRepository.searchRegisteredStudentsWithPaging(
                                id = null,
                                name = null,
                                email = null,
                                grade = null,
                                classNum = null,
                                number = null,
                                sex = null,
                                role = null,
                                dormitoryRoom = null,
                                includeGraduates = true,
                                pageable = PageRequest.of(0, 300),
                                sortBy = any(),
                                sortDirection = any(),
                            )
                        } returns PageImpl(listOf(testStudent), PageRequest.of(0, 300), 1L)
                    }

                    it("졸업생 포함 결과가 반환되어야 한다") {
                        val queryReq = QueryStudentReqDto(includeGraduates = true)
                        val result = queryStudentService.execute(queryReq)

                        result.totalElements shouldBe 1L
                        result.students.size shouldBe 1
                    }
                }

                context("동아리 정보가 있는 학생을 조회할 때") {
                    val majorClub =
                        ClubJpaEntity().apply {
                            id = 100L
                            name = "SW개발동아리"
                            type = ClubType.MAJOR_CLUB
                        }
                    val studentWithClub =
                        StudentJpaEntity().apply {
                            id = 2L
                            name = "김철수"
                            sex = Sex.MAN
                            email = "kim@gsm.hs.kr"
                            studentNumber = StudentNumber(2, 2, 5)
                            major = Major.AI
                            role = StudentRole.GENERAL_STUDENT
                            this.majorClub = majorClub
                        }

                    beforeEach {
                        every {
                            mockStudentRepository.searchRegisteredStudentsWithPaging(
                                id = 2L,
                                name = null,
                                email = null,
                                grade = null,
                                classNum = null,
                                number = null,
                                sex = null,
                                role = null,
                                dormitoryRoom = null,
                                includeGraduates = false,
                                pageable = PageRequest.of(0, 300),
                                sortBy = any(),
                                sortDirection = any(),
                            )
                        } returns PageImpl(listOf(studentWithClub), PageRequest.of(0, 300), 1L)
                    }

                    it("동아리 정보가 DTO로 매핑되어야 한다") {
                        val queryReq = QueryStudentReqDto(studentId = 2L)
                        val result = queryStudentService.execute(queryReq)

                        result.students.size shouldBe 1
                        val student = result.students[0]
                        student.majorClub?.id shouldBe 100L
                        student.majorClub?.name shouldBe "SW개발동아리"
                        student.majorClub?.type shouldBe ClubType.MAJOR_CLUB
                        student.jobClub shouldBe null
                        student.autonomousClub shouldBe null
                    }
                }
            }
        }
    })
