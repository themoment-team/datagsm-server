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
import team.themoment.datagsm.domain.auth.dto.request.CreateApiKeyReqDto
import team.themoment.datagsm.domain.auth.entity.ApiKey
import team.themoment.datagsm.domain.auth.repository.ApiKeyJpaRepository
import team.themoment.datagsm.domain.auth.service.impl.CreateAdminApiKeyServiceImpl
import team.themoment.datagsm.global.exception.error.ExpectedException
import team.themoment.datagsm.global.security.data.ApiKeyEnvironment
import team.themoment.datagsm.global.security.provider.CurrentUserProvider
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID

class CreateAdminApiKeyServiceTest :
    DescribeSpec({

        val mockApiKeyRepository = mockk<ApiKeyJpaRepository>()
        val mockCurrentUserProvider = mockk<CurrentUserProvider>()
        val mockApiKeyEnvironment = mockk<ApiKeyEnvironment>()

        val createAdminApiKeyService =
            CreateAdminApiKeyServiceImpl(
                mockApiKeyRepository,
                mockCurrentUserProvider,
                mockApiKeyEnvironment,
            )

        afterEach {
            clearAllMocks()
        }

        describe("CreateAdminApiKeyService 클래스의") {
            describe("execute 메서드는") {

                val mockAccount =
                    AccountJpaEntity().apply {
                        id = 1L
                        email = "admin@gsm.hs.kr"
                    }

                beforeEach {
                    every { mockCurrentUserProvider.getCurrentAccount() } returns mockAccount
                }

                context("기존 API 키가 없을 때") {
                    val reqDto =
                        CreateApiKeyReqDto(
                            scopes = setOf("student:read", "club:read", "student:write"),
                            description = "관리자용 API 키",
                        )

                    beforeEach {
                        every { mockApiKeyEnvironment.adminExpirationDays } returns 365L
                        every { mockApiKeyRepository.findByAccount(mockAccount) } returns Optional.empty()
                        every { mockApiKeyRepository.save(any()) } answers {
                            val entity = firstArg<ApiKey>()
                            entity.apply { id = 1L }
                        }
                    }

                    it("새로운 Admin API 키를 생성하고 반환해야 한다") {
                        val result = createAdminApiKeyService.execute(reqDto)

                        result.apiKey shouldNotBe null
                        result.expiresAt shouldNotBe null
                        result.scopes shouldBe reqDto.scopes
                        result.description shouldBe reqDto.description

                        verify(exactly = 1) { mockCurrentUserProvider.getCurrentAccount() }
                        verify(exactly = 1) { mockApiKeyRepository.findByAccount(mockAccount) }
                        verify(exactly = 1) { mockApiKeyRepository.save(any()) }
                    }
                }

                context("기존 API 키가 있을 때") {
                    val reqDto =
                        CreateApiKeyReqDto(
                            scopes = setOf("student:*"),
                            description = "관리자용",
                        )
                    val oldApiKeyValue = UUID.randomUUID()
                    val oldExpiresAt = LocalDateTime.now().plusDays(100)
                    val existingApiKey =
                        ApiKey().apply {
                            id = 1L
                            value = oldApiKeyValue
                            account = mockAccount
                            createdAt = LocalDateTime.now().minusDays(265)
                            updatedAt = LocalDateTime.now().minusDays(265)
                            expiresAt = oldExpiresAt
                        }

                    beforeEach {
                        every { mockApiKeyRepository.findByAccount(mockAccount) } returns
                            Optional.of(existingApiKey)
                    }

                    it("409 CONFLICT 예외가 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                createAdminApiKeyService.execute(reqDto)
                            }

                        exception.statusCode.value() shouldBe 409
                        exception.message shouldBe "이미 API 키가 존재합니다."

                        verify(exactly = 1) { mockCurrentUserProvider.getCurrentAccount() }
                        verify(exactly = 1) { mockApiKeyRepository.findByAccount(mockAccount) }
                        verify(exactly = 0) { mockApiKeyRepository.save(any()) }
                    }
                }

                context("Admin이 모든 scope를 요청할 때") {
                    val reqDto =
                        CreateApiKeyReqDto(
                            scopes =
                                setOf(
                                    "student:read",
                                    "student:write",
                                    "club:read",
                                    "club:write",
                                    "project:read",
                                    "project:write",
                                ),
                            description = "모든 권한 API 키",
                        )

                    beforeEach {
                        every { mockApiKeyEnvironment.adminExpirationDays } returns 365L
                        every { mockApiKeyRepository.findByAccount(mockAccount) } returns Optional.empty()
                        every { mockApiKeyRepository.save(any()) } answers {
                            val entity = firstArg<ApiKey>()
                            entity.apply { id = 1L }
                        }
                    }

                    it("API 키가 생성되어야 한다") {
                        val result = createAdminApiKeyService.execute(reqDto)

                        result.apiKey shouldNotBe null
                        result.scopes shouldBe reqDto.scopes

                        verify(exactly = 1) { mockApiKeyRepository.save(any()) }
                    }
                }

                context("Admin이 와일드카드 scope를 요청할 때") {
                    val reqDto =
                        CreateApiKeyReqDto(
                            scopes = setOf("admin:*"),
                            description = "관리자 와일드카드 API 키",
                        )

                    beforeEach {
                        every { mockApiKeyEnvironment.adminExpirationDays } returns 365L
                        every { mockApiKeyRepository.findByAccount(mockAccount) } returns Optional.empty()
                        every { mockApiKeyRepository.save(any()) } answers {
                            val entity = firstArg<ApiKey>()
                            entity.apply { id = 1L }
                        }
                    }

                    it("API 키가 생성되어야 한다") {
                        val result = createAdminApiKeyService.execute(reqDto)

                        result.apiKey shouldNotBe null
                        result.scopes shouldBe reqDto.scopes

                        verify(exactly = 1) { mockApiKeyRepository.save(any()) }
                    }
                }

                context("Admin API 키 생성 시 만료일자가 365일 후로 설정될 때") {
                    val reqDto =
                        CreateApiKeyReqDto(
                            scopes = setOf("student:read"),
                            description = null,
                        )
                    val beforeExecution = LocalDateTime.now()

                    beforeEach {
                        every { mockApiKeyEnvironment.adminExpirationDays } returns 365L
                        every { mockApiKeyRepository.findByAccount(mockAccount) } returns Optional.empty()
                        every { mockApiKeyRepository.save(any()) } answers {
                            val entity = firstArg<ApiKey>()
                            entity.apply { id = 1L }
                        }
                    }

                    it("만료일자가 365일 후로 설정되어야 한다") {
                        val result = createAdminApiKeyService.execute(reqDto)

                        val afterExecution = LocalDateTime.now()
                        val expectedMinExpiresAt = beforeExecution.plusDays(365)
                        val expectedMaxExpiresAt = afterExecution.plusDays(365)

                        result.expiresAt.isAfter(expectedMinExpiresAt.minusSeconds(1)) shouldBe true
                        result.expiresAt.isBefore(expectedMaxExpiresAt.plusSeconds(1)) shouldBe true

                        verify(exactly = 1) { mockApiKeyRepository.save(any()) }
                    }
                }

                context("유효하지 않은 scope를 요청할 때") {
                    val reqDto =
                        CreateApiKeyReqDto(
                            scopes = setOf("invalid:scope"),
                            description = "테스트",
                        )

                    beforeEach {
                        every { mockApiKeyRepository.findByAccount(mockAccount) } returns Optional.empty()
                    }

                    it("400 BAD_REQUEST 예외가 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                createAdminApiKeyService.execute(reqDto)
                            }

                        exception.statusCode.value() shouldBe 400
                        exception.message shouldBe "유효하지 않은 scope입니다: invalid:scope"

                        verify(exactly = 0) { mockApiKeyRepository.save(any()) }
                    }
                }
            }
        }
    })