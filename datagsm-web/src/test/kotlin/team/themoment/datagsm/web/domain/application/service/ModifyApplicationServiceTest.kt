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
import team.themoment.datagsm.common.domain.application.entity.ThirdPartyScopeJpaEntity
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

                val existingScope =
                    ThirdPartyScopeJpaEntity().apply {
                        id = 10L
                        scopeName = "profile"
                        description = "기존 스코프"
                    }

                val existingApplication =
                    ApplicationJpaEntity().apply {
                        id = applicationId
                        name = "Old Name"
                        account = ownerAccount
                        thirdPartyScopes = mutableListOf(existingScope)
                    }
                existingScope.application = existingApplication

                context("소유자가 Application을 수정할 때") {
                    val reqDto =
                        ModifyApplicationReqDto(
                            name = "New Name",
                            scopes =
                                listOf(
                                    ModifyApplicationReqDto.ScopeReqDto(
                                        scopeName = "email",
                                        description = "이메일 주소 조회",
                                    ),
                                ),
                        )

                    beforeEach {
                        every { mockApplicationJpaRepository.findById(applicationId) } returns Optional.of(existingApplication)
                        every { mockCurrentUserProvider.getCurrentAccount() } returns ownerAccount
                        every { mockApplicationJpaRepository.saveAndFlush(any()) } answers {
                            val app = firstArg<ApplicationJpaEntity>()
                            app.thirdPartyScopes.forEachIndexed { index, scope ->
                                if (scope.id == null) scope.id = (index + 1).toLong()
                            }
                            app
                        }
                    }

                    it("Application 이름과 스코프가 수정되어야 한다") {
                        val result = service.execute(applicationId, reqDto)

                        result.name shouldBe "New Name"
                        result.scopes.size shouldBe 1
                        result.scopes[0].scopeName shouldBe "email"
                        result.scopes[0].description shouldBe "이메일 주소 조회"

                        verify(exactly = 1) { mockApplicationJpaRepository.findById(applicationId) }
                        verify(exactly = 1) { mockCurrentUserProvider.getCurrentAccount() }
                        verify(exactly = 1) { mockApplicationJpaRepository.saveAndFlush(any()) }
                    }
                }

                context("ADMIN이 다른 사용자의 Application을 수정할 때") {
                    val adminAccount =
                        AccountJpaEntity().apply {
                            id = 99L
                            email = "admin@gsm.hs.kr"
                            role = AccountRole.ADMIN
                        }

                    val reqDto =
                        ModifyApplicationReqDto(
                            name = "Admin Modified Name",
                            scopes = emptyList(),
                        )

                    beforeEach {
                        every { mockApplicationJpaRepository.findById(applicationId) } returns Optional.of(existingApplication)
                        every { mockCurrentUserProvider.getCurrentAccount() } returns adminAccount
                        every { mockApplicationJpaRepository.saveAndFlush(any()) } answers { firstArg() }
                    }

                    it("성공적으로 수정되어야 한다") {
                        val result = service.execute(applicationId, reqDto)

                        result.name shouldBe "Admin Modified Name"

                        verify(exactly = 1) { mockApplicationJpaRepository.findById(applicationId) }
                    }
                }

                context("ROOT가 다른 사용자의 Application을 수정할 때") {
                    val rootAccount =
                        AccountJpaEntity().apply {
                            id = 100L
                            email = "root@gsm.hs.kr"
                            role = AccountRole.ROOT
                        }

                    val reqDto =
                        ModifyApplicationReqDto(
                            name = "Root Modified Name",
                            scopes = emptyList(),
                        )

                    beforeEach {
                        every { mockApplicationJpaRepository.findById(applicationId) } returns Optional.of(existingApplication)
                        every { mockCurrentUserProvider.getCurrentAccount() } returns rootAccount
                        every { mockApplicationJpaRepository.saveAndFlush(any()) } answers { firstArg() }
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

                    val reqDto =
                        ModifyApplicationReqDto(
                            name = "Unauthorized Name",
                            scopes = emptyList(),
                        )

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

                        verify(exactly = 0) { mockApplicationJpaRepository.save(any()) }
                    }
                }

                context("존재하지 않는 Application ID로 수정을 시도할 때") {
                    val nonExistingId = "non-existing-id"
                    val reqDto =
                        ModifyApplicationReqDto(
                            name = "New Name",
                            scopes = emptyList(),
                        )

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

                context("스코프를 빈 목록으로 수정할 때") {
                    val reqDto =
                        ModifyApplicationReqDto(
                            name = "No Scope App",
                            scopes = emptyList(),
                        )

                    beforeEach {
                        every { mockApplicationJpaRepository.findById(applicationId) } returns Optional.of(existingApplication)
                        every { mockCurrentUserProvider.getCurrentAccount() } returns ownerAccount
                        every { mockApplicationJpaRepository.saveAndFlush(any()) } answers { firstArg() }
                    }

                    it("기존 스코프가 모두 제거되어야 한다") {
                        val result = service.execute(applicationId, reqDto)

                        result.scopes shouldBe emptyList()
                    }
                }
            }
        }
    })
