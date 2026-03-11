package team.themoment.datagsm.web.domain.application.service

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.common.domain.application.dto.request.CreateApplicationReqDto
import team.themoment.datagsm.common.domain.application.entity.ApplicationJpaEntity
import team.themoment.datagsm.common.domain.application.repository.ApplicationJpaRepository
import team.themoment.datagsm.web.domain.application.service.impl.CreateApplicationServiceImpl
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider

class CreateApplicationServiceTest :
    DescribeSpec({

        val mockApplicationJpaRepository = mockk<ApplicationJpaRepository>()
        val mockCurrentUserProvider = mockk<CurrentUserProvider>()

        val service =
            CreateApplicationServiceImpl(
                mockApplicationJpaRepository,
                mockCurrentUserProvider,
            )

        afterEach {
            clearAllMocks()
        }

        describe("CreateApplicationService 클래스의") {
            describe("execute 메서드는") {

                val ownerAccount =
                    AccountJpaEntity().apply {
                        id = 1L
                        email = "owner@gsm.hs.kr"
                        role = AccountRole.USER
                    }

                context("스코프 없이 Application을 생성할 때") {
                    val reqDto =
                        CreateApplicationReqDto(
                            name = "My Application",
                            scopes = emptyList(),
                        )

                    beforeEach {
                        every { mockCurrentUserProvider.getCurrentAccount() } returns ownerAccount
                        every { mockApplicationJpaRepository.save(any()) } answers { firstArg() }
                    }

                    it("Application이 성공적으로 생성되어야 한다") {
                        val result = service.execute(reqDto)

                        result.name shouldBe "My Application"
                        result.accountId shouldBe 1L
                        result.scopes shouldBe emptyList()
                        result.id shouldNotBe null

                        verify(exactly = 1) { mockCurrentUserProvider.getCurrentAccount() }
                        verify(exactly = 1) { mockApplicationJpaRepository.save(any()) }
                    }
                }

                context("스코프를 포함하여 Application을 생성할 때") {
                    val reqDto =
                        CreateApplicationReqDto(
                            name = "My Application",
                            scopes =
                                listOf(
                                    CreateApplicationReqDto.ScopeReqDto(
                                        scopeName = "profile",
                                        description = "사용자 프로필 정보 조회",
                                    ),
                                    CreateApplicationReqDto.ScopeReqDto(
                                        scopeName = "email",
                                        description = "이메일 주소 조회",
                                    ),
                                ),
                        )

                    val savedApplicationSlot = slot<ApplicationJpaEntity>()

                    beforeEach {
                        every { mockCurrentUserProvider.getCurrentAccount() } returns ownerAccount
                        every { mockApplicationJpaRepository.save(capture(savedApplicationSlot)) } answers {
                            val app = firstArg<ApplicationJpaEntity>()
                            app.thirdPartyScopes.forEachIndexed { index, scope ->
                                if (scope.id == null) scope.id = (index + 1).toLong()
                            }
                            app
                        }
                    }

                    it("스코프가 포함된 Application이 생성되어야 한다") {
                        val result = service.execute(reqDto)

                        result.scopes.size shouldBe 2
                        result.scopes[0].scopeName shouldBe "profile"
                        result.scopes[0].description shouldBe "사용자 프로필 정보 조회"
                        result.scopes[1].scopeName shouldBe "email"
                        result.scopes[1].description shouldBe "이메일 주소 조회"

                        savedApplicationSlot.captured.thirdPartyScopes.size shouldBe 2
                    }
                }

                context("Application을 생성할 때") {
                    val reqDto =
                        CreateApplicationReqDto(
                            name = "UUID Test Application",
                            scopes = emptyList(),
                        )

                    beforeEach {
                        every { mockCurrentUserProvider.getCurrentAccount() } returns ownerAccount
                        every { mockApplicationJpaRepository.save(any()) } answers { firstArg() }
                    }

                    it("Application ID가 UUID 형식으로 생성되어야 한다") {
                        val result = service.execute(reqDto)

                        val uuidRegex = Regex("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")
                        uuidRegex.matches(result.id) shouldBe true
                    }
                }

                context("Application을 생성할 때 현재 사용자가 소유자로 연결될 때") {
                    val reqDto =
                        CreateApplicationReqDto(
                            name = "My Application",
                            scopes = emptyList(),
                        )

                    val savedApplicationSlot = slot<ApplicationJpaEntity>()

                    beforeEach {
                        every { mockCurrentUserProvider.getCurrentAccount() } returns ownerAccount
                        every { mockApplicationJpaRepository.save(capture(savedApplicationSlot)) } answers { firstArg() }
                    }

                    it("현재 사용자의 Account가 Application에 연결되어야 한다") {
                        service.execute(reqDto)

                        savedApplicationSlot.captured.account shouldBe ownerAccount
                        savedApplicationSlot.captured.account.id shouldBe 1L
                        savedApplicationSlot.captured.account.email shouldBe "owner@gsm.hs.kr"

                        verify(exactly = 1) { mockCurrentUserProvider.getCurrentAccount() }
                    }
                }

                context("스코프의 application 역참조가 올바르게 설정될 때") {
                    val reqDto =
                        CreateApplicationReqDto(
                            name = "My Application",
                            scopes =
                                listOf(
                                    CreateApplicationReqDto.ScopeReqDto(
                                        scopeName = "profile",
                                        description = "사용자 프로필 정보 조회",
                                    ),
                                ),
                        )

                    val savedApplicationSlot = slot<ApplicationJpaEntity>()

                    beforeEach {
                        every { mockCurrentUserProvider.getCurrentAccount() } returns ownerAccount
                        every { mockApplicationJpaRepository.save(capture(savedApplicationSlot)) } answers {
                            val app = firstArg<ApplicationJpaEntity>()
                            app.thirdPartyScopes.forEachIndexed { index, scope ->
                                if (scope.id == null) scope.id = (index + 1).toLong()
                            }
                            app
                        }
                    }

                    it("각 스코프의 application 참조가 올바르게 설정되어야 한다") {
                        shouldNotThrowAny { service.execute(reqDto) }

                        val savedApp = savedApplicationSlot.captured
                        savedApp.thirdPartyScopes[0].application shouldBe savedApp
                    }
                }
            }
        }
    })
