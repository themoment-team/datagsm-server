package team.themoment.datagsm.web.domain.utility.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.common.domain.account.repository.AccountJpaRepository
import team.themoment.datagsm.web.domain.utility.service.impl.ModifyAccountRoleServiceImpl
import team.themoment.sdk.exception.ExpectedException
import java.util.Optional

class ModifyAccountRoleServiceTest :
    DescribeSpec({

        val mockAccountJpaRepository = mockk<AccountJpaRepository>()

        val modifyAccountRoleService = ModifyAccountRoleServiceImpl(mockAccountJpaRepository)

        afterEach {
            clearAllMocks()
        }

        describe("ModifyAccountRoleService 클래스의") {
            describe("execute 메서드는") {

                context("존재하는 이메일로 권한을 변경할 때") {
                    val email = "test@gsm.hs.kr"
                    val account =
                        AccountJpaEntity().apply {
                            id = 1L
                            this.email = email
                            role = AccountRole.USER
                        }

                    beforeEach {
                        every { mockAccountJpaRepository.findByEmail(email) } returns Optional.of(account)
                    }

                    it("계정 권한이 변경되어야 한다") {
                        modifyAccountRoleService.execute(email, AccountRole.ADMIN)

                        account.role shouldBe AccountRole.ADMIN
                        verify(exactly = 1) { mockAccountJpaRepository.findByEmail(email) }
                    }
                }

                context("존재하지 않는 이메일로 권한을 변경할 때") {
                    val email = "notfound@gsm.hs.kr"

                    beforeEach {
                        every { mockAccountJpaRepository.findByEmail(email) } returns Optional.empty()
                    }

                    it("404 ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                modifyAccountRoleService.execute(email, AccountRole.ADMIN)
                            }

                        exception.statusCode.value() shouldBe 404
                        exception.message shouldBe "해당 이메일에 해당하는 계정이 존재하지 않습니다."

                        verify(exactly = 1) { mockAccountJpaRepository.findByEmail(email) }
                    }
                }

                context("현재와 동일한 권한으로 변경할 때") {
                    val email = "test@gsm.hs.kr"
                    val account =
                        AccountJpaEntity().apply {
                            id = 1L
                            this.email = email
                            role = AccountRole.ADMIN
                        }

                    beforeEach {
                        every { mockAccountJpaRepository.findByEmail(email) } returns Optional.of(account)
                    }

                    it("동일한 권한으로 유지되어야 한다") {
                        modifyAccountRoleService.execute(email, AccountRole.ADMIN)

                        account.role shouldBe AccountRole.ADMIN
                        verify(exactly = 1) { mockAccountJpaRepository.findByEmail(email) }
                    }
                }
            }
        }
    })
