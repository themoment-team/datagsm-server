package team.themoment.datagsm.web.domain.student.service

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.common.domain.student.entity.StudentNumber
import team.themoment.datagsm.common.domain.student.entity.constant.Major
import team.themoment.datagsm.common.domain.student.entity.constant.Sex
import team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.web.domain.student.service.impl.QueryParticipantCandidateServiceImpl

class QueryParticipantCandidateServiceTest :
    DescribeSpec({

        val mockStudentRepository = mockk<StudentJpaRepository>()
        val queryParticipantCandidateService = QueryParticipantCandidateServiceImpl(mockStudentRepository)

        afterEach {
            clearAllMocks()
        }

        describe("QueryParticipantCandidateService 클래스의") {

            describe("execute 메서드는") {

                context("재학생이 존재할 때") {
                    val student =
                        StudentJpaEntity().apply {
                            id = 1L
                            name = "홍길동"
                            email = "s24080@gsm.hs.kr"
                            sex = Sex.MAN
                            major = Major.SW_DEVELOPMENT
                            studentNumber = StudentNumber(2, 1, 5)
                        }

                    beforeEach {
                        every { mockStudentRepository.findAllEnrolledStudents() } returns listOf(student)
                    }

                    it("선택에 필요한 ID·이름·학번·학과만 반환되어야 한다") {
                        val result = queryParticipantCandidateService.execute()

                        result.students.size shouldBe 1
                        result.students[0].id shouldBe 1L
                        result.students[0].name shouldBe "홍길동"
                        result.students[0].studentNumber shouldBe 2105
                        result.students[0].major shouldBe Major.SW_DEVELOPMENT
                    }
                }

                context("재학생이 없을 때") {
                    beforeEach {
                        every { mockStudentRepository.findAllEnrolledStudents() } returns emptyList()
                    }

                    it("빈 목록이 반환되어야 한다") {
                        queryParticipantCandidateService.execute().students.shouldBeEmpty()
                    }
                }
            }
        }
    })
