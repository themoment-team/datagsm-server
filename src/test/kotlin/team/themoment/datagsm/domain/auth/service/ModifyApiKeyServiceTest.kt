package team.themoment.datagsm.domain.auth.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import team.themoment.datagsm.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.domain.auth.dto.request.ModifyApiKeyReqDto
import team.themoment.datagsm.domain.auth.entity.ApiKey
import team.themoment.datagsm.domain.auth.repository.ApiKeyJpaRepository
import team.themoment.datagsm.domain.auth.service.impl.ModifyApiKeyServiceImpl
import team.themoment.datagsm.global.exception.error.ExpectedException
import team.themoment.datagsm.global.security.data.ApiKeyEnvironment
import team.themoment.datagsm.global.security.provider.CurrentUserProvider
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID

class ModifyApiKeyServiceTest :
    DescribeSpec({

        val mockApiKeyRepository = mockk<ApiKeyJpaRepository>()
        val mockCurrentUserProvider = mockk<CurrentUserProvider>()
        val mockApiKeyEnvironment = mockk<ApiKeyEnvironment>()

        val modifyApiKeyService =
            ModifyApiKeyServiceImpl(
                mockApiKeyRepository,
                mockCurrentUserProvider,
                mockApiKeyEnvironment,
            )

        afterEach {
            clearAllMocks()
        }

        describe("ModifyApiKeyService 클래스의") {
            describe("execute 메서드는") {

                val mockAccount =
                    AccountJpaEntity().apply {
                        id = 1L
                        email = "test@gsm.hs.kr"
                    }

                beforeEach {
                    every { mockCurrentUserProvider.getCurrentAccount() } returns mockAccount
                    every { mockApiKeyEnvironment.expirationDays } returns 30L
                    every { mockApiKeyEnvironment.renewalPeriodDays } returns 15L
                }

                context("API 키를 찾을 수 없을 때") {
                    val reqDto =
                        ModifyApiKeyReqDto(
                            scopes = setOf("student:read"),
                            description = "테스트",
                        )

                    beforeEach {
                        every { mockApiKeyRepository.findByAccount(mockAccount) } returns Optional.empty()
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                modifyApiKeyService.execute(reqDto)
                            }

                        exception.statusCode.value() shouldBe 404
                        exception.message shouldBe "API 키를 찾을 수 없습니다."

                        verify(exactly = 1) { mockCurrentUserProvider.getCurrentAccount() }
                        verify(exactly = 1) { mockApiKeyRepository.findByAccount(mockAccount) }
                        verify(exactly = 0) { mockApiKeyRepository.save(any()) }
                        verify(exactly = 0) { mockApiKeyRepository.delete(any()) }
                    }
                }

                context("갱신 기간 이전일 때") {
                    val reqDto =
                        ModifyApiKeyReqDto(
                            scopes = setOf("student:read"),
                            description = "갱신 테스트",
                        )
                    val now = LocalDateTime.now()
                    val expiresAt = now.plusDays(20)
                    val apiKey =
                        ApiKey().apply {
                            id = 1L
                            value = UUID.randomUUID()
                            account = mockAccount
                            createdAt = now.minusDays(10)
                            updatedAt = now.minusDays(10)
                            this.expiresAt = expiresAt
                        }

                    beforeEach {
                        every { mockApiKeyRepository.findByAccount(mockAccount) } returns Optional.of(apiKey)
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                modifyApiKeyService.execute(reqDto)
                            }

                        exception.statusCode.value() shouldBe 400
                        exception.message shouldBe "API 키 갱신 기간이 아닙니다. 만료 15일 전부터 만료 15일 후까지만 갱신 가능합니다."

                        verify(exactly = 1) { mockApiKeyRepository.findByAccount(mockAccount) }
                        verify(exactly = 0) { mockApiKeyRepository.save(any()) }
                        verify(exactly = 0) { mockApiKeyRepository.delete(any()) }
                    }
                }

                context("갱신 기간이 지났을 때") {
                    val reqDto =
                        ModifyApiKeyReqDto(
                            scopes = setOf("club:read"),
                            description = null,
                        )
                    val now = LocalDateTime.now()
                    val expiresAt = now.minusDays(20)
                    val apiKey =
                        ApiKey().apply {
                            id = 1L
                            value = UUID.randomUUID()
                            account = mockAccount
                            createdAt = now.minusDays(50)
                            updatedAt = now.minusDays(50)
                            this.expiresAt = expiresAt
                        }

                    beforeEach {
                        every { mockApiKeyRepository.findByAccount(mockAccount) } returns Optional.of(apiKey)
                        every { mockApiKeyRepository.delete(apiKey) } returns Unit
                    }

                    it("API 키가 삭제되고 410 GONE 예외가 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                modifyApiKeyService.execute(reqDto)
                            }

                        exception.statusCode.value() shouldBe 410
                        exception.message shouldBe "API 키 갱신 기간이 지났습니다. 해당 API 키는 삭제되었습니다."

                        verify(exactly = 1) { mockApiKeyRepository.findByAccount(mockAccount) }
                        verify(exactly = 1) { mockApiKeyRepository.delete(apiKey) }
                        verify(exactly = 0) { mockApiKeyRepository.save(any()) }
                    }
                }

                context("정상적으로 갱신할 때 (만료 15일 전)") {
                    val reqDto =
                        ModifyApiKeyReqDto(
                            scopes = setOf("student:read", "club:read"),
                            description = "갱신된 API 키",
                        )
                    val now = LocalDateTime.now()
                    val expiresAt = now.plusDays(10)
                    val oldApiKeyValue = UUID.randomUUID()
                    val apiKey =
                        ApiKey().apply {
                            id = 1L
                            value = oldApiKeyValue
                            account = mockAccount
                            createdAt = now.minusDays(20)
                            updatedAt = now.minusDays(20)
                            this.expiresAt = expiresAt
                        }

                    beforeEach {
                        every { mockApiKeyRepository.findByAccount(mockAccount) } returns Optional.of(apiKey)
                        every { mockApiKeyRepository.save(apiKey) } returns apiKey
                    }

                    it("기존 API 키를 유지하고 만료일자 및 scope를 갱신해야 한다") {
                        val result = modifyApiKeyService.execute(reqDto)

                        result.apiKey shouldBe oldApiKeyValue
                        result.expiresAt shouldNotBe null
                        result.scopes shouldBe reqDto.scopes
                        result.description shouldBe reqDto.description

                        verify(exactly = 1) { mockApiKeyRepository.findByAccount(mockAccount) }
                        verify(exactly = 1) { mockApiKeyRepository.save(apiKey) }
                        verify(exactly = 0) { mockApiKeyRepository.delete(any()) }
                    }
                }

                context("정상적으로 갱신할 때 (만료 후 5일)") {
                    val reqDto =
                        ModifyApiKeyReqDto(
                            scopes = setOf("project:read"),
                            description = "만료 후 갱신",
                        )
                    val now = LocalDateTime.now()
                    val expiresAt = now.minusDays(5)
                    val oldApiKeyValue = UUID.randomUUID()
                    val apiKey =
                        ApiKey().apply {
                            id = 1L
                            value = oldApiKeyValue
                            account = mockAccount
                            createdAt = now.minusDays(35)
                            updatedAt = now.minusDays(35)
                            this.expiresAt = expiresAt
                        }

                    beforeEach {
                        every { mockApiKeyRepository.findByAccount(mockAccount) } returns Optional.of(apiKey)
                        every { mockApiKeyRepository.save(apiKey) } returns apiKey
                    }

                    it("기존 API 키를 유지하고 만료일자 및 scope를 갱신해야 한다") {
                        val result = modifyApiKeyService.execute(reqDto)

                        result.apiKey shouldBe oldApiKeyValue
                        apiKey.value shouldBe oldApiKeyValue
                        apiKey.expiresAt shouldNotBe expiresAt

                        verify(exactly = 1) { mockApiKeyRepository.findByAccount(mockAccount) }
                        verify(exactly = 1) { mockApiKeyRepository.save(apiKey) }
                        verify(exactly = 0) { mockApiKeyRepository.delete(any()) }
                    }
                }

                context("갱신 기간 경계값 테스트 - 정확히 만료 15일 전") {
                    val reqDto =
                        ModifyApiKeyReqDto(
                            scopes = setOf("student:read"),
                            description = "경계값 테스트",
                        )
                    val now = LocalDateTime.now()
                    val expiresAt = now.plusDays(15)
                    val apiKey =
                        ApiKey().apply {
                            id = 1L
                            value = UUID.randomUUID()
                            account = mockAccount
                            createdAt = now.minusDays(15)
                            updatedAt = now.minusDays(15)
                            this.expiresAt = expiresAt
                        }

                    beforeEach {
                        every { mockApiKeyRepository.findByAccount(mockAccount) } returns Optional.of(apiKey)
                        every { mockApiKeyRepository.save(apiKey) } returns apiKey
                    }

                    it("갱신이 가능해야 한다") {
                        val result = modifyApiKeyService.execute(reqDto)

                        result.apiKey shouldNotBe null

                        verify(exactly = 1) { mockApiKeyRepository.save(apiKey) }
                        verify(exactly = 0) { mockApiKeyRepository.delete(any()) }
                    }
                }

                context("갱신 기간 경계값 테스트 - 정확히 만료 15일 후") {
                    val reqDto =
                        ModifyApiKeyReqDto(
                            scopes = setOf("club:read"),
                            description = "경계값 테스트2",
                        )
                    val now = LocalDateTime.now()
                    val expiresAt = now.minusDays(15)
                    val apiKey =
                        ApiKey().apply {
                            id = 1L
                            value = UUID.randomUUID()
                            account = mockAccount
                            createdAt = now.minusDays(45)
                            updatedAt = now.minusDays(45)
                            this.expiresAt = expiresAt
                        }

                    beforeEach {
                        every { mockApiKeyRepository.findByAccount(mockAccount) } returns Optional.of(apiKey)
                        every { mockApiKeyRepository.delete(apiKey) } returns Unit
                    }

                    it("갱신 기간이 지나 삭제되어야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                modifyApiKeyService.execute(reqDto)
                            }

                        exception.statusCode.value() shouldBe 410

                        verify(exactly = 1) { mockApiKeyRepository.delete(apiKey) }
                        verify(exactly = 0) { mockApiKeyRepository.save(any()) }
                    }
                }
            }
        }
    })
