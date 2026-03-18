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
import team.themoment.datagsm.common.domain.application.dto.request.ModifyThirdPartyScopeReqDto
import team.themoment.datagsm.common.domain.application.entity.ApplicationJpaEntity
import team.themoment.datagsm.common.domain.application.entity.ThirdPartyScopeJpaEntity
import team.themoment.datagsm.common.domain.application.repository.ThirdPartyScopeJpaRepository
import team.themoment.datagsm.web.domain.application.service.impl.ModifyThirdPartyScopeServiceImpl
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException
import java.util.Optional

class ModifyThirdPartyScopeServiceTest :
    DescribeSpec({

        val mockThirdPartyScopeJpaRepository = mockk<ThirdPartyScopeJpaRepository>()
        val mockCurrentUserProvider = mockk<CurrentUserProvider>()

        val service =
            ModifyThirdPartyScopeServiceImpl(
                mockThirdPartyScopeJpaRepository,
                mockCurrentUserProvider,
            )

        afterEach {
            clearAllMocks()
        }

        describe("ModifyThirdPartyScopeService 클래스의") {
            describe("execute 메서드는") {

                val applicationId = "app-uuid-1234"
                val scopeId = 10L

                val ownerAccount =
                    AccountJpaEntity().apply {
                        id = 1L
                        email = "owner@gsm.hs.kr"
                        role = AccountRole.USER
                    }

                val application =
                    ApplicationJpaEntity().apply {
                        id = applicationId
                        name = "My Application"
                        account = ownerAccount
                    }

                val scope =
                    ThirdPartyScopeJpaEntity().apply {
                        id = scopeId
                        scopeName = "profile"
                        description = "사용자 프로필 정보 조회"
                        this.application = application
                    }

                beforeEach {
                    application.thirdPartyScopes.clear()
                    application.thirdPartyScopes.add(scope)
                }

                context("소유자가 스코프를 수정할 때") {
                    val reqDto =
                        ModifyThirdPartyScopeReqDto(
                            scopeName = "email",
                            description = "이메일 주소 조회",
                        )

                    beforeEach {
                        every { mockThirdPartyScopeJpaRepository.findById(scopeId) } returns Optional.of(scope)
                        every {
                            mockThirdPartyScopeJpaRepository.findByApplicationIdAndScopeName(applicationId, reqDto.scopeName)
                        } returns null
                        every { mockCurrentUserProvider.getCurrentAccount() } returns ownerAccount
                    }

                    it("스코프가 수정된 Application이 반환되어야 한다") {
                        val result = service.execute(applicationId, scopeId, reqDto)

                        result.scopes[0].scopeName shouldBe "email"
                        result.scopes[0].description shouldBe "이메일 주소 조회"

                        verify(exactly = 1) { mockThirdPartyScopeJpaRepository.findById(scopeId) }
                        verify(exactly = 1) { mockCurrentUserProvider.getCurrentAccount() }
                    }
                }

                context("ADMIN이 다른 사용자의 스코프를 수정할 때") {
                    val adminAccount =
                        AccountJpaEntity().apply {
                            id = 99L
                            email = "admin@gsm.hs.kr"
                            role = AccountRole.ADMIN
                        }

                    val reqDto =
                        ModifyThirdPartyScopeReqDto(
                            scopeName = "phone",
                            description = "전화번호 조회",
                        )

                    beforeEach {
                        every { mockThirdPartyScopeJpaRepository.findById(scopeId) } returns Optional.of(scope)
                        every {
                            mockThirdPartyScopeJpaRepository.findByApplicationIdAndScopeName(applicationId, reqDto.scopeName)
                        } returns null
                        every { mockCurrentUserProvider.getCurrentAccount() } returns adminAccount
                    }

                    it("성공적으로 수정되어야 한다") {
                        val result = service.execute(applicationId, scopeId, reqDto)

                        result.scopes[0].scopeName shouldBe "phone"
                    }
                }

                context("ROOT가 다른 사용자의 스코프를 수정할 때") {
                    val rootAccount =
                        AccountJpaEntity().apply {
                            id = 100L
                            email = "root@gsm.hs.kr"
                            role = AccountRole.ROOT
                        }

                    val reqDto =
                        ModifyThirdPartyScopeReqDto(
                            scopeName = "address",
                            description = "주소 조회",
                        )

                    beforeEach {
                        every { mockThirdPartyScopeJpaRepository.findById(scopeId) } returns Optional.of(scope)
                        every {
                            mockThirdPartyScopeJpaRepository.findByApplicationIdAndScopeName(applicationId, reqDto.scopeName)
                        } returns null
                        every { mockCurrentUserProvider.getCurrentAccount() } returns rootAccount
                    }

                    it("성공적으로 수정되어야 한다") {
                        val result = service.execute(applicationId, scopeId, reqDto)

                        result.scopes[0].scopeName shouldBe "address"
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
                        ModifyThirdPartyScopeReqDto(
                            scopeName = "profile",
                            description = "사용자 프로필 정보 조회",
                        )

                    beforeEach {
                        every { mockThirdPartyScopeJpaRepository.findById(scopeId) } returns Optional.of(scope)
                        every {
                            mockThirdPartyScopeJpaRepository.findByApplicationIdAndScopeName(applicationId, reqDto.scopeName)
                        } returns null
                        every { mockCurrentUserProvider.getCurrentAccount() } returns otherAccount
                    }

                    it("403 FORBIDDEN 예외가 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                service.execute(applicationId, scopeId, reqDto)
                            }

                        exception.statusCode shouldBe HttpStatus.FORBIDDEN
                        exception.message shouldBe "ThirdPartyScope 수정 권한이 없습니다."
                    }
                }

                context("이미 동일한 scopeName이 존재할 때") {
                    val duplicateScopeName = "profile"
                    val reqDto =
                        ModifyThirdPartyScopeReqDto(
                            scopeName = duplicateScopeName,
                            description = "변경된 설명",
                        )

                    val existingScope =
                        ThirdPartyScopeJpaEntity().apply {
                            id = 20L
                            scopeName = duplicateScopeName
                            description = "기존 스코프"
                            this.application = application
                        }

                    beforeEach {
                        every { mockThirdPartyScopeJpaRepository.findById(scopeId) } returns Optional.of(scope)
                        every {
                            mockThirdPartyScopeJpaRepository.findByApplicationIdAndScopeName(applicationId, duplicateScopeName)
                        } returns existingScope
                    }

                    it("409 CONFLICT 예외가 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                service.execute(applicationId, scopeId, reqDto)
                            }

                        exception.statusCode shouldBe HttpStatus.CONFLICT
                        exception.message shouldBe "${duplicateScopeName}은 이미 사용 중인 권한 범위 명칭입니다."
                    }
                }

                context("존재하지 않는 scopeId로 수정을 시도할 때") {
                    val nonExistingScopeId = 999L
                    val reqDto =
                        ModifyThirdPartyScopeReqDto(
                            scopeName = "profile",
                            description = "사용자 프로필 정보 조회",
                        )

                    beforeEach {
                        every { mockThirdPartyScopeJpaRepository.findById(nonExistingScopeId) } returns Optional.empty()
                    }

                    it("404 NOT_FOUND 예외가 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                service.execute(applicationId, nonExistingScopeId, reqDto)
                            }

                        exception.statusCode shouldBe HttpStatus.NOT_FOUND
                        exception.message shouldBe "ThirdPartyScope를 찾을 수 없습니다."
                    }
                }

                context("scopeId는 존재하지만 다른 Application의 스코프일 때") {
                    val otherApplication =
                        ApplicationJpaEntity().apply {
                            id = "other-app-id"
                            name = "Other Application"
                            account = ownerAccount
                        }

                    val scopeOfOtherApp =
                        ThirdPartyScopeJpaEntity().apply {
                            id = scopeId
                            scopeName = "profile"
                            description = "사용자 프로필 정보 조회"
                            this.application = otherApplication
                        }

                    val reqDto =
                        ModifyThirdPartyScopeReqDto(
                            scopeName = "email",
                            description = "이메일 주소 조회",
                        )

                    beforeEach {
                        every { mockThirdPartyScopeJpaRepository.findById(scopeId) } returns Optional.of(scopeOfOtherApp)
                    }

                    it("404 NOT_FOUND 예외가 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                service.execute(applicationId, scopeId, reqDto)
                            }

                        exception.statusCode shouldBe HttpStatus.NOT_FOUND
                        exception.message shouldBe "ThirdPartyScope를 찾을 수 없습니다."
                    }
                }
            }
        }
    })
