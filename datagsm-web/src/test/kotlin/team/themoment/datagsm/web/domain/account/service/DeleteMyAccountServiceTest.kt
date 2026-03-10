package team.themoment.datagsm.web.domain.account.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import team.themoment.datagsm.common.domain.account.dto.request.DeleteMyAccountReqDto
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.common.domain.account.repository.AccountJpaRepository
import team.themoment.datagsm.common.domain.auth.repository.ApiKeyJpaRepository
import team.themoment.datagsm.common.domain.client.repository.ClientJpaRepository
import team.themoment.datagsm.web.domain.account.service.impl.DeleteMyAccountServiceImpl
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException

class DeleteMyAccountServiceTest :
    DescribeSpec({

        lateinit var mockCurrentUserProvider: CurrentUserProvider
        lateinit var mockPasswordEncoder: PasswordEncoder
        lateinit var mockApiKeyJpaRepository: ApiKeyJpaRepository
        lateinit var mockClientJpaRepository: ClientJpaRepository
        lateinit var mockAccountJpaRepository: AccountJpaRepository
        lateinit var deleteMyAccountService: DeleteMyAccountService

        beforeEach {
            mockCurrentUserProvider = mockk<CurrentUserProvider>()
            mockPasswordEncoder = mockk<PasswordEncoder>()
            mockApiKeyJpaRepository = mockk<ApiKeyJpaRepository>()
            mockClientJpaRepository = mockk<ClientJpaRepository>()
            mockAccountJpaRepository = mockk<AccountJpaRepository>()
            deleteMyAccountService =
                DeleteMyAccountServiceImpl(
                    mockCurrentUserProvider,
                    mockPasswordEncoder,
                    mockApiKeyJpaRepository,
                    mockClientJpaRepository,
                    mockAccountJpaRepository,
                )
        }

        describe("DeleteMyAccountService 클래스의") {
            describe("execute 메서드는") {

                context("올바른 비밀번호로 탈퇴를 요청할 때") {
                    lateinit var account: AccountJpaEntity

                    beforeEach {
                        account =
                            AccountJpaEntity().apply {
                                id = 1L
                                email = "user@gsm.hs.kr"
                                password = "encoded_password"
                                role = AccountRole.USER
                            }
                        val reqDto = DeleteMyAccountReqDto(password = "plain_password")

                        every { mockCurrentUserProvider.getCurrentAccount() } returns account
                        every { mockPasswordEncoder.matches("plain_password", "encoded_password") } returns true
                        justRun { mockApiKeyJpaRepository.deleteByAccount(account) }
                        justRun { mockClientJpaRepository.deleteAllByAccount(account) }
                        justRun { mockAccountJpaRepository.delete(account) }

                        deleteMyAccountService.execute(reqDto)
                    }

                    it("ApiKey, Client, Account 순서로 각각 1회씩 삭제해야 한다") {
                        verify(exactly = 1) { mockApiKeyJpaRepository.deleteByAccount(account) }
                        verify(exactly = 1) { mockClientJpaRepository.deleteAllByAccount(account) }
                        verify(exactly = 1) { mockAccountJpaRepository.delete(account) }
                    }
                }

                context("잘못된 비밀번호로 탈퇴를 요청할 때") {
                    lateinit var account: AccountJpaEntity

                    beforeEach {
                        account =
                            AccountJpaEntity().apply {
                                id = 1L
                                email = "user@gsm.hs.kr"
                                password = "encoded_password"
                                role = AccountRole.USER
                            }
                        every { mockCurrentUserProvider.getCurrentAccount() } returns account
                        every { mockPasswordEncoder.matches("wrong_password", "encoded_password") } returns false
                    }

                    it("UNAUTHORIZED 상태의 ExpectedException이 발생해야 한다") {
                        val reqDto = DeleteMyAccountReqDto(password = "wrong_password")

                        val exception =
                            shouldThrow<ExpectedException> {
                                deleteMyAccountService.execute(reqDto)
                            }

                        exception.statusCode shouldBe HttpStatus.UNAUTHORIZED
                        verify(exactly = 0) { mockApiKeyJpaRepository.deleteByAccount(any()) }
                        verify(exactly = 0) { mockClientJpaRepository.deleteAllByAccount(any()) }
                        verify(exactly = 0) { mockAccountJpaRepository.delete(any()) }
                    }
                }

                context("계정을 찾을 수 없을 때") {
                    beforeEach {
                        every { mockCurrentUserProvider.getCurrentAccount() } throws
                            ExpectedException("계정을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
                    }

                    it("NOT_FOUND 상태의 ExpectedException이 발생해야 한다") {
                        val reqDto = DeleteMyAccountReqDto(password = "any_password")

                        val exception =
                            shouldThrow<ExpectedException> {
                                deleteMyAccountService.execute(reqDto)
                            }

                        exception.statusCode shouldBe HttpStatus.NOT_FOUND
                        verify(exactly = 0) { mockApiKeyJpaRepository.deleteByAccount(any()) }
                    }
                }
            }
        }
    })
