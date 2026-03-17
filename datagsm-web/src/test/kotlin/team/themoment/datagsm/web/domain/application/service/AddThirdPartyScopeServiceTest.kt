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
import team.themoment.datagsm.common.domain.application.dto.request.AddThirdPartyScopeReqDto
import team.themoment.datagsm.common.domain.application.entity.ApplicationJpaEntity
import team.themoment.datagsm.common.domain.application.entity.ThirdPartyScopeJpaEntity
import team.themoment.datagsm.common.domain.application.repository.ApplicationJpaRepository
import team.themoment.datagsm.common.domain.application.repository.ThirdPartyScopeJpaRepository
import team.themoment.datagsm.web.domain.application.service.impl.AddThirdPartyScopeServiceImpl
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException
import java.util.Optional

class AddThirdPartyScopeServiceTest :
    DescribeSpec({

        val mockApplicationJpaRepository = mockk<ApplicationJpaRepository>()
        val mockThirdPartyScopeJpaRepository = mockk<ThirdPartyScopeJpaRepository>()
        val mockCurrentUserProvider = mockk<CurrentUserProvider>()

        val service =
            AddThirdPartyScopeServiceImpl(
                mockApplicationJpaRepository,
                mockThirdPartyScopeJpaRepository,
                mockCurrentUserProvider,
            )

        afterEach {
            clearAllMocks()
        }

        describe("AddThirdPartyScopeService 클래스의") {
            describe("execute 메서드는") {

                val applicationId = "app-uuid-1234"

                val ownerAccount =
                    AccountJpaEntity().apply {
                        id = 1L
                        email = "owner@gsm.hs.kr"
                        role = AccountRole.USER
                    }

                lateinit var existingApplication: ApplicationJpaEntity

                beforeEach {
                    existingApplication =
                        ApplicationJpaEntity().apply {
                            id = applicationId
                            name = "My Application"
                            account = ownerAccount
                        }
                }

                context("소유자가 스코프를 추가할 때") {
                    val reqDto =
                        AddThirdPartyScopeReqDto(
                            scopeName = "profile",
                            description = "사용자 프로필 정보 조회",
                        )

                    beforeEach {
                        every { mockApplicationJpaRepository.findById(applicationId) } returns Optional.of(existingApplication)
                        every { mockCurrentUserProvider.getCurrentAccount() } returns ownerAccount
                        every {
                            mockThirdPartyScopeJpaRepository.findByApplicationIdAndScopeName(applicationId, reqDto.scopeName)
                        } returns null
                        every { mockThirdPartyScopeJpaRepository.save(any()) } answers {
                            firstArg<ThirdPartyScopeJpaEntity>().apply { id = 1L }
                        }
                    }

                    it("스코프가 추가된 Application이 반환되어야 한다") {
                        val result = service.execute(applicationId, reqDto)

                        result.scopes.size shouldBe 1
                        result.scopes[0].scopeName shouldBe "profile"
                        result.scopes[0].description shouldBe "사용자 프로필 정보 조회"

                        verify(exactly = 1) { mockApplicationJpaRepository.findById(applicationId) }
                        verify(exactly = 1) { mockCurrentUserProvider.getCurrentAccount() }
                        verify(exactly = 1) { mockThirdPartyScopeJpaRepository.save(any()) }
                    }
                }

                context("ADMIN이 다른 사용자의 Application에 스코프를 추가할 때") {
                    val adminAccount =
                        AccountJpaEntity().apply {
                            id = 99L
                            email = "admin@gsm.hs.kr"
                            role = AccountRole.ADMIN
                        }

                    val reqDto =
                        AddThirdPartyScopeReqDto(
                            scopeName = "email",
                            description = "이메일 주소 조회",
                        )

                    beforeEach {
                        every { mockApplicationJpaRepository.findById(applicationId) } returns Optional.of(existingApplication)
                        every { mockCurrentUserProvider.getCurrentAccount() } returns adminAccount
                        every {
                            mockThirdPartyScopeJpaRepository.findByApplicationIdAndScopeName(applicationId, reqDto.scopeName)
                        } returns null
                        every { mockThirdPartyScopeJpaRepository.save(any()) } answers {
                            firstArg<ThirdPartyScopeJpaEntity>().apply { id = 2L }
                        }
                    }

                    it("성공적으로 추가되어야 한다") {
                        val result = service.execute(applicationId, reqDto)

                        result.scopes.size shouldBe 1
                        result.scopes[0].scopeName shouldBe "email"
                    }
                }

                context("ROOT가 다른 사용자의 Application에 스코프를 추가할 때") {
                    val rootAccount =
                        AccountJpaEntity().apply {
                            id = 100L
                            email = "root@gsm.hs.kr"
                            role = AccountRole.ROOT
                        }

                    val reqDto =
                        AddThirdPartyScopeReqDto(
                            scopeName = "phone",
                            description = "전화번호 조회",
                        )

                    beforeEach {
                        every { mockApplicationJpaRepository.findById(applicationId) } returns Optional.of(existingApplication)
                        every { mockCurrentUserProvider.getCurrentAccount() } returns rootAccount
                        every {
                            mockThirdPartyScopeJpaRepository.findByApplicationIdAndScopeName(applicationId, reqDto.scopeName)
                        } returns null
                        every { mockThirdPartyScopeJpaRepository.save(any()) } answers {
                            firstArg<ThirdPartyScopeJpaEntity>().apply { id = 3L }
                        }
                    }

                    it("성공적으로 추가되어야 한다") {
                        val result = service.execute(applicationId, reqDto)

                        result.scopes.size shouldBe 1
                    }
                }

                context("이미 동일한 scopeName이 존재할 때") {
                    val reqDto =
                        AddThirdPartyScopeReqDto(
                            scopeName = "profile",
                            description = "사용자 프로필 정보 조회",
                        )

                    val existingScope =
                        ThirdPartyScopeJpaEntity().apply {
                            id = 10L
                            scopeName = "profile"
                            description = "기존 프로필 스코프"
                            application = existingApplication
                        }

                    beforeEach {
                        every { mockApplicationJpaRepository.findById(applicationId) } returns Optional.of(existingApplication)
                        every { mockCurrentUserProvider.getCurrentAccount() } returns ownerAccount
                        every {
                            mockThirdPartyScopeJpaRepository.findByApplicationIdAndScopeName(applicationId, reqDto.scopeName)
                        } returns existingScope
                    }

                    it("409 CONFLICT 예외가 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                service.execute(applicationId, reqDto)
                            }

                        exception.statusCode shouldBe HttpStatus.CONFLICT
                        verify(exactly = 0) { mockThirdPartyScopeJpaRepository.save(any()) }
                    }
                }

                context("소유자가 아닌 일반 사용자가 스코프 추가를 시도할 때") {
                    val otherAccount =
                        AccountJpaEntity().apply {
                            id = 2L
                            email = "other@gsm.hs.kr"
                            role = AccountRole.USER
                        }

                    val reqDto =
                        AddThirdPartyScopeReqDto(
                            scopeName = "profile",
                            description = "사용자 프로필 정보 조회",
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
                        exception.message shouldBe "ThirdPartyScope 추가 권한이 없습니다."

                        verify(exactly = 0) { mockThirdPartyScopeJpaRepository.save(any()) }
                    }
                }

                context("존재하지 않는 Application ID로 스코프 추가를 시도할 때") {
                    val nonExistingId = "non-existing-id"
                    val reqDto =
                        AddThirdPartyScopeReqDto(
                            scopeName = "profile",
                            description = "사용자 프로필 정보 조회",
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

                        verify(exactly = 0) { mockThirdPartyScopeJpaRepository.save(any()) }
                    }
                }
            }
        }
    })
