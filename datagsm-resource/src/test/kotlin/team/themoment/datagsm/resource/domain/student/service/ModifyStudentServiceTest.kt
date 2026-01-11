package team.themoment.datagsm.resource.domain.student.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import team.themoment.datagsm.common.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.common.domain.student.dto.request.UpdateStudentReqDto
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.common.domain.student.entity.StudentNumber
import team.themoment.datagsm.common.domain.student.entity.constant.Major
import team.themoment.datagsm.common.domain.student.entity.constant.Sex
import team.themoment.datagsm.common.domain.student.entity.constant.StudentRole
import team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.resource.domain.student.service.impl.ModifyStudentServiceImpl
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
                context("존재하지 않는 학생을 수정할 때") {
                    val req =
                        UpdateStudentReqDto(
                            name = "수정된이름",
                            sex = Sex.MAN,
                            email = "updated@gsm.hs.kr",
                            grade = 2,
                            classNum = 1,
                            number = 5,
                            role = StudentRole.GENERAL_STUDENT,
                            isLeaveSchool = false,
                        )

                    beforeEach {
                        every { mockStudentRepository.findById(999L) } returns Optional.empty()
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val ex =
                            shouldThrow<ExpectedException> {
                                modifyStudentService.execute(999L, req)
                            }
                        ex.message shouldBe "학생을 찾을 수 없습니다. studentId: 999"

                        verify(exactly = 1) { mockStudentRepository.findById(999L) }
                    }
                }

                context("정상적으로 학생 정보를 수정할 때") {
                    val studentId = 100L
                    val req =
                        UpdateStudentReqDto(
                            name = "수정된이름",
                            sex = Sex.WOMAN,
                            email = "updated@gsm.hs.kr",
                            grade = 3,
                            classNum = 2,
                            number = 15,
                            role = StudentRole.GENERAL_STUDENT,
                            isLeaveSchool = false,
                        )
                    lateinit var existingStudent: StudentJpaEntity

                    beforeEach {
                        existingStudent =
                            StudentJpaEntity().apply {
                                this.id = studentId
                                this.name = "기존이름"
                                this.sex = Sex.MAN
                                this.email = "old@gsm.hs.kr"
                                this.studentNumber = StudentNumber(2, 1, 10)
                                this.major = Major.AI
                                this.role = StudentRole.GENERAL_STUDENT
                            }

                        every { mockStudentRepository.findById(studentId) } returns Optional.of(existingStudent)
                        every {
                            mockStudentRepository.existsByStudentEmailAndNotId(
                                req.email,
                                studentId,
                            )
                        } returns false
                        every {
                            mockStudentRepository.existsByStudentNumberAndNotId(
                                req.grade,
                                req.classNum,
                                req.number,
                                studentId,
                            )
                        } returns false
                    }

                    it("수정된 학생 정보를 반환해야 한다") {
                        val res = modifyStudentService.execute(studentId, req)

                        res.id shouldBe studentId
                        res.name shouldBe "수정된이름"
                        res.email shouldBe "updated@gsm.hs.kr"
                        res.grade shouldBe 3
                        res.classNum shouldBe 2
                        res.number shouldBe 15
                        res.major shouldBe Major.AI

                        verify(exactly = 1) { mockStudentRepository.findById(studentId) }
                        verify(exactly = 1) {
                            mockStudentRepository.existsByStudentEmailAndNotId(
                                req.email,
                                studentId,
                            )
                        }
                        verify(exactly = 1) {
                            mockStudentRepository.existsByStudentNumberAndNotId(
                                req.grade,
                                req.classNum,
                                req.number,
                                studentId,
                            )
                        }
                    }
                }
            }
        }
    })
