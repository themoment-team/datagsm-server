package team.themoment.datagsm.web.domain.student.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import team.themoment.datagsm.common.domain.club.entity.ClubJpaEntity
import team.themoment.datagsm.common.domain.club.entity.constant.ClubType
import team.themoment.datagsm.common.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.common.domain.student.dto.request.UpdateStudentReqDto
import team.themoment.datagsm.common.domain.student.entity.DormitoryRoomNumber
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.common.domain.student.entity.StudentNumber
import team.themoment.datagsm.common.domain.student.entity.constant.Major
import team.themoment.datagsm.common.domain.student.entity.constant.Sex
import team.themoment.datagsm.common.domain.student.entity.constant.StudentRole
import team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.web.domain.student.service.impl.ModifyStudentServiceImpl
import team.themoment.sdk.exception.ExpectedException
import java.util.Optional

class ModifyStudentServiceTest :
    DescribeSpec({

        lateinit var mockStudentRepository: StudentJpaRepository
        lateinit var mockClubRepository: ClubJpaRepository
        lateinit var modifyStudentService: ModifyStudentService

        beforeEach {
            mockStudentRepository = mockk<StudentJpaRepository>()
            mockClubRepository = mockk<ClubJpaRepository>()
            modifyStudentService = ModifyStudentServiceImpl(mockStudentRepository, mockClubRepository)
        }

        describe("ModifyStudentService 클래스의") {
            describe("execute 메서드는") {

                val studentId = 1L
                lateinit var existingStudent: StudentJpaEntity

                beforeEach {
                    existingStudent =
                        StudentJpaEntity().apply {
                            this.id = studentId
                            name = "기존학생"
                            sex = Sex.MAN
                            email = "existing@gsm.hs.kr"
                            studentNumber = StudentNumber(2, 1, 5)
                            major = Major.SW_DEVELOPMENT
                            role = StudentRole.GENERAL_STUDENT
                            dormitoryRoomNumber = DormitoryRoomNumber(201)
                        }
                }

                context("학생 정보를 전체 교체할 때") {
                    val updateRequest =
                        UpdateStudentReqDto(
                            name = "수정된이름",
                            sex = Sex.WOMAN,
                            email = "updated@gsm.hs.kr",
                            grade = 2,
                            classNum = 1,
                            number = 5,
                            role = StudentRole.GENERAL_STUDENT,
                            dormitoryRoomNumber = 201,
                        )

                    beforeEach {
                        every { mockStudentRepository.findById(studentId) } returns Optional.of(existingStudent)
                        every { mockStudentRepository.existsByStudentEmailAndNotId("updated@gsm.hs.kr", studentId) } returns false
                        every { mockStudentRepository.existsByStudentNumberAndNotId(2, 1, 5, studentId) } returns false
                    }

                    it("학생 정보가 성공적으로 전체 교체되어야 한다") {
                        val result = modifyStudentService.execute(studentId, updateRequest)

                        result.id shouldBe studentId
                        result.name shouldBe "수정된이름"
                        result.sex shouldBe Sex.WOMAN
                        result.email shouldBe "updated@gsm.hs.kr"

                        verify(exactly = 1) { mockStudentRepository.findById(studentId) }
                    }
                }

                context("반 변경 시 전공도 함께 변경되는지 검증") {
                    val updateRequest =
                        UpdateStudentReqDto(
                            name = "학생",
                            sex = Sex.MAN,
                            email = "existing@gsm.hs.kr",
                            grade = 1,
                            classNum = 4,
                            number = 5,
                            role = StudentRole.GENERAL_STUDENT,
                            dormitoryRoomNumber = 201,
                        )

                    beforeEach {
                        every { mockStudentRepository.findById(studentId) } returns Optional.of(existingStudent)
                        every { mockStudentRepository.existsByStudentEmailAndNotId("existing@gsm.hs.kr", studentId) } returns false
                        every { mockStudentRepository.existsByStudentNumberAndNotId(1, 4, 5, studentId) } returns false
                    }

                    it("4반으로 변경 시 전공이 AI로 변경되어야 한다") {
                        val result = modifyStudentService.execute(studentId, updateRequest)

                        result.classNum shouldBe 4
                        result.major shouldBe Major.AI
                    }
                }

                context("이미 존재하는 이메일로 변경을 시도할 때") {
                    val updateRequest =
                        UpdateStudentReqDto(
                            name = "학생",
                            sex = Sex.MAN,
                            email = "duplicate@gsm.hs.kr",
                            grade = 2,
                            classNum = 1,
                            number = 5,
                            role = StudentRole.GENERAL_STUDENT,
                            dormitoryRoomNumber = 201,
                        )

                    beforeEach {
                        every { mockStudentRepository.findById(studentId) } returns Optional.of(existingStudent)
                        every { mockStudentRepository.existsByStudentEmailAndNotId("duplicate@gsm.hs.kr", studentId) } returns true
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                modifyStudentService.execute(studentId, updateRequest)
                            }

                        exception.message shouldBe "이미 존재하는 이메일입니다: duplicate@gsm.hs.kr"
                    }
                }

                context("이미 존재하는 학번으로 변경을 시도할 때") {
                    val updateRequest =
                        UpdateStudentReqDto(
                            name = "학생",
                            sex = Sex.MAN,
                            email = "existing@gsm.hs.kr",
                            grade = 2,
                            classNum = 2,
                            number = 10,
                            role = StudentRole.GENERAL_STUDENT,
                            dormitoryRoomNumber = 201,
                        )

                    beforeEach {
                        every { mockStudentRepository.findById(studentId) } returns Optional.of(existingStudent)
                        every { mockStudentRepository.existsByStudentEmailAndNotId("existing@gsm.hs.kr", studentId) } returns false
                        every { mockStudentRepository.existsByStudentNumberAndNotId(2, 2, 10, studentId) } returns true
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                modifyStudentService.execute(studentId, updateRequest)
                            }

                        exception.message shouldBe "이미 존재하는 학번입니다: 2학년 2반 10번"
                    }
                }

                context("존재하지 않는 학생 ID로 수정을 시도할 때") {
                    val updateRequest =
                        UpdateStudentReqDto(
                            name = "학생",
                            sex = Sex.MAN,
                            email = "test@gsm.hs.kr",
                            grade = 1,
                            classNum = 1,
                            number = 1,
                            role = StudentRole.GENERAL_STUDENT,
                            dormitoryRoomNumber = 201,
                        )

                    beforeEach {
                        every { mockStudentRepository.findById(studentId) } returns Optional.empty()
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                modifyStudentService.execute(studentId, updateRequest)
                            }

                        exception.message shouldBe "학생을 찾을 수 없습니다. studentId: $studentId"
                    }
                }

                context("유효하지 않은 학급으로 변경할 때") {
                    val updateRequest =
                        UpdateStudentReqDto(
                            name = "학생",
                            sex = Sex.MAN,
                            email = "existing@gsm.hs.kr",
                            grade = 1,
                            classNum = 5,
                            number = 1,
                            role = StudentRole.GENERAL_STUDENT,
                            dormitoryRoomNumber = 201,
                        )

                    beforeEach {
                        every { mockStudentRepository.findById(studentId) } returns Optional.of(existingStudent)
                        every { mockStudentRepository.existsByStudentEmailAndNotId("existing@gsm.hs.kr", studentId) } returns false
                        every { mockStudentRepository.existsByStudentNumberAndNotId(1, 5, 1, studentId) } returns false
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                modifyStudentService.execute(studentId, updateRequest)
                            }

                        exception.message shouldBe "유효하지 않은 학급입니다: 5"
                    }
                }

                context("기숙사 정보를 null로 설정할 때") {
                    val updateRequest =
                        UpdateStudentReqDto(
                            name = "학생",
                            sex = Sex.MAN,
                            email = "existing@gsm.hs.kr",
                            grade = 2,
                            classNum = 1,
                            number = 5,
                            role = StudentRole.GENERAL_STUDENT,
                            dormitoryRoomNumber = null,
                        )

                    beforeEach {
                        every { mockStudentRepository.findById(studentId) } returns Optional.of(existingStudent)
                        every { mockStudentRepository.existsByStudentEmailAndNotId("existing@gsm.hs.kr", studentId) } returns false
                        every { mockStudentRepository.existsByStudentNumberAndNotId(2, 1, 5, studentId) } returns false
                    }

                    it("기숙사 정보가 null로 설정되어야 한다") {
                        val result = modifyStudentService.execute(studentId, updateRequest)

                        result.dormitoryRoom shouldBe null
                        result.dormitoryFloor shouldBe null
                    }
                }

                context("유효한 클럽 ID로 동아리를 변경할 때") {
                    val majorClub =
                        ClubJpaEntity().apply {
                            id = 10L
                            name = "새로운전공동아리"
                            type = ClubType.MAJOR_CLUB
                        }
                    val jobClub =
                        ClubJpaEntity().apply {
                            id = 20L
                            name = "새로운취업동아리"
                            type = ClubType.JOB_CLUB
                        }

                    val updateRequest =
                        UpdateStudentReqDto(
                            name = "학생",
                            sex = Sex.MAN,
                            email = "existing@gsm.hs.kr",
                            grade = 2,
                            classNum = 1,
                            number = 5,
                            role = StudentRole.GENERAL_STUDENT,
                            dormitoryRoomNumber = 201,
                            majorClubId = 10L,
                            jobClubId = 20L,
                        )

                    beforeEach {
                        every { mockStudentRepository.findById(studentId) } returns Optional.of(existingStudent)
                        every { mockStudentRepository.existsByStudentEmailAndNotId("existing@gsm.hs.kr", studentId) } returns false
                        every { mockStudentRepository.existsByStudentNumberAndNotId(2, 1, 5, studentId) } returns false
                        every { mockClubRepository.findAllById(listOf(10L, 20L)) } returns listOf(majorClub, jobClub)
                    }

                    it("클럽 정보가 성공적으로 변경되어야 한다") {
                        val result = modifyStudentService.execute(studentId, updateRequest)

                        result.majorClub?.id shouldBe 10L
                        result.majorClub?.name shouldBe "새로운전공동아리"
                        result.jobClub?.id shouldBe 20L
                        result.jobClub?.name shouldBe "새로운취업동아리"

                        verify(exactly = 1) { mockClubRepository.findAllById(listOf(10L, 20L)) }
                    }
                }

                context("동아리를 null로 설정하여 제거할 때") {
                    val updateRequest =
                        UpdateStudentReqDto(
                            name = "학생",
                            sex = Sex.MAN,
                            email = "existing@gsm.hs.kr",
                            grade = 2,
                            classNum = 1,
                            number = 5,
                            role = StudentRole.GENERAL_STUDENT,
                            dormitoryRoomNumber = 201,
                            majorClubId = null,
                            jobClubId = null,
                            autonomousClubId = null,
                        )

                    beforeEach {
                        existingStudent.majorClub =
                            ClubJpaEntity().apply {
                                id = 1L
                                name = "기존동아리"
                                type = ClubType.MAJOR_CLUB
                            }
                        every { mockStudentRepository.findById(studentId) } returns Optional.of(existingStudent)
                        every { mockStudentRepository.existsByStudentEmailAndNotId("existing@gsm.hs.kr", studentId) } returns false
                        every { mockStudentRepository.existsByStudentNumberAndNotId(2, 1, 5, studentId) } returns false
                    }

                    it("동아리 정보가 null로 제거되어야 한다") {
                        val result = modifyStudentService.execute(studentId, updateRequest)

                        result.majorClub shouldBe null
                        result.jobClub shouldBe null
                        result.autonomousClub shouldBe null
                    }
                }

                context("존재하지 않는 전공 동아리 ID로 변경 시도할 때") {
                    val updateRequest =
                        UpdateStudentReqDto(
                            name = "학생",
                            sex = Sex.MAN,
                            email = "existing@gsm.hs.kr",
                            grade = 2,
                            classNum = 1,
                            number = 5,
                            role = StudentRole.GENERAL_STUDENT,
                            dormitoryRoomNumber = 201,
                            majorClubId = 999L,
                        )

                    beforeEach {
                        every { mockStudentRepository.findById(studentId) } returns Optional.of(existingStudent)
                        every { mockStudentRepository.existsByStudentEmailAndNotId("existing@gsm.hs.kr", studentId) } returns false
                        every { mockStudentRepository.existsByStudentNumberAndNotId(2, 1, 5, studentId) } returns false
                        every { mockClubRepository.findAllById(listOf(999L)) } returns emptyList()
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                modifyStudentService.execute(studentId, updateRequest)
                            }

                        exception.message shouldBe "전공 동아리를 찾을 수 없습니다."

                        verify(exactly = 1) { mockClubRepository.findAllById(listOf(999L)) }
                    }
                }

                context("role을 GRADUATE로 변경 시도할 때") {
                    val updateRequest =
                        UpdateStudentReqDto(
                            name = "학생",
                            sex = Sex.MAN,
                            email = "existing@gsm.hs.kr",
                            grade = 2,
                            classNum = 1,
                            number = 5,
                            role = StudentRole.GRADUATE,
                            dormitoryRoomNumber = 201,
                        )

                    beforeEach {
                        every { mockStudentRepository.findById(studentId) } returns Optional.of(existingStudent)
                        every { mockStudentRepository.existsByStudentEmailAndNotId("existing@gsm.hs.kr", studentId) } returns false
                        every { mockStudentRepository.existsByStudentNumberAndNotId(2, 1, 5, studentId) } returns false
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                modifyStudentService.execute(studentId, updateRequest)
                            }

                        exception.message shouldBe "졸업생이나 자퇴생으로의 role 변경은 전용 API를 사용해야 합니다."
                    }
                }

                context("role을 LEAVE_SCHOOL로 변경 시도할 때") {
                    val updateRequest =
                        UpdateStudentReqDto(
                            name = "학생",
                            sex = Sex.MAN,
                            email = "existing@gsm.hs.kr",
                            grade = 2,
                            classNum = 1,
                            number = 5,
                            role = StudentRole.LEAVE_SCHOOL,
                            dormitoryRoomNumber = 201,
                        )

                    beforeEach {
                        every { mockStudentRepository.findById(studentId) } returns Optional.of(existingStudent)
                        every { mockStudentRepository.existsByStudentEmailAndNotId("existing@gsm.hs.kr", studentId) } returns false
                        every { mockStudentRepository.existsByStudentNumberAndNotId(2, 1, 5, studentId) } returns false
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                modifyStudentService.execute(studentId, updateRequest)
                            }

                        exception.message shouldBe "졸업생이나 자퇴생으로의 role 변경은 전용 API를 사용해야 합니다."
                    }
                }

                context("role을 STUDENT_COUNCIL로 변경할 때") {
                    val updateRequest =
                        UpdateStudentReqDto(
                            name = "학생",
                            sex = Sex.MAN,
                            email = "existing@gsm.hs.kr",
                            grade = 2,
                            classNum = 1,
                            number = 5,
                            role = StudentRole.STUDENT_COUNCIL,
                            dormitoryRoomNumber = 201,
                        )

                    beforeEach {
                        every { mockStudentRepository.findById(studentId) } returns Optional.of(existingStudent)
                        every { mockStudentRepository.existsByStudentEmailAndNotId("existing@gsm.hs.kr", studentId) } returns false
                        every { mockStudentRepository.existsByStudentNumberAndNotId(2, 1, 5, studentId) } returns false
                    }

                    it("role이 성공적으로 변경되어야 한다") {
                        val result = modifyStudentService.execute(studentId, updateRequest)

                        result.role shouldBe StudentRole.STUDENT_COUNCIL
                    }
                }
            }
        }
    })
