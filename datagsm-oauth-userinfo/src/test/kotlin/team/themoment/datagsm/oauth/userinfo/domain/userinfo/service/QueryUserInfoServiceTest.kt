package team.themoment.datagsm.oauth.userinfo.domain.userinfo.service

import io.kotest.core.spec.style.DescribeSpec
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
                        every { mockAccountObjectResolver.resolve(mockAccount) } returns
                            ResolvedAccountObject(null, null)
                    }

                    it("계정 정보가 정상적으로 반환되어야 한다") {
                        val result = queryUserInfoService.execute()

                        result.id shouldBe 1L
                        result.email shouldBe testEmail
                        result.role shouldBe AccountRole.USER
                        result.objectType shouldBe null
                        result.student shouldBe null
                        result.teacher shouldBe null

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
                        every { mockAccountObjectResolver.resolve(mockAccount) } returns
                            ResolvedAccountObject(null, TeacherResDto.from(teacher))
                    }

                    it("선생님 정보가 포함되어 반환되어야 한다") {
                        val result = queryUserInfoService.execute()

                        result.objectType shouldBe AccountObjectType.TEACHER
                        result.teacher?.id shouldBe 50L
                        result.teacher?.name shouldBe "김선생"
                    }
                }
            }
        }
    })
