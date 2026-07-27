package team.themoment.datagsm.web.domain.account.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import org.springframework.http.HttpStatus
import team.themoment.datagsm.common.domain.account.dto.internal.ResolvedAccountObject
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
import team.themoment.datagsm.common.domain.teacher.entity.constant.TeacherDepartment
import team.themoment.datagsm.web.domain.account.service.impl.QueryAccountDetailServiceImpl
import team.themoment.sdk.exception.ExpectedException
import java.util.Optional

class QueryAccountDetailServiceTest :
    DescribeSpec({

        val mockAccountRepository = mockk<AccountJpaRepository>()
        val mockAccountObjectResolver = mockk<AccountObjectResolver>()

        val queryAccountDetailService =
            QueryAccountDetailServiceImpl(mockAccountRepository, mockAccountObjectResolver)

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
                            objectId = 10L
                            objectType = AccountObjectType.STUDENT
                        }

                    beforeEach {
                        every { mockAccountRepository.findById(1L) } returns Optional.of(studentAccount)
                        every { mockAccountObjectResolver.resolve(studentAccount) } returns
                            ResolvedAccountObject(StudentResDto.from(linkedStudent), null)
                    }

                    it("연결된 학생 정보가 포함되어야 한다") {
                        val result = queryAccountDetailService.execute(1L)

                        result.id shouldBe 1L
                        result.objectType shouldBe AccountObjectType.STUDENT
                        result.student?.id shouldBe 10L
                        result.student?.name shouldBe "홍길동"
                    }
                }

                context("선생님과 연결된 계정을 조회할 때") {
                    val linkedTeacher =
                        TeacherJpaEntity
                            .create("김선생", "teacher@gsm.hs.kr", TeacherDepartment.GRADE, "3학년 1반 담임선생님")
                            .apply { id = 20L }
                    val teacherAccount =
                        AccountJpaEntity().apply {
                            id = 2L
                            email = "teacher@gsm.hs.kr"
                            password = "encoded"
                            role = AccountRole.USER
                            objectId = 20L
                            objectType = AccountObjectType.TEACHER
                        }

                    beforeEach {
                        every { mockAccountRepository.findById(2L) } returns Optional.of(teacherAccount)
                        every { mockAccountObjectResolver.resolve(teacherAccount) } returns
                            ResolvedAccountObject(null, TeacherResDto.from(linkedTeacher))
                    }

                    it("연결된 선생님 정보가 포함되어야 한다") {
                        val result = queryAccountDetailService.execute(2L)

                        result.id shouldBe 2L
                        result.objectType shouldBe AccountObjectType.TEACHER
                        result.student shouldBe null
                        result.teacher?.id shouldBe 20L
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
