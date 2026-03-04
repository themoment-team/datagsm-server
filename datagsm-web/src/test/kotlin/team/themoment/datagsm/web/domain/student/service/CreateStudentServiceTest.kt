package team.themoment.datagsm.web.domain.student.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import team.themoment.datagsm.common.domain.club.entity.ClubJpaEntity
import team.themoment.datagsm.common.domain.club.entity.constant.ClubType
import team.themoment.datagsm.common.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.common.domain.student.dto.request.CreateStudentReqDto
import team.themoment.datagsm.common.domain.student.entity.DormitoryRoomNumber
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.common.domain.student.entity.StudentNumber
import team.themoment.datagsm.common.domain.student.entity.constant.Major
import team.themoment.datagsm.common.domain.student.entity.constant.Sex
import team.themoment.datagsm.common.domain.student.entity.constant.StudentRole
import team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.web.domain.student.service.impl.CreateStudentServiceImpl
import team.themoment.sdk.exception.ExpectedException

class CreateStudentServiceTest :
    DescribeSpec({

        val mockStudentRepository = mockk<StudentJpaRepository>()
        val mockClubRepository = mockk<ClubJpaRepository>()

        val createStudentService = CreateStudentServiceImpl(mockStudentRepository, mockClubRepository)

        afterEach {
            clearAllMocks()
        }

        describe("CreateStudentService 클래스의") {
            describe("execute 메서드는") {

                context("유효한 1반 학생 정보로 생성 요청할 때") {
                    val createRequest =
                        CreateStudentReqDto(
                            name = "김학생",
                            sex = Sex.WOMAN,
                            email = "kim@gsm.hs.kr",
                            grade = 2,
                            classNum = 1,
                            number = 15,
                            role = StudentRole.GENERAL_STUDENT,
                            dormitoryRoomNumber = 205,
                        )

                    val savedStudent =
                        StudentJpaEntity().apply {
                            id = 1L
                            name = createRequest.name
                            sex = createRequest.sex
                            email = createRequest.email
                            studentNumber =
                                StudentNumber(createRequest.grade, createRequest.classNum, createRequest.number)
                            major = Major.SW_DEVELOPMENT
                            role = createRequest.role
                            dormitoryRoomNumber = DormitoryRoomNumber(createRequest.dormitoryRoomNumber)
                        }

                    beforeEach {
                        every { mockStudentRepository.existsByEmail(createRequest.email) } returns false
                        every {
                            mockStudentRepository.existsByStudentNumber(
                                createRequest.grade!!,
                                createRequest.classNum!!,
                                createRequest.number!!,
                            )
                        } returns false
                        every { mockStudentRepository.save(any()) } returns savedStudent
                    }

                    it("새로운 학생을 생성하고 저장 후 결과를 반환한다") {
                        val result = createStudentService.execute(createRequest)

                        result.name shouldBe "김학생"
                        result.sex shouldBe Sex.WOMAN
                        result.email shouldBe "kim@gsm.hs.kr"
                        result.grade shouldBe 2
                        result.classNum shouldBe 1
                        result.number shouldBe 15
                        result.studentNumber shouldBe 2115
                        result.major shouldBe Major.SW_DEVELOPMENT
                        result.role shouldBe StudentRole.GENERAL_STUDENT
                        result.dormitoryFloor shouldBe 2
                        result.dormitoryRoom shouldBe 205

                        verify(exactly = 1) { mockStudentRepository.existsByEmail(createRequest.email) }
                        verify(exactly = 1) {
                            mockStudentRepository.existsByStudentNumber(
                                createRequest.grade!!,
                                createRequest.classNum!!,
                                createRequest.number!!,
                            )
                        }
                        verify(exactly = 1) { mockStudentRepository.save(any()) }
                    }
                }

                context("유효한 3반 학생 정보로 생성 요청할 때") {
                    val createRequest =
                        CreateStudentReqDto(
                            name = "이학생",
                            sex = Sex.MAN,
                            email = "lee@gsm.hs.kr",
                            grade = 1,
                            classNum = 3,
                            number = 5,
                            role = StudentRole.GENERAL_STUDENT,
                            dormitoryRoomNumber = 301,
                        )

                    val savedStudent =
                        StudentJpaEntity().apply {
                            id = 2L
                            name = createRequest.name
                            sex = createRequest.sex
                            email = createRequest.email
                            studentNumber =
                                StudentNumber(createRequest.grade, createRequest.classNum, createRequest.number)
                            major = Major.SMART_IOT
                            role = createRequest.role
                            dormitoryRoomNumber = DormitoryRoomNumber(createRequest.dormitoryRoomNumber)
                        }

                    beforeEach {
                        every { mockStudentRepository.existsByEmail(createRequest.email) } returns false
                        every {
                            mockStudentRepository.existsByStudentNumber(
                                createRequest.grade!!,
                                createRequest.classNum!!,
                                createRequest.number!!,
                            )
                        } returns false
                        every { mockStudentRepository.save(any()) } returns savedStudent
                    }

                    it("3반 학생이 SMART_IOT 전공으로 생성되어야 한다") {
                        val result = createStudentService.execute(createRequest)

                        result.major shouldBe Major.SMART_IOT
                        result.classNum shouldBe 3
                        result.studentNumber shouldBe 1305
                        result.dormitoryFloor shouldBe 3
                    }
                }

                context("이미 존재하는 이메일로 생성 요청할 때") {
                    val createRequest =
                        CreateStudentReqDto(
                            name = "중복학생",
                            sex = Sex.WOMAN,
                            email = "duplicate@gsm.hs.kr",
                            grade = 1,
                            classNum = 1,
                            number = 1,
                            role = StudentRole.GENERAL_STUDENT,
                            dormitoryRoomNumber = 201,
                        )

                    beforeEach {
                        every { mockStudentRepository.existsByEmail(createRequest.email) } returns true
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                createStudentService.execute(createRequest)
                            }

                        exception.message shouldBe "이미 존재하는 이메일입니다: ${createRequest.email}"

                        verify(exactly = 1) { mockStudentRepository.existsByEmail(createRequest.email) }
                        verify(exactly = 0) { mockStudentRepository.save(any()) }
                    }
                }

                context("이미 존재하는 학번으로 생성 요청할 때") {
                    val createRequest =
                        CreateStudentReqDto(
                            name = "학번중복",
                            sex = Sex.MAN,
                            email = "unique@gsm.hs.kr",
                            grade = 2,
                            classNum = 2,
                            number = 10,
                            role = StudentRole.GENERAL_STUDENT,
                            dormitoryRoomNumber = 410,
                        )

                    beforeEach {
                        every { mockStudentRepository.existsByEmail(createRequest.email) } returns false
                        every {
                            mockStudentRepository.existsByStudentNumber(
                                createRequest.grade!!,
                                createRequest.classNum!!,
                                createRequest.number!!,
                            )
                        } returns true
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                createStudentService.execute(createRequest)
                            }

                        exception.message shouldBe
                            "이미 존재하는 학번입니다: ${createRequest.grade}학년 ${createRequest.classNum}반" +
                            " ${createRequest.number}번"

                        verify(exactly = 1) { mockStudentRepository.existsByEmail(createRequest.email) }
                        verify(exactly = 1) {
                            mockStudentRepository.existsByStudentNumber(
                                createRequest.grade!!,
                                createRequest.classNum!!,
                                createRequest.number!!,
                            )
                        }
                        verify(exactly = 0) { mockStudentRepository.save(any()) }
                    }
                }

                context("유효하지 않은 학급으로 생성 요청할 때") {
                    val createRequest =
                        CreateStudentReqDto(
                            name = "무효학급",
                            sex = Sex.WOMAN,
                            email = "invalid@gsm.hs.kr",
                            grade = 1,
                            classNum = 5,
                            number = 1,
                            role = StudentRole.GENERAL_STUDENT,
                            dormitoryRoomNumber = 201,
                        )

                    beforeEach {
                        every { mockStudentRepository.existsByEmail(createRequest.email) } returns false
                        every {
                            mockStudentRepository.existsByStudentNumber(
                                createRequest.grade!!,
                                createRequest.classNum!!,
                                createRequest.number!!,
                            )
                        } returns false
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                createStudentService.execute(createRequest)
                            }

                        exception.message shouldBe "유효하지 않은 학급입니다: ${createRequest.classNum}"
                    }
                }

                context("1반 학생 생성 시 SW_DEVELOPMENT 전공이 할당될 때") {
                    val createRequest =
                        CreateStudentReqDto(
                            name = "1반학생",
                            sex = Sex.WOMAN,
                            email = "class1@gsm.hs.kr",
                            grade = 3,
                            classNum = 1,
                            number = 1,
                            role = StudentRole.GENERAL_STUDENT,
                            dormitoryRoomNumber = 211,
                        )

                    val savedStudent =
                        StudentJpaEntity().apply {
                            id = 1L
                            name = createRequest.name
                            sex = createRequest.sex
                            email = createRequest.email
                            studentNumber =
                                StudentNumber(createRequest.grade, createRequest.classNum, createRequest.number)
                            major = Major.SW_DEVELOPMENT
                            role = createRequest.role
                            dormitoryRoomNumber = DormitoryRoomNumber(createRequest.dormitoryRoomNumber)
                        }

                    beforeEach {
                        every { mockStudentRepository.existsByEmail(createRequest.email) } returns false
                        every {
                            mockStudentRepository.existsByStudentNumber(
                                createRequest.grade!!,
                                createRequest.classNum!!,
                                createRequest.number!!,
                            )
                        } returns false
                        every { mockStudentRepository.save(any()) } returns savedStudent
                    }

                    it("1반 학생이 SW_DEVELOPMENT 전공으로 생성되어야 한다") {
                        val result = createStudentService.execute(createRequest)

                        result.major shouldBe Major.SW_DEVELOPMENT
                        result.classNum shouldBe 1
                        result.name shouldBe "1반학생"
                    }
                }

                context("2반 학생 생성 시 SW_DEVELOPMENT 전공이 할당될 때") {
                    val createRequest =
                        CreateStudentReqDto(
                            name = "2반학생",
                            sex = Sex.MAN,
                            email = "class2@gsm.hs.kr",
                            grade = 2,
                            classNum = 2,
                            number = 2,
                            role = StudentRole.GENERAL_STUDENT,
                            dormitoryRoomNumber = 222,
                        )

                    val savedStudent =
                        StudentJpaEntity().apply {
                            id = 2L
                            name = createRequest.name
                            sex = createRequest.sex
                            email = createRequest.email
                            studentNumber =
                                StudentNumber(createRequest.grade, createRequest.classNum, createRequest.number)
                            major = Major.SW_DEVELOPMENT
                            role = createRequest.role
                            dormitoryRoomNumber = DormitoryRoomNumber(createRequest.dormitoryRoomNumber)
                        }

                    beforeEach {
                        every { mockStudentRepository.existsByEmail(createRequest.email) } returns false
                        every {
                            mockStudentRepository.existsByStudentNumber(
                                createRequest.grade!!,
                                createRequest.classNum!!,
                                createRequest.number!!,
                            )
                        } returns false
                        every { mockStudentRepository.save(any()) } returns savedStudent
                    }

                    it("2반 학생이 SW_DEVELOPMENT 전공으로 생성되어야 한다") {
                        val result = createStudentService.execute(createRequest)

                        result.major shouldBe Major.SW_DEVELOPMENT
                        result.classNum shouldBe 2
                        result.name shouldBe "2반학생"
                    }
                }

                context("4반 학생 생성 시 AI 전공이 할당될 때") {
                    val createRequest =
                        CreateStudentReqDto(
                            name = "4반학생",
                            sex = Sex.MAN,
                            email = "class4@gsm.hs.kr",
                            grade = 1,
                            classNum = 4,
                            number = 4,
                            role = StudentRole.GENERAL_STUDENT,
                            dormitoryRoomNumber = 241,
                        )

                    val savedStudent =
                        StudentJpaEntity().apply {
                            id = 4L
                            name = createRequest.name
                            sex = createRequest.sex
                            email = createRequest.email
                            studentNumber =
                                StudentNumber(createRequest.grade, createRequest.classNum, createRequest.number)
                            major = Major.AI
                            role = createRequest.role
                            dormitoryRoomNumber = DormitoryRoomNumber(createRequest.dormitoryRoomNumber)
                        }

                    beforeEach {
                        every { mockStudentRepository.existsByEmail(createRequest.email) } returns false
                        every {
                            mockStudentRepository.existsByStudentNumber(
                                createRequest.grade!!,
                                createRequest.classNum!!,
                                createRequest.number!!,
                            )
                        } returns false
                        every { mockStudentRepository.save(any()) } returns savedStudent
                    }

                    it("4반 학생이 AI 전공으로 생성되어야 한다") {
                        val result = createStudentService.execute(createRequest)

                        result.major shouldBe Major.AI
                        result.classNum shouldBe 4
                        result.name shouldBe "4반학생"
                    }
                }

                context("유효한 클럽 ID들과 함께 학생 생성 요청할 때") {
                    val majorClub =
                        ClubJpaEntity().apply {
                            id = 1L
                            name = "SW개발동아리"
                            type = ClubType.MAJOR_CLUB
                        }
                    val jobClub =
                        ClubJpaEntity().apply {
                            id = 2L
                            name = "취업동아리"
                            type = ClubType.JOB_CLUB
                        }
                    val autonomousClub =
                        ClubJpaEntity().apply {
                            id = 3L
                            name = "자율동아리"
                            type = ClubType.AUTONOMOUS_CLUB
                        }

                    val createRequest =
                        CreateStudentReqDto(
                            name = "동아리학생",
                            sex = Sex.WOMAN,
                            email = "club@gsm.hs.kr",
                            grade = 2,
                            classNum = 1,
                            number = 10,
                            role = StudentRole.GENERAL_STUDENT,
                            dormitoryRoomNumber = 210,
                            majorClubId = 1L,
                            jobClubId = 2L,
                            autonomousClubId = 3L,
                        )

                    val savedStudent =
                        StudentJpaEntity().apply {
                            id = 1L
                            name = createRequest.name
                            sex = createRequest.sex
                            email = createRequest.email
                            studentNumber =
                                StudentNumber(createRequest.grade, createRequest.classNum, createRequest.number)
                            major = Major.SW_DEVELOPMENT
                            role = createRequest.role
                            dormitoryRoomNumber = DormitoryRoomNumber(createRequest.dormitoryRoomNumber)
                            this.majorClub = majorClub
                            this.jobClub = jobClub
                            this.autonomousClub = autonomousClub
                        }

                    beforeEach {
                        every { mockStudentRepository.existsByEmail(createRequest.email) } returns false
                        every {
                            mockStudentRepository.existsByStudentNumber(
                                createRequest.grade!!,
                                createRequest.classNum!!,
                                createRequest.number!!,
                            )
                        } returns false
                        every {
                            mockClubRepository.findAllById(listOf(1L, 2L, 3L))
                        } returns listOf(majorClub, jobClub, autonomousClub)
                        every { mockStudentRepository.save(any()) } returns savedStudent
                    }

                    it("클럽 정보와 함께 학생이 생성되어야 한다") {
                        val result = createStudentService.execute(createRequest)

                        result.name shouldBe "동아리학생"
                        result.majorClub?.id shouldBe 1L
                        result.majorClub?.name shouldBe "SW개발동아리"
                        result.jobClub?.id shouldBe 2L
                        result.jobClub?.name shouldBe "취업동아리"
                        result.autonomousClub?.id shouldBe 3L
                        result.autonomousClub?.name shouldBe "자율동아리"

                        verify(exactly = 1) {
                            mockClubRepository.findAllById(listOf(1L, 2L, 3L))
                        }
                    }
                }

                context("존재하지 않는 전공 동아리 ID로 생성 요청할 때") {
                    val createRequest =
                        CreateStudentReqDto(
                            name = "학생",
                            sex = Sex.MAN,
                            email = "test@gsm.hs.kr",
                            grade = 1,
                            classNum = 1,
                            number = 1,
                            role = StudentRole.GENERAL_STUDENT,
                            dormitoryRoomNumber = 201,
                            majorClubId = 999L,
                        )

                    beforeEach {
                        every { mockStudentRepository.existsByEmail(createRequest.email) } returns false
                        every {
                            mockStudentRepository.existsByStudentNumber(
                                createRequest.grade!!,
                                createRequest.classNum!!,
                                createRequest.number!!,
                            )
                        } returns false
                        every { mockClubRepository.findAllById(listOf(999L)) } returns emptyList()
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                createStudentService.execute(createRequest)
                            }

                        exception.message shouldBe "전공 동아리를 찾을 수 없습니다."

                        verify(exactly = 1) { mockClubRepository.findAllById(listOf(999L)) }
                    }
                }

                context("존재하지 않는 취업 동아리 ID로 생성 요청할 때") {
                    val createRequest =
                        CreateStudentReqDto(
                            name = "학생",
                            sex = Sex.MAN,
                            email = "test@gsm.hs.kr",
                            grade = 1,
                            classNum = 1,
                            number = 2,
                            role = StudentRole.GENERAL_STUDENT,
                            dormitoryRoomNumber = 202,
                            jobClubId = 999L,
                        )

                    beforeEach {
                        every { mockStudentRepository.existsByEmail(createRequest.email) } returns false
                        every {
                            mockStudentRepository.existsByStudentNumber(
                                createRequest.grade!!,
                                createRequest.classNum!!,
                                createRequest.number!!,
                            )
                        } returns false
                        every { mockClubRepository.findAllById(listOf(999L)) } returns emptyList()
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                createStudentService.execute(createRequest)
                            }

                        exception.message shouldBe "취업 동아리를 찾을 수 없습니다."

                        verify(exactly = 1) { mockClubRepository.findAllById(listOf(999L)) }
                    }
                }

                context("존재하지 않는 자율 동아리 ID로 생성 요청할 때") {
                    val createRequest =
                        CreateStudentReqDto(
                            name = "학생",
                            sex = Sex.WOMAN,
                            email = "test@gsm.hs.kr",
                            grade = 1,
                            classNum = 1,
                            number = 3,
                            role = StudentRole.GENERAL_STUDENT,
                            dormitoryRoomNumber = 203,
                            autonomousClubId = 999L,
                        )

                    beforeEach {
                        every { mockStudentRepository.existsByEmail(createRequest.email) } returns false
                        every {
                            mockStudentRepository.existsByStudentNumber(
                                createRequest.grade!!,
                                createRequest.classNum!!,
                                createRequest.number!!,
                            )
                        } returns false
                        every { mockClubRepository.findAllById(listOf(999L)) } returns emptyList()
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                createStudentService.execute(createRequest)
                            }

                        exception.message shouldBe "자율 동아리를 찾을 수 없습니다."

                        verify(exactly = 1) { mockClubRepository.findAllById(listOf(999L)) }
                    }
                }

                context("GRADUATE role로 grade 정보와 함께 생성 요청할 때") {
                    val createRequest =
                        CreateStudentReqDto(
                            name = "졸업생",
                            sex = Sex.MAN,
                            email = "graduate@gsm.hs.kr",
                            grade = 3,
                            role = StudentRole.GRADUATE,
                            dormitoryRoomNumber = null,
                        )

                    beforeEach {
                        every { mockStudentRepository.existsByEmail(createRequest.email) } returns false
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                createStudentService.execute(createRequest)
                            }

                        exception.message shouldBe "졸업생은 학번, 기숙사, 동아리 정보를 가질 수 없습니다."

                        verify(exactly = 0) { mockStudentRepository.save(any()) }
                    }
                }

                context("GRADUATE role로 majorClubId와 함께 생성 요청할 때") {
                    val createRequest =
                        CreateStudentReqDto(
                            name = "졸업생",
                            sex = Sex.WOMAN,
                            email = "graduate2@gsm.hs.kr",
                            role = StudentRole.GRADUATE,
                            dormitoryRoomNumber = null,
                            majorClubId = 1L,
                        )

                    beforeEach {
                        every { mockStudentRepository.existsByEmail(createRequest.email) } returns false
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                createStudentService.execute(createRequest)
                            }

                        exception.message shouldBe "졸업생은 학번, 기숙사, 동아리 정보를 가질 수 없습니다."

                        verify(exactly = 0) { mockStudentRepository.save(any()) }
                    }
                }

                context("GRADUATE role로 제한 필드 없이 생성 요청할 때") {
                    val createRequest =
                        CreateStudentReqDto(
                            name = "졸업생",
                            sex = Sex.MAN,
                            email = "graduate3@gsm.hs.kr",
                            role = StudentRole.GRADUATE,
                            dormitoryRoomNumber = null,
                        )

                    val savedStudent =
                        StudentJpaEntity().apply {
                            id = 10L
                            name = createRequest.name
                            sex = createRequest.sex
                            email = createRequest.email
                            role = createRequest.role
                        }

                    beforeEach {
                        every { mockStudentRepository.existsByEmail(createRequest.email) } returns false
                        every { mockStudentRepository.save(any()) } returns savedStudent
                    }

                    it("졸업생이 정상적으로 생성되어야 한다") {
                        val result = createStudentService.execute(createRequest)

                        result.name shouldBe "졸업생"
                        result.role shouldBe StudentRole.GRADUATE
                        result.grade shouldBe null
                        result.classNum shouldBe null
                        result.number shouldBe null
                        result.major shouldBe null

                        verify(exactly = 1) { mockStudentRepository.save(any()) }
                    }
                }

                context("WITHDRAWN role로 dormitoryRoomNumber와 함께 생성 요청할 때") {
                    val createRequest =
                        CreateStudentReqDto(
                            name = "자퇴생",
                            sex = Sex.WOMAN,
                            email = "withdrawn@gsm.hs.kr",
                            role = StudentRole.WITHDRAWN,
                            dormitoryRoomNumber = 301,
                        )

                    beforeEach {
                        every { mockStudentRepository.existsByEmail(createRequest.email) } returns false
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                createStudentService.execute(createRequest)
                            }

                        exception.message shouldBe "자퇴생은 기숙사, 동아리 정보를 가질 수 없습니다."

                        verify(exactly = 0) { mockStudentRepository.save(any()) }
                    }
                }

                context("WITHDRAWN role로 majorClubId와 함께 생성 요청할 때") {
                    val createRequest =
                        CreateStudentReqDto(
                            name = "자퇴생",
                            sex = Sex.MAN,
                            email = "withdrawn2@gsm.hs.kr",
                            role = StudentRole.WITHDRAWN,
                            dormitoryRoomNumber = null,
                            majorClubId = 1L,
                        )

                    beforeEach {
                        every { mockStudentRepository.existsByEmail(createRequest.email) } returns false
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                createStudentService.execute(createRequest)
                            }

                        exception.message shouldBe "자퇴생은 기숙사, 동아리 정보를 가질 수 없습니다."

                        verify(exactly = 0) { mockStudentRepository.save(any()) }
                    }
                }

                context("WITHDRAWN role로 grade/classNum/number와 함께 생성 요청할 때") {
                    val createRequest =
                        CreateStudentReqDto(
                            name = "자퇴생",
                            sex = Sex.WOMAN,
                            email = "withdrawn3@gsm.hs.kr",
                            grade = 2,
                            classNum = 1,
                            number = 5,
                            role = StudentRole.WITHDRAWN,
                            dormitoryRoomNumber = null,
                        )

                    val savedStudent =
                        StudentJpaEntity().apply {
                            id = 11L
                            name = createRequest.name
                            sex = createRequest.sex
                            email = createRequest.email
                            role = createRequest.role
                            studentNumber = StudentNumber(createRequest.grade, createRequest.classNum, createRequest.number)
                        }

                    beforeEach {
                        every { mockStudentRepository.existsByEmail(createRequest.email) } returns false
                        every {
                            mockStudentRepository.existsByStudentNumber(
                                createRequest.grade!!,
                                createRequest.classNum!!,
                                createRequest.number!!,
                            )
                        } returns false
                        every { mockStudentRepository.save(any()) } returns savedStudent
                    }

                    it("학번 정보와 함께 자퇴생이 정상적으로 생성되어야 한다") {
                        val result = createStudentService.execute(createRequest)

                        result.name shouldBe "자퇴생"
                        result.role shouldBe StudentRole.WITHDRAWN
                        result.grade shouldBe 2
                        result.classNum shouldBe 1
                        result.number shouldBe 5
                        result.major shouldBe null
                        result.dormitoryFloor shouldBe null
                        result.majorClub shouldBe null

                        verify(exactly = 1) { mockStudentRepository.save(any()) }
                    }
                }

                context("WITHDRAWN role로 grade 없이 생성 요청할 때") {
                    val createRequest =
                        CreateStudentReqDto(
                            name = "자퇴생",
                            sex = Sex.MAN,
                            email = "withdrawn4@gsm.hs.kr",
                            role = StudentRole.WITHDRAWN,
                            dormitoryRoomNumber = null,
                        )

                    val savedStudent =
                        StudentJpaEntity().apply {
                            id = 12L
                            name = createRequest.name
                            sex = createRequest.sex
                            email = createRequest.email
                            role = createRequest.role
                        }

                    beforeEach {
                        every { mockStudentRepository.existsByEmail(createRequest.email) } returns false
                        every { mockStudentRepository.save(any()) } returns savedStudent
                    }

                    it("학번 없이 자퇴생이 정상적으로 생성되어야 한다") {
                        val result = createStudentService.execute(createRequest)

                        result.name shouldBe "자퇴생"
                        result.role shouldBe StudentRole.WITHDRAWN
                        result.grade shouldBe null
                        result.classNum shouldBe null
                        result.number shouldBe null

                        verify(exactly = 1) { mockStudentRepository.save(any()) }
                    }
                }

                context("GENERAL_STUDENT role로 학번 정보 없이 생성 요청할 때") {
                    val createRequest =
                        CreateStudentReqDto(
                            name = "학생",
                            sex = Sex.WOMAN,
                            email = "nograde@gsm.hs.kr",
                            role = StudentRole.GENERAL_STUDENT,
                            dormitoryRoomNumber = 201,
                        )

                    beforeEach {
                        every { mockStudentRepository.existsByEmail(createRequest.email) } returns false
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                createStudentService.execute(createRequest)
                            }

                        exception.message shouldBe "재학생은 학번 정보(학년, 반, 번호)가 필수입니다."

                        verify(exactly = 0) { mockStudentRepository.save(any()) }
                    }
                }
            }
        }
    })
