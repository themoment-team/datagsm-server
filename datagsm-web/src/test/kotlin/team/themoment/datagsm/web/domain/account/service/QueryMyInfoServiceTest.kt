package team.themoment.datagsm.web.domain.account.service

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import team.themoment.datagsm.common.domain.account.dto.internal.ResolvedAccountObject
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.account.entity.constant.AccountObjectType
import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.common.domain.account.resolver.AccountObjectResolver
import team.themoment.datagsm.common.domain.student.dto.response.StudentResDto
import team.themoment.datagsm.common.domain.student.entity.DormitoryRoomNumber
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.common.domain.student.entity.StudentNumber
import team.themoment.datagsm.common.domain.student.entity.constant.Major
import team.themoment.datagsm.common.domain.student.entity.constant.Sex
import team.themoment.datagsm.common.domain.teacher.dto.response.TeacherResDto
import team.themoment.datagsm.common.domain.teacher.entity.TeacherJpaEntity
import team.themoment.datagsm.common.domain.teacher.entity.constant.TeacherDepartment
import team.themoment.datagsm.web.domain.account.service.impl.QueryMyInfoServiceImpl
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider

class QueryMyInfoServiceTest :
    DescribeSpec({

        lateinit var mockCurrentUserProvider: CurrentUserProvider
        lateinit var mockAccountObjectResolver: AccountObjectResolver
        lateinit var queryMyInfoService: QueryMyInfoService

        beforeEach {
            mockCurrentUserProvider = mockk<CurrentUserProvider>()
            mockAccountObjectResolver = mockk<AccountObjectResolver>()
            queryMyInfoService = QueryMyInfoServiceImpl(mockCurrentUserProvider, mockAccountObjectResolver)
        }

        describe("QueryMyInfoService 클래스의") {
            describe("execute 메서드는") {

                context("연결 대상이 없는 계정일 때") {
                    lateinit var account: AccountJpaEntity

                    beforeEach {
                        account =
                            AccountJpaEntity().apply {
                                id = 1L
                                email = "admin@gsm.hs.kr"
                                password = "encoded_password"
                                role = AccountRole.ADMIN
                            }
                        every { mockCurrentUserProvider.getCurrentAccount() } returns account
                        every { mockAccountObjectResolver.resolve(account) } returns ResolvedAccountObject(null, null)
                    }

                    it("연결 대상 정보 없이 계정 정보를 반환해야 한다") {
                        val result = queryMyInfoService.execute()

                        result.id shouldBe 1L
                        result.email shouldBe "admin@gsm.hs.kr"
                        result.role shouldBe AccountRole.ADMIN
                        result.objectType shouldBe null
                        result.student shouldBe null
                        result.teacher shouldBe null

                        verify(exactly = 1) { mockCurrentUserProvider.getCurrentAccount() }
                    }
                }

                context("학생 정보가 있는 계정일 때") {
                    lateinit var account: AccountJpaEntity
                    lateinit var student: StudentJpaEntity

                    beforeEach {
                        student =
                            StudentJpaEntity().apply {
                                id = 10L
                                name = "홍길동"
                                sex = Sex.MAN
                                email = "hong@gsm.hs.kr"
                                studentNumber = StudentNumber(1, 2, 3)
                                major = Major.SW_DEVELOPMENT
                                dormitoryRoomNumber = DormitoryRoomNumber(201)
                            }
                        account =
                            AccountJpaEntity().apply {
                                id = 2L
                                email = "hong@gsm.hs.kr"
                                password = "encoded_password"
                                role = AccountRole.USER
                                objectId = 10L
                                objectType = AccountObjectType.STUDENT
                            }
                        every { mockCurrentUserProvider.getCurrentAccount() } returns account
                        every { mockAccountObjectResolver.resolve(account) } returns
                            ResolvedAccountObject(StudentResDto.from(student), null)
                    }

                    it("학생 정보를 포함하여 계정 정보를 반환해야 한다") {
                        val result = queryMyInfoService.execute()

                        result.id shouldBe 2L
                        result.email shouldBe "hong@gsm.hs.kr"
                        result.role shouldBe AccountRole.USER
                        result.objectType shouldBe AccountObjectType.STUDENT

                        val studentDto = result.student!!
                        studentDto.id shouldBe 10L
                        studentDto.name shouldBe "홍길동"
                        studentDto.sex shouldBe Sex.MAN
                        studentDto.email shouldBe "hong@gsm.hs.kr"
                        studentDto.grade shouldBe 1
                        studentDto.classNum shouldBe 2
                        studentDto.number shouldBe 3
                        studentDto.studentNumber shouldBe 1203
                        studentDto.major shouldBe Major.SW_DEVELOPMENT
                        studentDto.dormitoryFloor shouldBe 2
                        studentDto.dormitoryRoom shouldBe 201

                        verify(exactly = 1) { mockCurrentUserProvider.getCurrentAccount() }
                    }
                }

                context("선생님 정보가 있는 계정일 때") {
                    lateinit var account: AccountJpaEntity
                    lateinit var teacher: TeacherJpaEntity

                    beforeEach {
                        teacher =
                            TeacherJpaEntity
                                .create("김선생", "teacher@gsm.hs.kr", TeacherDepartment.GRADE, "3학년 1반 담임선생님")
                                .apply { id = 50L }
                        account =
                            AccountJpaEntity().apply {
                                id = 5L
                                email = "teacher@gsm.hs.kr"
                                password = "encoded_password"
                                role = AccountRole.USER
                                objectId = 50L
                                objectType = AccountObjectType.TEACHER
                            }
                        every { mockCurrentUserProvider.getCurrentAccount() } returns account
                        every { mockAccountObjectResolver.resolve(account) } returns
                            ResolvedAccountObject(null, TeacherResDto.from(teacher))
                    }

                    it("선생님 정보를 포함하여 계정 정보를 반환해야 한다") {
                        val result = queryMyInfoService.execute()

                        result.objectType shouldBe AccountObjectType.TEACHER
                        result.student shouldBe null
                        val teacherDto = result.teacher!!
                        teacherDto.id shouldBe 50L
                        teacherDto.name shouldBe "김선생"
                        teacherDto.email shouldBe "teacher@gsm.hs.kr"
                    }
                }
            }
        }
    })
