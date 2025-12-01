package team.themoment.datagsm.domain.student.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import team.themoment.datagsm.domain.student.dto.request.UpdateStudentReqDto
import team.themoment.datagsm.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.domain.student.entity.constant.DormitoryRoomNumber
import team.themoment.datagsm.domain.student.entity.constant.Major
import team.themoment.datagsm.domain.student.entity.constant.Sex
import team.themoment.datagsm.domain.student.entity.constant.StudentNumber
import team.themoment.datagsm.domain.student.entity.constant.StudentRole
import team.themoment.datagsm.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.domain.student.service.impl.ModifyStudentServiceImpl
import team.themoment.datagsm.global.exception.error.ExpectedException
import java.util.Optional

class ModifyStudentServiceTest :
    DescribeSpec({

        lateinit var mockStudentRepository: StudentJpaRepository
        lateinit var modifyStudentService: ModifyStudentService

        beforeEach {
            mockStudentRepository = mockk<StudentJpaRepository>()
            modifyStudentService = ModifyStudentServiceImpl(mockStudentRepository)
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
                            isLeaveSchool = false
                        }
                }

                context("존재하는 학생의 이름을 수정할 때") {
                    val updateRequest =
                        UpdateStudentReqDto(
                            name = "수정된이름",
                            sex = null,
                            email = null,
                            grade = null,
                            classNum = null,
                            number = null,
                            role = null,
                            dormitoryRoomNumber = null,
                        )

                    beforeEach {
                        every { mockStudentRepository.findById(studentId) } returns Optional.of(existingStudent)
                    }

                    it("학생 이름이 성공적으로 업데이트되어야 한다") {
                        existingStudent.name = updateRequest.name!!
                        val result = modifyStudentService.execute(studentId, updateRequest)

                        result.studentId shouldBe studentId
                        result.name shouldBe "수정된이름"

                        verify(exactly = 1) { mockStudentRepository.findById(studentId) }
                    }
                }

                context("학생의 학년만 변경할 때 (반은 변경하지 않음)") {
                    val updateRequest =
                        UpdateStudentReqDto(
                            name = null,
                            sex = null,
                            email = null,
                            grade = 3,
                            classNum = null,
                            number = null,
                            role = null,
                            dormitoryRoomNumber = null,
                        )

                    beforeEach {
                        every { mockStudentRepository.findById(studentId) } returns Optional.of(existingStudent)
                        every {
                            mockStudentRepository.existsByStudentNumberAndNotId(
                                3,
                                1,
                                5,
                                studentId,
                            )
                        } returns false
                    }

                    it("학년만 변경하고 반이 변경되지 않으면 전공은 유지되어야 한다") {
                        val result = modifyStudentService.execute(studentId, updateRequest)

                        result.grade shouldBe 3
                        result.classNum shouldBe 1
                        result.major shouldBe Major.SW_DEVELOPMENT
                        result.studentNumber shouldBe 3105

                        verify(exactly = 1) {
                            mockStudentRepository.existsByStudentNumberAndNotId(
                                3,
                                1,
                                5,
                                studentId,
                            )
                        }
                    }
                }

                context("학생의 반만 변경할 때") {
                    val updateRequest =
                        UpdateStudentReqDto(
                            name = null,
                            sex = null,
                            email = null,
                            grade = null,
                            classNum = 3,
                            number = null,
                            role = null,
                            dormitoryRoomNumber = null,
                        )

                    beforeEach {
                        every { mockStudentRepository.findById(studentId) } returns Optional.of(existingStudent)
                        every {
                            mockStudentRepository.existsByStudentNumberAndNotId(
                                2,
                                3,
                                5,
                                studentId,
                            )
                        } returns false
                    }

                    it("반 변경 시 새로운 반에 따라 전공이 변경되어야 한다") {
                        val result = modifyStudentService.execute(studentId, updateRequest)

                        result.grade shouldBe 2
                        result.classNum shouldBe 3
                        result.major shouldBe Major.SMART_IOT
                        result.studentNumber shouldBe 2305
                    }
                }

                context("학생의 학년과 반을 동시에 변경할 때") {
                    val updateRequest =
                        UpdateStudentReqDto(
                            name = null,
                            sex = null,
                            email = null,
                            grade = 1,
                            classNum = 4,
                            number = null,
                            role = null,
                            dormitoryRoomNumber = null,
                        )

                    beforeEach {
                        every { mockStudentRepository.findById(studentId) } returns Optional.of(existingStudent)
                        every {
                            mockStudentRepository.existsByStudentNumberAndNotId(
                                1,
                                4,
                                5,
                                studentId,
                            )
                        } returns false
                    }

                    it("반 변경 시 새로운 반에 따라 전공이 변경되어야 한다") {
                        val result = modifyStudentService.execute(studentId, updateRequest)

                        result.grade shouldBe 1
                        result.classNum shouldBe 4
                        result.major shouldBe Major.AI
                        result.studentNumber shouldBe 1405
                    }
                }

                context("이미 존재하는 이메일로 변경을 시도할 때") {
                    val updateRequest =
                        UpdateStudentReqDto(
                            name = null,
                            sex = null,
                            email = "duplicate@gsm.hs.kr",
                            grade = null,
                            classNum = null,
                            number = null,
                            role = null,
                            dormitoryRoomNumber = null,
                        )

                    beforeEach {
                        every { mockStudentRepository.findById(studentId) } returns Optional.of(existingStudent)
                        every {
                            mockStudentRepository.existsByStudentEmailAndNotId(
                                updateRequest.email!!,
                                studentId,
                            )
                        } returns true
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                modifyStudentService.execute(studentId, updateRequest)
                            }

                        exception.message shouldBe "이미 존재하는 이메일입니다: ${updateRequest.email}"

                        verify(exactly = 1) { mockStudentRepository.findById(studentId) }
                        verify(exactly = 1) {
                            mockStudentRepository.existsByStudentEmailAndNotId(
                                updateRequest.email!!,
                                studentId,
                            )
                        }
                        verify(exactly = 0) { mockStudentRepository.save(any()) }
                    }
                }

                context("이미 존재하는 학번으로 변경을 시도할 때") {
                    val updateRequest =
                        UpdateStudentReqDto(
                            name = null,
                            sex = null,
                            email = null,
                            grade = 2,
                            classNum = 2,
                            number = 10,
                            role = null,
                            dormitoryRoomNumber = null,
                        )

                    beforeEach {
                        every { mockStudentRepository.findById(studentId) } returns Optional.of(existingStudent)
                        every {
                            mockStudentRepository.existsByStudentNumberAndNotId(
                                2,
                                2,
                                10,
                                studentId,
                            )
                        } returns true
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                modifyStudentService.execute(studentId, updateRequest)
                            }

                        exception.message shouldBe "이미 존재하는 학번입니다: 2학년 2반 10번"

                        verify(exactly = 1) {
                            mockStudentRepository.existsByStudentNumberAndNotId(
                                2,
                                2,
                                10,
                                studentId,
                            )
                        }
                        verify(exactly = 0) { mockStudentRepository.save(any()) }
                    }
                }

                context("존재하지 않는 학생 ID로 수정을 시도할 때") {
                    val updateRequest =
                        UpdateStudentReqDto(
                            name = "수정이름",
                            sex = null,
                            email = null,
                            grade = null,
                            classNum = null,
                            number = null,
                            role = null,
                            dormitoryRoomNumber = null,
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

                        verify(exactly = 1) { mockStudentRepository.findById(studentId) }
                        verify(exactly = 0) { mockStudentRepository.save(any()) }
                    }
                }

                context("유효하지 않은 학급으로 반을 변경할 때") {
                    val updateRequest =
                        UpdateStudentReqDto(
                            name = null,
                            sex = null,
                            email = null,
                            grade = null,
                            classNum = 5,
                            number = null,
                            role = null,
                            dormitoryRoomNumber = null,
                        )

                    beforeEach {
                        every { mockStudentRepository.findById(studentId) } returns Optional.of(existingStudent)
                        every {
                            mockStudentRepository.existsByStudentNumberAndNotId(
                                2,
                                5,
                                5,
                                studentId,
                            )
                        } returns false
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                modifyStudentService.execute(studentId, updateRequest)
                            }

                        exception.message shouldBe "유효하지 않은 학급입니다: 5"

                        verify(exactly = 1) { mockStudentRepository.findById(studentId) }
                        verify(exactly = 1) {
                            mockStudentRepository.existsByStudentNumberAndNotId(
                                2,
                                5,
                                5,
                                studentId,
                            )
                        }
                        verify(exactly = 0) { mockStudentRepository.save(any()) }
                    }
                }

                context("기숙사 방 번호를 변경할 때") {
                    val updateRequest =
                        UpdateStudentReqDto(
                            name = null,
                            sex = null,
                            email = null,
                            grade = null,
                            classNum = null,
                            number = null,
                            role = null,
                            dormitoryRoomNumber = 305,
                        )

                    beforeEach {
                        every { mockStudentRepository.findById(studentId) } returns Optional.of(existingStudent)
                    }

                    it("기숙사 방 번호가 성공적으로 변경되어야 한다") {
                        val result = modifyStudentService.execute(studentId, updateRequest)

                        result.dormitoryRoom shouldBe 305
                        result.dormitoryFloor shouldBe 3

                        verify(exactly = 1) { mockStudentRepository.findById(studentId) }
                    }
                }

                context("학생의 성별과 역할을 동시에 변경할 때") {
                    val updateRequest =
                        UpdateStudentReqDto(
                            name = null,
                            sex = Sex.WOMAN,
                            email = null,
                            grade = null,
                            classNum = null,
                            number = null,
                            role = StudentRole.STUDENT_COUNCIL,
                            dormitoryRoomNumber = null,
                        )

                    beforeEach {
                        every { mockStudentRepository.findById(studentId) } returns Optional.of(existingStudent)
                    }

                    it("성별과 역할이 성공적으로 변경되어야 한다") {
                        val result = modifyStudentService.execute(studentId, updateRequest)

                        result.sex shouldBe Sex.WOMAN
                        result.role shouldBe StudentRole.STUDENT_COUNCIL

                        verify(exactly = 1) { mockStudentRepository.findById(studentId) }
                    }
                }

                context("모든 필드를 한번에 수정할 때") {
                    val updateRequest =
                        UpdateStudentReqDto(
                            name = "완전수정학생",
                            sex = Sex.WOMAN,
                            email = "updated@gsm.hs.kr",
                            grade = 4,
                            classNum = 2,
                            number = 8,
                            role = StudentRole.DORMITORY_MANAGER,
                            dormitoryRoomNumber = 418,
                        )

                    beforeEach {
                        every { mockStudentRepository.findById(studentId) } returns Optional.of(existingStudent)
                        every {
                            mockStudentRepository.existsByStudentEmailAndNotId(
                                updateRequest.email!!,
                                studentId,
                            )
                        } returns false
                        every {
                            mockStudentRepository.existsByStudentNumberAndNotId(
                                4,
                                2,
                                8,
                                studentId,
                            )
                        } returns false
                    }

                    it("모든 필드가 성공적으로 업데이트되어야 한다") {
                        val result = modifyStudentService.execute(studentId, updateRequest)

                        result.name shouldBe "완전수정학생"
                        result.sex shouldBe Sex.WOMAN
                        result.email shouldBe "updated@gsm.hs.kr"
                        result.grade shouldBe 4
                        result.classNum shouldBe 2
                        result.number shouldBe 8
                        result.studentNumber shouldBe 4208
                        result.major shouldBe Major.SW_DEVELOPMENT
                        result.role shouldBe StudentRole.DORMITORY_MANAGER
                        result.dormitoryRoom shouldBe 418
                        result.dormitoryFloor shouldBe 4

                        verify(exactly = 1) { mockStudentRepository.findById(studentId) }
                        verify(exactly = 1) {
                            mockStudentRepository.existsByStudentEmailAndNotId(
                                updateRequest.email!!,
                                studentId,
                            )
                        }
                        verify(exactly = 1) {
                            mockStudentRepository.existsByStudentNumberAndNotId(
                                4,
                                2,
                                8,
                                studentId,
                            )
                        }
                    }
                }
            }
        }
    })
