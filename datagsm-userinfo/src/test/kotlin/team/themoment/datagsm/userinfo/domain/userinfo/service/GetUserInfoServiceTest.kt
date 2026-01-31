package team.themoment.datagsm.userinfo.domain.userinfo.service

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.userinfo.domain.userinfo.service.impl.GetUserInfoServiceImpl
import team.themoment.datagsm.userinfo.global.security.provider.CurrentUserProvider

class GetUserInfoServiceTest :
    DescribeSpec({

        val mockCurrentUserProvider = mockk<CurrentUserProvider>()
        val getUserInfoService = GetUserInfoServiceImpl(mockCurrentUserProvider)

        afterEach {
            clearAllMocks()
        }

        describe("GetUserInfoService 클래스의") {
            describe("execute 메서드는") {

                val testEmail = "test@gsm.hs.kr"
                val mockAccount =
                    AccountJpaEntity().apply {
                        id = 1L
                        email = testEmail
                        password = "encodedPassword"
                        role = AccountRole.USER
                    }

                context("OAuth JWT 인증으로 요청할 때") {
                    beforeEach {
                        every { mockCurrentUserProvider.getCurrentAccount() } returns mockAccount
                    }

                    it("계정 정보가 정상적으로 반환되어야 한다") {
                        val result = getUserInfoService.execute()

                        result.id shouldBe 1L
                        result.email shouldBe testEmail
                        result.role shouldBe AccountRole.USER
                        result.isStudent shouldBe false

                        verify(exactly = 1) { mockCurrentUserProvider.getCurrentAccount() }
                    }
                }
            }
        }
    })
