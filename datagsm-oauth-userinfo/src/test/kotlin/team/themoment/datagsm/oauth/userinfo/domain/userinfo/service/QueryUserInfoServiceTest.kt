package team.themoment.datagsm.oauth.userinfo.domain.userinfo.service

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import team.themoment.datagsm.common.domain.account.dto.internal.ResolvedAccountObject
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.account.entity.constant.AccountObjectType
import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.common.domain.account.resolver.AccountObjectResolver
import team.themoment.datagsm.common.domain.club.dto.internal.ClubSummaryDto
import team.themoment.datagsm.common.domain.club.entity.constant.ClubType
import team.themoment.datagsm.common.domain.project.dto.internal.ProjectSummaryDto
import team.themoment.datagsm.common.domain.project.entity.constant.ProjectStatus
import team.themoment.datagsm.common.domain.student.dto.response.StudentResDto
import team.themoment.datagsm.common.domain.student.entity.constant.Sex
import team.themoment.datagsm.common.domain.student.entity.constant.StudentRole
import team.themoment.datagsm.common.domain.teacher.dto.response.TeacherResDto
import team.themoment.datagsm.common.domain.teacher.entity.TeacherJpaEntity
import team.themoment.datagsm.common.domain.teacher.entity.constant.TeacherDepartment
import team.themoment.datagsm.oauth.userinfo.domain.userinfo.service.impl.QueryUserInfoServiceImpl
import team.themoment.datagsm.oauth.userinfo.global.security.provider.CurrentUserProvider

class QueryUserInfoServiceTest :
    DescribeSpec({

        val mockCurrentUserProvider = mockk<CurrentUserProvider>()
        val mockAccountObjectResolver = mockk<AccountObjectResolver>()
        val queryUserInfoService = QueryUserInfoServiceImpl(mockCurrentUserProvider, mockAccountObjectResolver)

        afterEach {
            clearAllMocks()
        }

        val studentDto =
            StudentResDto(
                id = 10L,
                name = "홍길동",
                sex = Sex.MAN,
                email = "student@gsm.hs.kr",
                grade = 1,
                classNum = 1,
                number = 1,
                studentNumber = 1101,
                major = null,
                specialty = null,
                role = StudentRole.GENERAL_STUDENT,
                dormitoryFloor = null,
                dormitoryRoom = null,
                majorClub = null,
                autonomousClub = null,
                githubId = null,
                githubUrl = null,
            )
        val clubSummary = ClubSummaryDto(id = 100L, name = "SW개발동아리", type = ClubType.MAJOR_CLUB)
        val projectSummary = ProjectSummaryDto(id = 200L, name = "DataGSM", status = ProjectStatus.ACTIVE, club = clubSummary)

        describe("QueryUserInfoService 클래스의") {
            describe("execute 메서드는") {

                context("연결 대상이 없는 계정으로 요청할 때") {
                    val testEmail = "test@gsm.hs.kr"
                    val mockAccount =
                        AccountJpaEntity().apply {
                            id = 1L
                            email = testEmail
                            password = "encodedPassword"
                            role = AccountRole.USER
                        }

                    beforeEach {
                        every { mockCurrentUserProvider.getCurrentAccount() } returns mockAccount
                        every { mockAccountObjectResolver.resolve(mockAccount, includeClubs = false, includeProjects = false) } returns
                            ResolvedAccountObject(null, null)
                        every { mockCurrentUserProvider.getGrantedScopeNames() } returns setOf("account_read")
                    }

                    it("계정 정보가 정상적으로 반환되어야 한다") {
                        val result = queryUserInfoService.execute()

                        result.id shouldBe 1L
                        result.email shouldBe testEmail
                        result.role shouldBe AccountRole.USER
                        result.objectType shouldBe null
                        result.student shouldBe null
                        result.teacher shouldBe null
                        result.clubs.shouldBeEmpty()
                        result.projects.shouldBeEmpty()

                        verify(exactly = 1) { mockCurrentUserProvider.getCurrentAccount() }
                    }
                }

                context("선생님 계정으로 요청할 때") {
                    val teacher =
                        TeacherJpaEntity
                            .create("김선생", "teacher@gsm.hs.kr", TeacherDepartment.GRADE, "3학년 1반 담임선생님")
                            .apply { id = 50L }
                    val mockAccount =
                        AccountJpaEntity().apply {
                            id = 5L
                            email = "teacher@gsm.hs.kr"
                            password = "encodedPassword"
                            role = AccountRole.USER
                            objectId = 50L
                            objectType = AccountObjectType.TEACHER
                        }

                    beforeEach {
                        every { mockCurrentUserProvider.getCurrentAccount() } returns mockAccount
                        every { mockAccountObjectResolver.resolve(mockAccount, includeClubs = false, includeProjects = false) } returns
                            ResolvedAccountObject(null, TeacherResDto.from(teacher))
                        every { mockCurrentUserProvider.getGrantedScopeNames() } returns setOf("account_read", "student_read")
                    }

                    it("student_read 스코프가 있으면 선생님 정보가 포함되어 반환되어야 한다") {
                        val result = queryUserInfoService.execute()

                        result.objectType shouldBe AccountObjectType.TEACHER
                        result.teacher?.id shouldBe 50L
                        result.teacher?.name shouldBe "김선생"
                    }
                }

                context("학생 계정이 account_read 스코프만 가지고 있을 때") {
                    val mockAccount =
                        AccountJpaEntity().apply {
                            id = 2L
                            email = "student@gsm.hs.kr"
                            password = "encodedPassword"
                            role = AccountRole.USER
                            objectId = 10L
                            objectType = AccountObjectType.STUDENT
                        }

                    beforeEach {
                        every { mockCurrentUserProvider.getCurrentAccount() } returns mockAccount
                        every { mockAccountObjectResolver.resolve(mockAccount, includeClubs = false, includeProjects = false) } returns
                            ResolvedAccountObject(studentDto, null)
                        every { mockCurrentUserProvider.getGrantedScopeNames() } returns setOf("account_read")
                    }

                    it("student/clubs/projects는 모두 비어있어야 한다") {
                        val result = queryUserInfoService.execute()

                        result.student shouldBe null
                        result.teacher shouldBe null
                        result.clubs.shouldBeEmpty()
                        result.projects.shouldBeEmpty()
                    }
                }

                context("학생 계정이 account_read와 student_read 스코프를 가지고 있을 때") {
                    val mockAccount =
                        AccountJpaEntity().apply {
                            id = 2L
                            email = "student@gsm.hs.kr"
                            password = "encodedPassword"
                            role = AccountRole.USER
                            objectId = 10L
                            objectType = AccountObjectType.STUDENT
                        }

                    beforeEach {
                        every { mockCurrentUserProvider.getCurrentAccount() } returns mockAccount
                        every { mockAccountObjectResolver.resolve(mockAccount, includeClubs = false, includeProjects = false) } returns
                            ResolvedAccountObject(studentDto, null)
                        every { mockCurrentUserProvider.getGrantedScopeNames() } returns setOf("account_read", "student_read")
                    }

                    it("student 정보만 채워지고 clubs/projects는 비어있어야 한다") {
                        val result = queryUserInfoService.execute()

                        result.student shouldBe studentDto
                        result.clubs.shouldBeEmpty()
                        result.projects.shouldBeEmpty()
                    }
                }

                context("학생 계정이 club_read 스코프까지 가지고 있을 때") {
                    val mockAccount =
                        AccountJpaEntity().apply {
                            id = 2L
                            email = "student@gsm.hs.kr"
                            password = "encodedPassword"
                            role = AccountRole.USER
                            objectId = 10L
                            objectType = AccountObjectType.STUDENT
                        }

                    beforeEach {
                        every { mockCurrentUserProvider.getCurrentAccount() } returns mockAccount
                        every { mockAccountObjectResolver.resolve(mockAccount, includeClubs = true, includeProjects = false) } returns
                            ResolvedAccountObject(studentDto, null, listOf(clubSummary))
                        every { mockCurrentUserProvider.getGrantedScopeNames() } returns setOf("account_read", "student_read", "club_read")
                    }

                    it("clubs가 채워지고 projects는 비어있어야 한다") {
                        val result = queryUserInfoService.execute()

                        result.clubs shouldBe listOf(clubSummary)
                        result.projects.shouldBeEmpty()
                    }
                }

                context("학생 계정이 project_read 스코프까지 가지고 있을 때") {
                    val mockAccount =
                        AccountJpaEntity().apply {
                            id = 2L
                            email = "student@gsm.hs.kr"
                            password = "encodedPassword"
                            role = AccountRole.USER
                            objectId = 10L
                            objectType = AccountObjectType.STUDENT
                        }

                    beforeEach {
                        every { mockCurrentUserProvider.getCurrentAccount() } returns mockAccount
                        every { mockAccountObjectResolver.resolve(mockAccount, includeClubs = true, includeProjects = true) } returns
                            ResolvedAccountObject(studentDto, null, listOf(clubSummary), listOf(projectSummary))
                        every { mockCurrentUserProvider.getGrantedScopeNames() } returns
                            setOf("account_read", "student_read", "club_read", "project_read")
                    }

                    it("clubs와 projects가 모두 채워져야 한다") {
                        val result = queryUserInfoService.execute()

                        result.clubs shouldBe listOf(clubSummary)
                        result.projects shouldBe listOf(projectSummary)
                    }
                }

                context("학생 계정이 deprecated self_read 스코프만 가지고 있을 때 (하위 호환)") {
                    val mockAccount =
                        AccountJpaEntity().apply {
                            id = 2L
                            email = "student@gsm.hs.kr"
                            password = "encodedPassword"
                            role = AccountRole.USER
                            objectId = 10L
                            objectType = AccountObjectType.STUDENT
                        }

                    beforeEach {
                        every { mockCurrentUserProvider.getCurrentAccount() } returns mockAccount
                        every { mockAccountObjectResolver.resolve(mockAccount, includeClubs = false, includeProjects = false) } returns
                            ResolvedAccountObject(studentDto, null)
                        every { mockCurrentUserProvider.getGrantedScopeNames() } returns setOf("self_read")
                    }

                    it("student 정보는 채워지고 clubs/projects는 비어있어야 한다") {
                        val result = queryUserInfoService.execute()

                        result.student shouldBe studentDto
                        result.clubs.shouldBeEmpty()
                        result.projects.shouldBeEmpty()
                    }
                }
            }
        }
    })
