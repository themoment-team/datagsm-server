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
import team.themoment.datagsm.domain.auth.service.impl.ModifyAdminApiKeyServiceImpl
import team.themoment.datagsm.global.exception.error.ExpectedException
import team.themoment.datagsm.global.security.provider.CurrentUserProvider
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID

class ModifyAdminApiKeyServiceTest :
    DescribeSpec({

        val mockApiKeyRepository = mockk<ApiKeyJpaRepository>()
        val mockCurrentUserProvider = mockk<CurrentUserProvider>()

        val modifyAdminApiKeyService =
            ModifyAdminApiKeyServiceImpl(
                mockApiKeyRepository,
                mockCurrentUserProvider,
            )

        afterEach {
            clearAllMocks()
        }

        describe("ModifyAdminApiKeyService 클래스의") {
            describe("execute 메서드는") {

                val mockAccount =
                    AccountJpaEntity().apply {
                        id = 1L
                        email = "admin@gsm.hs.kr"
                    }

                beforeEach {
                    every { mockCurrentUserProvider.getCurrentAccount() } returns mockAccount
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
                                modifyAdminApiKeyService.execute(reqDto)
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
                    val expiresAt = now.plusDays(100)
                    val apiKey =
                        ApiKey().apply {
                            id = 1L
                            value = UUID.randomUUID()
                            account = mockAccount
                            createdAt = now.minusDays(265)
                            updatedAt = now.minusDays(265)
                            this.expiresAt = expiresAt
                        }

                    beforeEach {
                        every { mockApiKeyRepository.findByAccount(mockAccount) } returns Optional.of(apiKey)
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                modifyAdminApiKeyService.execute(reqDto)
                            }

                        exception.statusCode.value() shouldBe 400
                        exception.message shouldBe "API 키 갱신 기간이 아닙니다. 만료 30일 전부터 만료 30일 후까지만 갱신 가능합니다."

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
                    val expiresAt = now.minusDays(50)
                    val apiKey =
                        ApiKey().apply {
                            id = 1L
                            value = UUID.randomUUID()
                            account = mockAccount
                            createdAt = now.minusDays(415)
                            updatedAt = now.minusDays(415)
                            this.expiresAt = expiresAt
                        }

                    beforeEach {
                        every { mockApiKeyRepository.findByAccount(mockAccount) } returns Optional.of(apiKey)
                        every { mockApiKeyRepository.delete(apiKey) } returns Unit
                    }

                    it("API 키가 삭제되고 410 GONE 예외가 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                modifyAdminApiKeyService.execute(reqDto)
                            }

                        exception.statusCode.value() shouldBe 410
                        exception.message shouldBe "API 키 갱신 기간이 지났습니다. 해당 API 키는 삭제되었습니다."

                        verify(exactly = 1) { mockApiKeyRepository.findByAccount(mockAccount) }
                        verify(exactly = 1) { mockApiKeyRepository.delete(apiKey) }
                        verify(exactly = 0) { mockApiKeyRepository.save(any()) }
                    }
                }

                context("정상적으로 갱신할 때 (만료 30일 전)") {
                    val reqDto =
                        ModifyApiKeyReqDto(
                            scopes = setOf("student:read", "club:write"),
                            description = "갱신된 Admin API 키",
                        )
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
                        }

                    beforeEach {
                        every { mockApiKeyRepository.findByAccount(mockAccount) } returns Optional.of(apiKey)
                        every { mockApiKeyRepository.save(apiKey) } returns apiKey
                    }

                    it("기존 API 키를 유지하고 만료일자 및 scope를 갱신해야 한다") {
                        val result = modifyAdminApiKeyService.execute(reqDto)

                        result.apiKey shouldBe oldApiKeyValue
                        result.expiresAt shouldNotBe null
                        result.scopes shouldBe reqDto.scopes
                        result.description shouldBe reqDto.description

                        verify(exactly = 1) { mockApiKeyRepository.findByAccount(mockAccount) }
                        verify(exactly = 1) { mockApiKeyRepository.save(apiKey) }
                        verify(exactly = 0) { mockApiKeyRepository.delete(any()) }
                    }
                }

                context("정상적으로 갱신할 때 (만료 후 10일)") {
                    val reqDto =
                        ModifyApiKeyReqDto(
                            scopes = setOf("project:*"),
                            description = "만료 후 갱신",
                        )
                    val now = LocalDateTime.now()
                    val expiresAt = now.minusDays(10)
                    val oldApiKeyValue = UUID.randomUUID()
                    val apiKey =
                        ApiKey().apply {
                            id = 1L
                            value = oldApiKeyValue
                            account = mockAccount
                            createdAt = now.minusDays(375)
                            updatedAt = now.minusDays(375)
                            this.expiresAt = expiresAt
                        }

                    beforeEach {
                        every { mockApiKeyRepository.findByAccount(mockAccount) } returns Optional.of(apiKey)
                        every { mockApiKeyRepository.save(apiKey) } returns apiKey
                    }

                    it("기존 API 키를 유지하고 만료일자 및 scope를 갱신해야 한다") {
                        val result = modifyAdminApiKeyService.execute(reqDto)

                        result.apiKey shouldBe oldApiKeyValue
                        apiKey.value shouldBe oldApiKeyValue
                        apiKey.expiresAt shouldNotBe expiresAt

                        verify(exactly = 1) { mockApiKeyRepository.findByAccount(mockAccount) }
                        verify(exactly = 1) { mockApiKeyRepository.save(apiKey) }
                        verify(exactly = 0) { mockApiKeyRepository.delete(any()) }
                    }
                }

                context("갱신 기간 경계값 테스트 - 정확히 만료 30일 전") {
                    val reqDto =
                        ModifyApiKeyReqDto(
                            scopes = setOf("student:*"),
                            description = "경계값 테스트",
                        )
                    val now = LocalDateTime.now()
                    val expiresAt = now.plusDays(30)
                    val apiKey =
                        ApiKey().apply {
                            id = 1L
                            value = UUID.randomUUID()
                            account = mockAccount
                            createdAt = now.minusDays(335)
                            updatedAt = now.minusDays(335)
                            this.expiresAt = expiresAt
                        }

                    beforeEach {
                        every { mockApiKeyRepository.findByAccount(mockAccount) } returns Optional.of(apiKey)
                        every { mockApiKeyRepository.save(apiKey) } returns apiKey
                    }

                    it("갱신이 가능해야 한다") {
                        val result = modifyAdminApiKeyService.execute(reqDto)

                        result.apiKey shouldNotBe null

                        verify(exactly = 1) { mockApiKeyRepository.save(apiKey) }
                        verify(exactly = 0) { mockApiKeyRepository.delete(any()) }
                    }
                }

                context("갱신 기간 경계값 테스트 - 정확히 만료 30일 후") {
                    val reqDto =
                        ModifyApiKeyReqDto(
                            scopes = setOf("club:*"),
                            description = "경계값 테스트2",
                        )
                    val now = LocalDateTime.now()
                    val expiresAt = now.minusDays(30)
                    val apiKey =
                        ApiKey().apply {
                            id = 1L
                            value = UUID.randomUUID()
                            account = mockAccount
                            createdAt = now.minusDays(395)
                            updatedAt = now.minusDays(395)
                            this.expiresAt = expiresAt
                        }

                    beforeEach {
                        every { mockApiKeyRepository.findByAccount(mockAccount) } returns Optional.of(apiKey)
                        every { mockApiKeyRepository.delete(apiKey) } returns Unit
                    }

                    it("갱신 기간이 지나 삭제되어야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                modifyAdminApiKeyService.execute(reqDto)
                            }

                        exception.statusCode.value() shouldBe 410

                        verify(exactly = 1) { mockApiKeyRepository.delete(apiKey) }
                        verify(exactly = 0) { mockApiKeyRepository.save(any()) }
                    }
                }

                context("Admin이 모든 scope로 갱신할 때") {
                    val reqDto =
                        ModifyApiKeyReqDto(
                            scopes =
                                setOf(
                                    "student:read",
                                    "student:write",
                                    "club:read",
                                    "club:write",
                                    "project:read",
                                    "project:write",
                                ),
                            description = "모든 권한",
                        )
                    val now = LocalDateTime.now()
                    val expiresAt = now.plusDays(20)
                    val apiKey =
                        ApiKey().apply {
                            id = 1L
                            value = UUID.randomUUID()
                            account = mockAccount
                            createdAt = now.minusDays(345)
                            updatedAt = now.minusDays(345)
                            this.expiresAt = expiresAt
                        }

                    beforeEach {
                        every { mockApiKeyRepository.findByAccount(mockAccount) } returns Optional.of(apiKey)
                        every { mockApiKeyRepository.save(apiKey) } returns apiKey
                    }

                    it("갱신이 성공해야 한다") {
                        val result = modifyAdminApiKeyService.execute(reqDto)

                        result.scopes shouldBe reqDto.scopes

                        verify(exactly = 1) { mockApiKeyRepository.save(apiKey) }
                    }
                }

                context("유효하지 않은 scope로 갱신을 시도할 때") {
                    val reqDto =
                        ModifyApiKeyReqDto(
                            scopes = setOf("invalid:scope"),
                            description = "테스트",
                        )
                    val now = LocalDateTime.now()
                    val expiresAt = now.plusDays(20)
                    val apiKey =
                        ApiKey().apply {
                            id = 1L
                            value = UUID.randomUUID()
                            account = mockAccount
                            createdAt = now.minusDays(345)
                            updatedAt = now.minusDays(345)
                            this.expiresAt = expiresAt
                        }

                    beforeEach {
                        every { mockApiKeyRepository.findByAccount(mockAccount) } returns Optional.of(apiKey)
                    }

                    it("400 BAD_REQUEST 예외가 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                modifyAdminApiKeyService.execute(reqDto)
                            }

                        exception.statusCode.value() shouldBe 400
                        exception.message shouldBe "유효하지 않은 scope입니다: invalid:scope"

                        verify(exactly = 0) { mockApiKeyRepository.save(any()) }
                        verify(exactly = 0) { mockApiKeyRepository.delete(any()) }
                    }
                }
            }
        }
    })