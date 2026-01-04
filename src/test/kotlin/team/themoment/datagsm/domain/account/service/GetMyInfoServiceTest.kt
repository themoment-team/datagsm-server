package team.themoment.datagsm.domain.account.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.http.HttpStatus
import team.themoment.datagsm.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.domain.account.service.impl.GetMyInfoServiceImpl
import team.themoment.datagsm.domain.club.entity.ClubJpaEntity
import team.themoment.datagsm.domain.club.entity.constant.ClubType
import team.themoment.datagsm.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.domain.student.entity.constant.DormitoryRoomNumber
import team.themoment.datagsm.domain.student.entity.constant.Major
import team.themoment.datagsm.domain.student.entity.constant.Sex
import team.themoment.datagsm.domain.student.entity.constant.StudentNumber
import team.themoment.datagsm.domain.student.entity.constant.StudentRole
import team.themoment.datagsm.global.exception.error.ExpectedException
import team.themoment.datagsm.global.security.authentication.principal.CustomPrincipal
import team.themoment.datagsm.global.security.authentication.type.AuthType
import team.themoment.datagsm.global.security.provider.CurrentUserProvider

class GetMyInfoServiceTest :
    DescribeSpec({

        val mockCurrentUserProvider = mockk<CurrentUserProvider>()

        val getMyInfoService = GetMyInfoServiceImpl(mockCurrentUserProvider)

        afterEach {
            clearAllMocks()
        }

        describe("GetMyInfoService 클래스의") {
            describe("execute 메서드는") {

                val testEmail = "test@gsm.hs.kr"

                val mockAccount =
                    AccountJpaEntity().apply {
                        id = 1L
                        email = testEmail
                        password = "encodedPassword"
                        role = AccountRole.USER
                    }

                val mockStudent =
                    StudentJpaEntity().apply {
                        id = 1L
                        name = "홍길동"
                        sex = Sex.MAN
                        email = testEmail
                        studentNumber = StudentNumber(1, 1, 1)
                        major = Major.SW_DEVELOPMENT
                        role = StudentRole.GENERAL_STUDENT
                        dormitoryRoomNumber = DormitoryRoomNumber(301)
                        isLeaveSchool = false
                    }

                val mockMajorClub =
                    ClubJpaEntity().apply {
                        id = 1L
                        name = "SW개발동아리"
                        type = ClubType.MAJOR_CLUB
                    }

                context("API Key 인증으로 요청할 때") {
                    val apiKeyPrincipal =
                        CustomPrincipal(
                            email = testEmail,
                            type = AuthType.API_KEY,
                            clientId = null,
                            apiKey = null,
                        )

                    beforeEach {
                        every { mockCurrentUserProvider.getPrincipal() } returns apiKeyPrincipal
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                getMyInfoService.execute()
                            }

                        exception.message shouldBe "API Key 인증은 해당 API를 지원하지 않습니다."
                        exception.statusCode shouldBe HttpStatus.FORBIDDEN

                        verify(exactly = 1) { mockCurrentUserProvider.getPrincipal() }
                        verify(exactly = 0) { mockCurrentUserProvider.getCurrentAccount() }
                    }
                }

                context("JWT 인증으로 요청하고 학생 정보가 없을 때") {
                    val jwtPrincipal =
                        CustomPrincipal(
                            email = testEmail,
                            type = AuthType.INTERNAL_JWT,
                            clientId = null,
                            apiKey = null,
                        )

                    beforeEach {
                        every { mockCurrentUserProvider.getPrincipal() } returns jwtPrincipal
                        every { mockCurrentUserProvider.getCurrentAccount() } returns mockAccount
                    }

                    it("학생 정보가 null인 계정 정보가 반환되어야 한다") {
                        val result = getMyInfoService.execute()

                        result.id shouldBe 1L
                        result.email shouldBe testEmail
                        result.role shouldBe AccountRole.USER
                        result.isStudent shouldBe false
                        result.student shouldBe null

                        verify(exactly = 1) { mockCurrentUserProvider.getPrincipal() }
                        verify(exactly = 1) { mockCurrentUserProvider.getCurrentAccount() }
                    }
                }

                context("JWT 인증으로 요청하고 학생 정보가 있을 때") {
                    val jwtPrincipal =
                        CustomPrincipal(
                            email = testEmail,
                            type = AuthType.INTERNAL_JWT,
                            clientId = null,
                            apiKey = null,
                        )

                    val accountWithStudent =
                        AccountJpaEntity().apply {
                            id = 1L
                            email = testEmail
                            password = "encodedPassword"
                            role = AccountRole.USER
                            student = mockStudent.apply { majorClub = mockMajorClub }
                        }

                    beforeEach {
                        every { mockCurrentUserProvider.getPrincipal() } returns jwtPrincipal
                        every { mockCurrentUserProvider.getCurrentAccount() } returns accountWithStudent
                    }

                    it("학생 정보를 포함한 계정 정보가 반환되어야 한다") {
                        val result = getMyInfoService.execute()

                        result.id shouldBe 1L
                        result.email shouldBe testEmail
                        result.role shouldBe AccountRole.USER
                        result.isStudent shouldBe true
                        result.student shouldNotBe null

                        val studentResult = result.student!!
                        studentResult.id shouldBe 1L
                        studentResult.name shouldBe "홍길동"
                        studentResult.sex shouldBe Sex.MAN
                        studentResult.email shouldBe testEmail
                        studentResult.grade shouldBe 1
                        studentResult.classNum shouldBe 1
                        studentResult.number shouldBe 1
                        studentResult.studentNumber shouldBe 1101
                        studentResult.major shouldBe Major.SW_DEVELOPMENT
                        studentResult.role shouldBe StudentRole.GENERAL_STUDENT
                        studentResult.dormitoryFloor shouldBe 3
                        studentResult.dormitoryRoom shouldBe 301
                        studentResult.isLeaveSchool shouldBe false

                        studentResult.majorClub shouldNotBe null
                        studentResult.majorClub!!.id shouldBe 1L
                        studentResult.majorClub!!.name shouldBe "SW개발동아리"
                        studentResult.majorClub!!.type shouldBe ClubType.MAJOR_CLUB

                        verify(exactly = 1) { mockCurrentUserProvider.getPrincipal() }
                        verify(exactly = 1) { mockCurrentUserProvider.getCurrentAccount() }
                    }
                }

                context("OAuth JWT 인증으로 요청할 때") {
                    val oauthPrincipal =
                        CustomPrincipal(
                            email = testEmail,
                            type = AuthType.OAUTH_JWT,
                            clientId = "client-123",
                            apiKey = null,
                        )

                    beforeEach {
                        every { mockCurrentUserProvider.getPrincipal() } returns oauthPrincipal
                        every { mockCurrentUserProvider.getCurrentAccount() } returns mockAccount
                    }

                    it("계정 정보가 정상적으로 반환되어야 한다") {
                        val result = getMyInfoService.execute()

                        result.id shouldBe 1L
                        result.email shouldBe testEmail
                        result.role shouldBe AccountRole.USER
                        result.isStudent shouldBe false

                        verify(exactly = 1) { mockCurrentUserProvider.getPrincipal() }
                        verify(exactly = 1) { mockCurrentUserProvider.getCurrentAccount() }
                    }
                }
            }
        }
    })
