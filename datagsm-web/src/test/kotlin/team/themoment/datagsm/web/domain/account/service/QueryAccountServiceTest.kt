package team.themoment.datagsm.web.domain.account.service

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import team.themoment.datagsm.common.domain.account.dto.internal.ResolvedAccountObject
import team.themoment.datagsm.common.domain.account.dto.request.QueryAccountReqDto
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.account.entity.constant.AccountObjectType
import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.common.domain.account.repository.AccountJpaRepository
import team.themoment.datagsm.common.domain.account.resolver.AccountObjectResolver
import team.themoment.datagsm.common.domain.student.dto.response.StudentResDto
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.common.domain.student.entity.StudentNumber
import team.themoment.datagsm.common.domain.student.entity.constant.Major
import team.themoment.datagsm.common.domain.student.entity.constant.Sex
import team.themoment.datagsm.common.domain.student.entity.constant.StudentRole
import team.themoment.datagsm.common.domain.teacher.dto.response.TeacherResDto
import team.themoment.datagsm.common.domain.teacher.entity.TeacherJpaEntity
import team.themoment.datagsm.web.domain.account.service.impl.QueryAccountServiceImpl

class QueryAccountServiceTest :
    DescribeSpec({

        val mockAccountRepository = mockk<AccountJpaRepository>()
        val mockAccountObjectResolver = mockk<AccountObjectResolver>()

        val queryAccountService = QueryAccountServiceImpl(mockAccountRepository, mockAccountObjectResolver)

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
                objectId = 10L
                objectType = AccountObjectType.STUDENT
            }

        val linkedTeacher = TeacherJpaEntity.create("김선생", "teacher@gsm.hs.kr").apply { id = 20L }

        val teacherAccount =
            AccountJpaEntity().apply {
                id = 2L
                email = "teacher@gsm.hs.kr"
                password = "encoded"
                role = AccountRole.USER
                objectId = 20L
                objectType = AccountObjectType.TEACHER
            }

        describe("QueryAccountService 클래스의") {
            describe("execute 메서드는") {

                context("학생과 연결된 계정을 조회할 때") {
                    beforeEach {
                        every {
                            mockAccountRepository.searchAccountsWithPaging(
                                email = null,
                                role = null,
                                objectType = AccountObjectType.STUDENT,
                                status = null,
                                pageable = PageRequest.of(0, 300),
                                sortBy = any(),
                                sortDirection = any(),
                            )
                        } returns PageImpl(listOf(studentAccount), PageRequest.of(0, 300), 1L)
                        every { mockAccountObjectResolver.resolveAll(listOf(studentAccount)) } returns
                            mapOf(1L to ResolvedAccountObject(StudentResDto.from(linkedStudent), null))
                    }

                    it("연결된 학생 정보가 포함되어야 한다") {
                        val result =
                            queryAccountService.execute(
                                QueryAccountReqDto(objectType = AccountObjectType.STUDENT),
                            )

                        result.totalElements shouldBe 1L
                        val account = result.accounts[0]
                        account.id shouldBe 1L
                        account.objectType shouldBe AccountObjectType.STUDENT
                        account.student?.id shouldBe 10L
                        account.student?.name shouldBe "홍길동"
                    }
                }

                context("선생님과 연결된 계정을 조회할 때") {
                    beforeEach {
                        every {
                            mockAccountRepository.searchAccountsWithPaging(
                                email = null,
                                role = null,
                                objectType = AccountObjectType.TEACHER,
                                status = null,
                                pageable = PageRequest.of(0, 300),
                                sortBy = any(),
                                sortDirection = any(),
                            )
                        } returns PageImpl(listOf(teacherAccount), PageRequest.of(0, 300), 1L)
                        every { mockAccountObjectResolver.resolveAll(listOf(teacherAccount)) } returns
                            mapOf(2L to ResolvedAccountObject(null, TeacherResDto.from(linkedTeacher)))
                    }

                    it("연결된 선생님 정보가 포함되어야 한다") {
                        val result =
                            queryAccountService.execute(
                                QueryAccountReqDto(objectType = AccountObjectType.TEACHER),
                            )

                        result.totalElements shouldBe 1L
                        val account = result.accounts[0]
                        account.id shouldBe 2L
                        account.objectType shouldBe AccountObjectType.TEACHER
                        account.student shouldBe null
                        account.teacher?.id shouldBe 20L
                        account.teacher?.name shouldBe "김선생"
                    }
                }

                context("조건에 맞는 계정이 없을 때") {
                    beforeEach {
                        every {
                            mockAccountRepository.searchAccountsWithPaging(
                                email = "nobody",
                                role = null,
                                objectType = null,
                                status = null,
                                pageable = PageRequest.of(0, 300),
                                sortBy = any(),
                                sortDirection = any(),
                            )
                        } returns PageImpl(emptyList(), PageRequest.of(0, 300), 0L)
                        every { mockAccountObjectResolver.resolveAll(emptyList()) } returns emptyMap()
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
