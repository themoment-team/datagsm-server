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
import team.themoment.datagsm.common.domain.application.dto.request.ModifyApplicationReqDto
import team.themoment.datagsm.common.domain.application.entity.ApplicationJpaEntity
import team.themoment.datagsm.common.domain.application.repository.ApplicationJpaRepository
import team.themoment.datagsm.web.domain.application.service.impl.ModifyApplicationServiceImpl
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException
import java.util.Optional

class ModifyApplicationServiceTest :
    DescribeSpec({

        val mockApplicationJpaRepository = mockk<ApplicationJpaRepository>()
        val mockCurrentUserProvider = mockk<CurrentUserProvider>()

        val service =
            ModifyApplicationServiceImpl(
                mockApplicationJpaRepository,
                mockCurrentUserProvider,
            )

        afterEach {
            clearAllMocks()
        }

        describe("ModifyApplicationService 클래스의") {
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
                        name = "Old Name"
                        account = ownerAccount
                    }

                context("소유자가 Application 이름을 수정할 때") {
                    val reqDto = ModifyApplicationReqDto(name = "New Name")

                    beforeEach {
                        every { mockApplicationJpaRepository.findById(applicationId) } returns Optional.of(existingApplication)
                        every { mockCurrentUserProvider.getCurrentAccount() } returns ownerAccount
                    }

                    it("Application 이름이 수정되어야 한다") {
                        val result = service.execute(applicationId, reqDto)

                        result.name shouldBe "New Name"

                        verify(exactly = 1) { mockApplicationJpaRepository.findById(applicationId) }
                        verify(exactly = 1) { mockCurrentUserProvider.getCurrentAccount() }
                    }
                }

                context("ADMIN이 다른 사용자의 Application 이름을 수정할 때") {
                    val adminAccount =
                        AccountJpaEntity().apply {
                            id = 99L
                            email = "admin@gsm.hs.kr"
                            role = AccountRole.ADMIN
                        }

                    val reqDto = ModifyApplicationReqDto(name = "Admin Modified Name")

                    beforeEach {
                        every { mockApplicationJpaRepository.findById(applicationId) } returns Optional.of(existingApplication)
                        every { mockCurrentUserProvider.getCurrentAccount() } returns adminAccount
                    }

                    it("성공적으로 수정되어야 한다") {
                        val result = service.execute(applicationId, reqDto)

                        result.name shouldBe "Admin Modified Name"
                    }
                }

                context("ROOT가 다른 사용자의 Application 이름을 수정할 때") {
                    val rootAccount =
                        AccountJpaEntity().apply {
                            id = 100L
                            email = "root@gsm.hs.kr"
                            role = AccountRole.ROOT
                        }

                    val reqDto = ModifyApplicationReqDto(name = "Root Modified Name")

                    beforeEach {
                        every { mockApplicationJpaRepository.findById(applicationId) } returns Optional.of(existingApplication)
                        every { mockCurrentUserProvider.getCurrentAccount() } returns rootAccount
                    }

                    it("성공적으로 수정되어야 한다") {
                        val result = service.execute(applicationId, reqDto)

                        result.name shouldBe "Root Modified Name"
                    }
                }

                context("소유자가 아닌 일반 사용자가 수정을 시도할 때") {
                    val otherAccount =
                        AccountJpaEntity().apply {
                            id = 2L
                            email = "other@gsm.hs.kr"
                            role = AccountRole.USER
                        }

                    val reqDto = ModifyApplicationReqDto(name = "Unauthorized Name")

                    beforeEach {
                        every { mockApplicationJpaRepository.findById(applicationId) } returns Optional.of(existingApplication)
                        every { mockCurrentUserProvider.getCurrentAccount() } returns otherAccount
                    }

                    it("403 FORBIDDEN 예외가 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                service.execute(applicationId, reqDto)
                            }

                        exception.statusCode shouldBe HttpStatus.FORBIDDEN
                        exception.message shouldBe "Application 수정 권한이 없습니다."
                    }
                }

                context("존재하지 않는 Application ID로 수정을 시도할 때") {
                    val nonExistingId = "non-existing-id"
                    val reqDto = ModifyApplicationReqDto(name = "New Name")

                    beforeEach {
                        every { mockApplicationJpaRepository.findById(nonExistingId) } returns Optional.empty()
                    }

                    it("404 NOT_FOUND 예외가 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                service.execute(nonExistingId, reqDto)
                            }

                        exception.statusCode shouldBe HttpStatus.NOT_FOUND
                        exception.message shouldBe "Application을 찾을 수 없습니다."
                    }
                }
            }
        }
    })
