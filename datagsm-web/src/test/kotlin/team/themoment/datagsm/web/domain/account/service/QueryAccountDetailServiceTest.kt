package team.themoment.datagsm.web.domain.account.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import org.springframework.http.HttpStatus
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.common.domain.account.repository.AccountJpaRepository
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.common.domain.student.entity.StudentNumber
import team.themoment.datagsm.common.domain.student.entity.constant.Major
import team.themoment.datagsm.common.domain.student.entity.constant.Sex
import team.themoment.datagsm.common.domain.student.entity.constant.StudentRole
import team.themoment.datagsm.web.domain.account.service.impl.QueryAccountDetailServiceImpl
import team.themoment.sdk.exception.ExpectedException
import java.util.Optional

class QueryAccountDetailServiceTest :
    DescribeSpec({

        val mockAccountRepository = mockk<AccountJpaRepository>()

        val queryAccountDetailService = QueryAccountDetailServiceImpl(mockAccountRepository)

        afterEach {
            clearAllMocks()
        }

        describe("QueryAccountDetailService 클래스의") {
            describe("execute 메서드는") {

                context("학생과 연결된 계정을 조회할 때") {
                    val linkedStudent =
                        StudentJpaEntity().apply {
                            id = 10L
                            name = "홍길동"
                            sex = Sex.MAN
                            email = "hong@gsm.hs.kr"
                            studentNumber = StudentNumber(1, 1, 1)
                            major = Major.SW_DEVELOPMENT
                            role = StudentRole.GENERAL_STUDENT
                        }
                    val studentAccount =
                        AccountJpaEntity().apply {
                            id = 1L
                            email = "hong@gsm.hs.kr"
                            password = "encoded"
                            role = AccountRole.USER
                            student = linkedStudent
                        }

                    beforeEach {
                        every { mockAccountRepository.findById(1L) } returns Optional.of(studentAccount)
                    }

                    it("연결된 학생 정보가 포함되어야 한다") {
                        val result = queryAccountDetailService.execute(1L)

                        result.id shouldBe 1L
                        result.isStudent shouldBe true
                        result.student?.id shouldBe 10L
                        result.student?.name shouldBe "홍길동"
                    }
                }

                context("학생과 연결되지 않은 계정을 조회할 때") {
                    val teacherAccount =
                        AccountJpaEntity().apply {
                            id = 2L
                            email = "teacher@gsm.hs.kr"
                            password = "encoded"
                            role = AccountRole.USER
                            student = null
                        }

                    beforeEach {
                        every { mockAccountRepository.findById(2L) } returns Optional.of(teacherAccount)
                    }

                    it("학생 정보가 null이어야 한다") {
                        val result = queryAccountDetailService.execute(2L)

                        result.id shouldBe 2L
                        result.isStudent shouldBe false
                        result.student shouldBe null
                    }
                }

                context("존재하지 않는 계정을 조회할 때") {
                    beforeEach {
                        every { mockAccountRepository.findById(999L) } returns Optional.empty()
                    }

                    it("NOT_FOUND ExpectedException이 발생해야 한다") {
                        val ex =
                            shouldThrow<ExpectedException> {
                                queryAccountDetailService.execute(999L)
                            }
                        ex.message shouldBe "계정을 찾을 수 없습니다."
                        ex.statusCode shouldBe HttpStatus.NOT_FOUND
                    }
                }
            }
        }
    })
