package team.themoment.datagsm.web.domain.account.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import org.springframework.http.HttpStatus
import team.themoment.datagsm.common.domain.account.dto.request.ModifyAccountRoleReqDto
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.common.domain.account.repository.AccountJpaRepository
import team.themoment.datagsm.web.domain.account.service.impl.ModifyAccountRoleServiceImpl
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException
import java.util.Optional

class ModifyAccountRoleServiceTest :
    DescribeSpec({

        val mockAccountRepository = mockk<AccountJpaRepository>()
        val mockCurrentUserProvider = mockk<CurrentUserProvider>()

        val modifyAccountRoleService =
            ModifyAccountRoleServiceImpl(mockAccountRepository, mockCurrentUserProvider)

        afterEach {
            clearAllMocks()
        }

        fun account(
            accountId: Long,
            accountRole: AccountRole,
        ): AccountJpaEntity =
            AccountJpaEntity().apply {
                id = accountId
                email = "user$accountId@gsm.hs.kr"
                password = "encoded"
                role = accountRole
            }

        describe("ModifyAccountRoleService 클래스의") {
            describe("execute 메서드는") {

                context("어드민이 다른 USER 계정의 권한을 ADMIN으로 변경할 때") {
                    val target = account(2L, AccountRole.USER)
                    val admin = account(1L, AccountRole.ADMIN)

                    beforeEach {
                        every { mockAccountRepository.findById(2L) } returns Optional.of(target)
                        every { mockCurrentUserProvider.getCurrentAccount() } returns admin
                    }

                    it("대상 계정의 role이 변경되어야 한다") {
                        modifyAccountRoleService.execute(2L, ModifyAccountRoleReqDto(AccountRole.ADMIN))

                        target.role shouldBe AccountRole.ADMIN
                    }
                }

                context("존재하지 않는 계정의 권한을 변경할 때") {
                    beforeEach {
                        every { mockAccountRepository.findById(999L) } returns Optional.empty()
                    }

                    it("NOT_FOUND ExpectedException이 발생해야 한다") {
                        val ex =
                            shouldThrow<ExpectedException> {
                                modifyAccountRoleService.execute(999L, ModifyAccountRoleReqDto(AccountRole.ADMIN))
                            }
                        ex.message shouldBe "계정을 찾을 수 없습니다."
                        ex.statusCode shouldBe HttpStatus.NOT_FOUND
                    }
                }

                context("본인의 권한을 변경하려 할 때") {
                    val admin = account(1L, AccountRole.ADMIN)

                    beforeEach {
                        every { mockAccountRepository.findById(1L) } returns Optional.of(admin)
                        every { mockCurrentUserProvider.getCurrentAccount() } returns admin
                    }

                    it("FORBIDDEN ExpectedException이 발생해야 한다") {
                        val ex =
                            shouldThrow<ExpectedException> {
                                modifyAccountRoleService.execute(1L, ModifyAccountRoleReqDto(AccountRole.USER))
                            }
                        ex.message shouldBe "본인의 권한은 변경할 수 없습니다."
                        ex.statusCode shouldBe HttpStatus.FORBIDDEN
                    }
                }

                context("대상 계정에 ROOT 권한을 부여하려 할 때") {
                    val target = account(2L, AccountRole.USER)
                    val admin = account(1L, AccountRole.ADMIN)

                    beforeEach {
                        every { mockAccountRepository.findById(2L) } returns Optional.of(target)
                        every { mockCurrentUserProvider.getCurrentAccount() } returns admin
                    }

                    it("FORBIDDEN ExpectedException이 발생해야 한다") {
                        val ex =
                            shouldThrow<ExpectedException> {
                                modifyAccountRoleService.execute(2L, ModifyAccountRoleReqDto(AccountRole.ROOT))
                            }
                        ex.message shouldBe "최고 관리자 권한은 부여할 수 없습니다."
                        ex.statusCode shouldBe HttpStatus.FORBIDDEN
                    }
                }

                context("대상 계정이 ROOT일 때") {
                    val target = account(2L, AccountRole.ROOT)
                    val admin = account(1L, AccountRole.ADMIN)

                    beforeEach {
                        every { mockAccountRepository.findById(2L) } returns Optional.of(target)
                        every { mockCurrentUserProvider.getCurrentAccount() } returns admin
                    }

                    it("FORBIDDEN ExpectedException이 발생해야 한다") {
                        val ex =
                            shouldThrow<ExpectedException> {
                                modifyAccountRoleService.execute(2L, ModifyAccountRoleReqDto(AccountRole.USER))
                            }
                        ex.message shouldBe "최고 관리자 계정의 권한은 변경할 수 없습니다."
                        ex.statusCode shouldBe HttpStatus.FORBIDDEN
                    }
                }
            }
        }
    })
