package team.themoment.datagsm.web.domain.application.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.http.HttpStatus
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.common.domain.application.entity.ApplicationJpaEntity
import team.themoment.datagsm.common.domain.application.repository.ApplicationJpaRepository
import team.themoment.datagsm.web.domain.application.service.impl.DeleteApplicationServiceImpl
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException
import java.util.Optional

class DeleteApplicationServiceTest :
    DescribeSpec({

        val mockApplicationJpaRepository = mockk<ApplicationJpaRepository>()
        val mockCurrentUserProvider = mockk<CurrentUserProvider>()

        val service =
            DeleteApplicationServiceImpl(
                mockApplicationJpaRepository,
                mockCurrentUserProvider,
            )

        afterEach {
            clearAllMocks()
        }

        describe("DeleteApplicationService 클래스의") {
            describe("execute 메서드는") {

                val applicationId = "app-uuid-1234"

                val ownerAccount =
                    AccountJpaEntity().apply {
                        id = 1L
                        email = "owner@gsm.hs.kr"
                        role = AccountRole.USER
                    }

                val existingApplication =
                    ApplicationJpaEntity().apply {
                        id = applicationId
                        name = "My Application"
                        account = ownerAccount
                    }

                context("소유자가 Application을 삭제할 때") {
                    beforeEach {
                        every { mockApplicationJpaRepository.findById(applicationId) } returns Optional.of(existingApplication)
                        every { mockCurrentUserProvider.getCurrentAccount() } returns ownerAccount
                        every { mockApplicationJpaRepository.delete(existingApplication) } returns Unit
                    }

                    it("Application이 성공적으로 삭제되어야 한다") {
                        service.execute(applicationId)

                        verify(exactly = 1) { mockApplicationJpaRepository.findById(applicationId) }
                        verify(exactly = 1) { mockCurrentUserProvider.getCurrentAccount() }
                        verify(exactly = 1) { mockApplicationJpaRepository.delete(existingApplication) }
                    }
                }

                context("ADMIN이 다른 사용자의 Application을 삭제할 때") {
                    val adminAccount =
                        AccountJpaEntity().apply {
                            id = 99L
                            email = "admin@gsm.hs.kr"
                            role = AccountRole.ADMIN
                        }

                    beforeEach {
                        every { mockApplicationJpaRepository.findById(applicationId) } returns Optional.of(existingApplication)
                        every { mockCurrentUserProvider.getCurrentAccount() } returns adminAccount
                        every { mockApplicationJpaRepository.delete(existingApplication) } returns Unit
                    }

                    it("성공적으로 삭제되어야 한다") {
                        service.execute(applicationId)

                        verify(exactly = 1) { mockApplicationJpaRepository.delete(existingApplication) }
                    }
                }

                context("ROOT가 다른 사용자의 Application을 삭제할 때") {
                    val rootAccount =
                        AccountJpaEntity().apply {
                            id = 100L
                            email = "root@gsm.hs.kr"
                            role = AccountRole.ROOT
                        }

                    beforeEach {
                        every { mockApplicationJpaRepository.findById(applicationId) } returns Optional.of(existingApplication)
                        every { mockCurrentUserProvider.getCurrentAccount() } returns rootAccount
                        every { mockApplicationJpaRepository.delete(existingApplication) } returns Unit
                    }

                    it("성공적으로 삭제되어야 한다") {
                        service.execute(applicationId)

                        verify(exactly = 1) { mockApplicationJpaRepository.delete(existingApplication) }
                    }
                }

                context("소유자가 아닌 일반 사용자가 삭제를 시도할 때") {
                    val otherAccount =
                        AccountJpaEntity().apply {
                            id = 2L
                            email = "other@gsm.hs.kr"
                            role = AccountRole.USER
                        }

                    beforeEach {
                        every { mockApplicationJpaRepository.findById(applicationId) } returns Optional.of(existingApplication)
                        every { mockCurrentUserProvider.getCurrentAccount() } returns otherAccount
                    }

                    it("403 FORBIDDEN 예외가 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                service.execute(applicationId)
                            }

                        exception.statusCode shouldBe HttpStatus.FORBIDDEN
                        exception.message shouldBe "Application 삭제 권한이 없습니다."

                        verify(exactly = 0) { mockApplicationJpaRepository.delete(any()) }
                    }
                }

                context("존재하지 않는 Application ID로 삭제를 시도할 때") {
                    val nonExistingId = "non-existing-id"

                    beforeEach {
                        every { mockApplicationJpaRepository.findById(nonExistingId) } returns Optional.empty()
                    }

                    it("404 NOT_FOUND 예외가 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                service.execute(nonExistingId)
                            }

                        exception.statusCode shouldBe HttpStatus.NOT_FOUND
                        exception.message shouldBe "Application을 찾을 수 없습니다."

                        verify(exactly = 0) { mockApplicationJpaRepository.delete(any()) }
                    }
                }
            }
        }
    })
