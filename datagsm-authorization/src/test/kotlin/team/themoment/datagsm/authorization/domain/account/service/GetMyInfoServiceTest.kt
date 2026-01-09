package team.themoment.datagsm.authorization.domain.account.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.http.HttpStatus
import team.themoment.datagsm.authorization.domain.account.service.impl.GetMyInfoServiceImpl
import team.themoment.datagsm.authorization.global.security.authentication.principal.CustomPrincipal
import team.themoment.datagsm.authorization.global.security.authentication.type.AuthType
import team.themoment.datagsm.authorization.global.security.provider.CurrentUserProvider
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole
import team.themoment.sdk.exception.ExpectedException

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

                context("API Key 인증으로 요청할 때") {
                    val apiKeyPrincipal =
                        CustomPrincipal(
                            email = testEmail,
                            type = AuthType.API_KEY,
                            clientId = null,
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

                context("JWT 인증으로 요청할 때") {
                    val jwtPrincipal =
                        CustomPrincipal(
                            email = testEmail,
                            type = AuthType.INTERNAL_JWT,
                            clientId = null,
                        )

                    beforeEach {
                        every { mockCurrentUserProvider.getPrincipal() } returns jwtPrincipal
                        every { mockCurrentUserProvider.getCurrentAccount() } returns mockAccount
                    }

                    it("계정 정보가 반환되어야 한다") {
                        val result = getMyInfoService.execute()

                        result.id shouldBe 1L
                        result.email shouldBe testEmail
                        result.role shouldBe AccountRole.USER

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

                        verify(exactly = 1) { mockCurrentUserProvider.getPrincipal() }
                        verify(exactly = 1) { mockCurrentUserProvider.getCurrentAccount() }
                    }
                }
            }
        }
    })
