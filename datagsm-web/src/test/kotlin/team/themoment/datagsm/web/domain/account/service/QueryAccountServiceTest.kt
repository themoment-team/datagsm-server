package team.themoment.datagsm.web.domain.account.service

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import team.themoment.datagsm.common.domain.account.dto.request.QueryAccountReqDto
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.common.domain.account.repository.AccountJpaRepository
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.common.domain.student.entity.StudentNumber
import team.themoment.datagsm.common.domain.student.entity.constant.Major
import team.themoment.datagsm.common.domain.student.entity.constant.Sex
import team.themoment.datagsm.common.domain.student.entity.constant.StudentRole
import team.themoment.datagsm.web.domain.account.service.impl.QueryAccountServiceImpl

class QueryAccountServiceTest :
    DescribeSpec({

        val mockAccountRepository = mockk<AccountJpaRepository>()

        val queryAccountService = QueryAccountServiceImpl(mockAccountRepository)

        afterEach {
            clearAllMocks()
        }

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

        val teacherAccount =
            AccountJpaEntity().apply {
                id = 2L
                email = "teacher@gsm.hs.kr"
                password = "encoded"
                role = AccountRole.USER
                student = null
            }

        describe("QueryAccountService 클래스의") {
            describe("execute 메서드는") {

                context("학생과 연결된 계정을 조회할 때") {
                    beforeEach {
                        every {
                            mockAccountRepository.searchAccountsWithPaging(
                                email = null,
                                role = null,
                                isStudent = true,
                                pageable = PageRequest.of(0, 300),
                                sortBy = any(),
                                sortDirection = any(),
                            )
                        } returns PageImpl(listOf(studentAccount), PageRequest.of(0, 300), 1L)
                    }

                    it("연결된 학생 정보가 포함되어야 한다") {
                        val result = queryAccountService.execute(QueryAccountReqDto(isStudent = true))

                        result.totalElements shouldBe 1L
                        val account = result.accounts[0]
                        account.id shouldBe 1L
                        account.isStudent shouldBe true
                        account.student?.id shouldBe 10L
                        account.student?.name shouldBe "홍길동"
                    }
                }

                context("학생과 연결되지 않은 계정을 조회할 때") {
                    beforeEach {
                        every {
                            mockAccountRepository.searchAccountsWithPaging(
                                email = null,
                                role = null,
                                isStudent = false,
                                pageable = PageRequest.of(0, 300),
                                sortBy = any(),
                                sortDirection = any(),
                            )
                        } returns PageImpl(listOf(teacherAccount), PageRequest.of(0, 300), 1L)
                    }

                    it("학생 정보가 null이어야 한다") {
                        val result = queryAccountService.execute(QueryAccountReqDto(isStudent = false))

                        result.totalElements shouldBe 1L
                        val account = result.accounts[0]
                        account.id shouldBe 2L
                        account.isStudent shouldBe false
                        account.student shouldBe null
                    }
                }

                context("조건에 맞는 계정이 없을 때") {
                    beforeEach {
                        every {
                            mockAccountRepository.searchAccountsWithPaging(
                                email = "nobody",
                                role = null,
                                isStudent = null,
                                pageable = PageRequest.of(0, 300),
                                sortBy = any(),
                                sortDirection = any(),
                            )
                        } returns PageImpl(emptyList(), PageRequest.of(0, 300), 0L)
                    }

                    it("빈 결과가 반환되어야 한다") {
                        val result = queryAccountService.execute(QueryAccountReqDto(email = "nobody"))

                        result.totalElements shouldBe 0L
                        result.totalPages shouldBe 0
                        result.accounts.size shouldBe 0
                    }
                }
            }
        }
    })
