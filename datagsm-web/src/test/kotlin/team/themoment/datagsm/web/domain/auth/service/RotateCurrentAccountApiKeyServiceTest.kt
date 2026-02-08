package team.themoment.datagsm.web.domain.auth.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.common.domain.auth.entity.ApiKey
import team.themoment.datagsm.common.domain.auth.repository.ApiKeyJpaRepository
import team.themoment.datagsm.common.global.data.ApiKeyEnvironment
import team.themoment.datagsm.web.domain.auth.service.impl.RotateCurrentAccountApiKeyServiceImpl
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID

class RotateCurrentAccountApiKeyServiceTest :
    DescribeSpec({

        val mockApiKeyRepository = mockk<ApiKeyJpaRepository>()
        val mockCurrentUserProvider = mockk<CurrentUserProvider>()
        val apiKeyEnvironment =
            ApiKeyEnvironment(
                expirationDays = 30L,
                renewalPeriodDays = 15L,
                adminExpirationDays = 365L,
                rateLimit = ApiKeyEnvironment.RateLimit(true, 100L, 100L, 60L),
            )

        val rotateApiKeyService =
            RotateCurrentAccountApiKeyServiceImpl(
                mockApiKeyRepository,
                mockCurrentUserProvider,
                apiKeyEnvironment,
            )

        afterEach {
            clearAllMocks()
        }

        describe("RotateCurrentAccountApiKeyService 클래스의") {
            describe("execute 메서드는") {

                val mockAccount =
                    AccountJpaEntity().apply {
                        id = 1L
                        email = "test@gsm.hs.kr"
                        role = AccountRole.USER
                    }

                beforeEach {
                    every { mockCurrentUserProvider.getCurrentAccount() } returns mockAccount
                }

                context("API 키를 찾을 수 없을 때") {
                    beforeEach {
                        every { mockApiKeyRepository.findByAccount(mockAccount) } returns Optional.empty()
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                rotateApiKeyService.execute()
                            }

                        exception.statusCode.value() shouldBe 404
                        exception.message shouldBe "API 키를 찾을 수 없습니다."

                        verify(exactly = 1) { mockCurrentUserProvider.getCurrentAccount() }
                        verify(exactly = 1) { mockApiKeyRepository.findByAccount(mockAccount) }
                        verify(exactly = 0) { mockApiKeyRepository.save(any()) }
                        verify(exactly = 0) { mockApiKeyRepository.delete(any()) }
                    }
                }

                context("갱신 기간이 지났을 때") {
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
                            updateScopes(setOf("student:read"))
                            description = "테스트 키"
                        }

                    beforeEach {
                        every { mockApiKeyRepository.findByAccount(mockAccount) } returns Optional.of(apiKey)
                        every { mockApiKeyRepository.delete(apiKey) } returns Unit
                    }

                    it("API 키가 삭제되고 410 GONE 예외가 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                rotateApiKeyService.execute()
                            }

                        exception.statusCode.value() shouldBe 410
                        exception.message shouldBe "API 키 갱신 기간이 지났습니다. 해당 API 키는 삭제되었습니다."

                        verify(exactly = 1) { mockApiKeyRepository.findByAccount(mockAccount) }
                        verify(exactly = 1) { mockApiKeyRepository.delete(apiKey) }
                        verify(exactly = 0) { mockApiKeyRepository.save(any()) }
                    }
                }

                context("일반 사용자가 정상적으로 재발급할 때") {
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
                            this.description = "기존 설명"
                            updateScopes(setOf("student:read", "club:read"))
                        }

                    beforeEach {
                        every { mockApiKeyRepository.findByAccount(mockAccount) } returns Optional.of(apiKey)
                        every { mockApiKeyRepository.save(apiKey) } returns apiKey
                    }

                    it("새로운 UUID가 발급되고 기존 Scope와 Description을 유지하며 30일 만료기간을 설정해야 한다") {
                        val beforeExecution = LocalDateTime.now()
                        val result = rotateApiKeyService.execute()
                        val afterExecution = LocalDateTime.now()

                        result.apiKey shouldNotBe oldApiKeyValue.toString()
                        result.apiKey shouldNotBe apiKey.maskedValue
                        apiKey.value shouldNotBe oldApiKeyValue
                        result.scopes shouldBe setOf("student:read", "club:read")
                        result.description shouldBe "기존 설명"

                        val expectedMinExpiresAt = beforeExecution.plusDays(30)
                        val expectedMaxExpiresAt = afterExecution.plusDays(30)
                        result.expiresAt.isAfter(expectedMinExpiresAt.minusSeconds(1)) shouldBe true
                        result.expiresAt.isBefore(expectedMaxExpiresAt.plusSeconds(1)) shouldBe true

                        verify(exactly = 1) { mockApiKeyRepository.findByAccount(mockAccount) }
                        verify(exactly = 1) { mockApiKeyRepository.save(apiKey) }
                        verify(exactly = 0) { mockApiKeyRepository.delete(any()) }
                    }
                }

                context("관리자가 정상적으로 재발급할 때") {
                    val now = LocalDateTime.now()
                    val expiresAt = now.plusDays(20)
                    val oldApiKeyValue = UUID.randomUUID()
                    val apiKey =
                        ApiKey().apply {
                            id = 1L
                            value = oldApiKeyValue
                            account = mockAccount
                            createdAt = now.minusDays(345)
                            updatedAt = now.minusDays(345)
                            this.expiresAt = expiresAt
                            this.description = "관리자 API 키"
                            updateScopes(
                                setOf(
                                    "student:read",
                                    "student:write",
                                    "club:read",
                                    "club:write",
                                    "project:read",
                                    "project:write",
                                ),
                            )
                        }

                    beforeEach {
                        mockAccount.role = AccountRole.ADMIN
                        every { mockApiKeyRepository.findByAccount(mockAccount) } returns Optional.of(apiKey)
                        every { mockApiKeyRepository.save(apiKey) } returns apiKey
                    }

                    it("새로운 UUID가 발급되고 기존 Scope와 Description을 유지하며 365일 만료기간을 설정해야 한다") {
                        val beforeExecution = LocalDateTime.now()
                        val result = rotateApiKeyService.execute()
                        val afterExecution = LocalDateTime.now()

                        result.apiKey shouldNotBe oldApiKeyValue.toString()
                        result.apiKey shouldNotBe apiKey.maskedValue
                        apiKey.value shouldNotBe oldApiKeyValue
                        result.scopes shouldBe
                            setOf(
                                "student:read",
                                "student:write",
                                "club:read",
                                "club:write",
                                "project:read",
                                "project:write",
                            )
                        result.description shouldBe "관리자 API 키"

                        val expectedMinExpiresAt = beforeExecution.plusDays(365)
                        val expectedMaxExpiresAt = afterExecution.plusDays(365)
                        result.expiresAt.isAfter(expectedMinExpiresAt.minusSeconds(1)) shouldBe true
                        result.expiresAt.isBefore(expectedMaxExpiresAt.plusSeconds(1)) shouldBe true

                        verify(exactly = 1) { mockApiKeyRepository.save(apiKey) }
                        verify(exactly = 0) { mockApiKeyRepository.delete(any()) }
                    }
                }

                context("만료 후 갱신 기간 내에 재발급할 때") {
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
                            this.description = "만료 후 재발급"
                            updateScopes(setOf("project:read"))
                        }

                    beforeEach {
                        every { mockApiKeyRepository.findByAccount(mockAccount) } returns Optional.of(apiKey)
                        every { mockApiKeyRepository.save(apiKey) } returns apiKey
                    }

                    it("새로운 UUID가 발급되고 만료일이 재설정되어야 한다") {
                        val result = rotateApiKeyService.execute()

                        result.apiKey shouldNotBe oldApiKeyValue.toString()
                        apiKey.value shouldNotBe oldApiKeyValue
                        apiKey.expiresAt shouldNotBe expiresAt
                        result.scopes shouldBe setOf("project:read")
                        result.description shouldBe "만료 후 재발급"

                        verify(exactly = 1) { mockApiKeyRepository.findByAccount(mockAccount) }
                        verify(exactly = 1) { mockApiKeyRepository.save(apiKey) }
                        verify(exactly = 0) { mockApiKeyRepository.delete(any()) }
                    }
                }

                context("갱신 기간 경계값 테스트 - 정확히 만료 15일 후") {
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
                            updateScopes(setOf("club:read"))
                            description = "경계값 테스트"
                        }

                    beforeEach {
                        every { mockApiKeyRepository.findByAccount(mockAccount) } returns Optional.of(apiKey)
                        every { mockApiKeyRepository.delete(apiKey) } returns Unit
                    }

                    it("갱신 기간이 지나 삭제되어야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                rotateApiKeyService.execute()
                            }

                        exception.statusCode.value() shouldBe 410

                        verify(exactly = 1) { mockApiKeyRepository.delete(apiKey) }
                        verify(exactly = 0) { mockApiKeyRepository.save(any()) }
                    }
                }

                context("ROOT 권한 사용자가 재발급할 때") {
                    val now = LocalDateTime.now()
                    val expiresAt = now.plusDays(100)
                    val oldApiKeyValue = UUID.randomUUID()
                    val apiKey =
                        ApiKey().apply {
                            id = 1L
                            value = oldApiKeyValue
                            account = mockAccount
                            createdAt = now.minusDays(200)
                            updatedAt = now.minusDays(200)
                            this.expiresAt = expiresAt
                            this.description = "루트 API 키"
                            updateScopes(setOf("student:read", "student:write"))
                        }

                    beforeEach {
                        mockAccount.role = AccountRole.ROOT
                        every { mockApiKeyRepository.findByAccount(mockAccount) } returns Optional.of(apiKey)
                        every { mockApiKeyRepository.save(apiKey) } returns apiKey
                    }

                    it("365일 만료기간으로 재발급되어야 한다") {
                        val beforeExecution = LocalDateTime.now()
                        val result = rotateApiKeyService.execute()
                        val afterExecution = LocalDateTime.now()

                        result.apiKey shouldNotBe oldApiKeyValue.toString()
                        apiKey.value shouldNotBe oldApiKeyValue

                        val expectedMinExpiresAt = beforeExecution.plusDays(365)
                        val expectedMaxExpiresAt = afterExecution.plusDays(365)
                        result.expiresAt.isAfter(expectedMinExpiresAt.minusSeconds(1)) shouldBe true
                        result.expiresAt.isBefore(expectedMaxExpiresAt.plusSeconds(1)) shouldBe true

                        verify(exactly = 1) { mockApiKeyRepository.save(apiKey) }
                    }
                }
            }
        }
    })
