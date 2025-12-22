package team.themoment.datagsm.domain.auth.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import team.themoment.datagsm.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.domain.auth.dto.request.CreateApiKeyReqDto
import team.themoment.datagsm.domain.auth.entity.ApiKey
import team.themoment.datagsm.domain.auth.repository.ApiKeyJpaRepository
import team.themoment.datagsm.domain.auth.service.impl.CreateApiKeyServiceImpl
import team.themoment.datagsm.global.exception.error.ExpectedException
import team.themoment.datagsm.global.security.checker.ScopeChecker
import team.themoment.datagsm.global.security.data.ApiKeyEnvironment
import team.themoment.datagsm.global.security.provider.CurrentUserProvider
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID

class CreateApiKeyServiceTest :
    DescribeSpec({

        val mockApiKeyRepository = mockk<ApiKeyJpaRepository>()
        val mockCurrentUserProvider = mockk<CurrentUserProvider>()
        val mockApiKeyEnvironment = mockk<ApiKeyEnvironment>()
        val mockScopeChecker = mockk<ScopeChecker>()

        val createApiKeyService =
            CreateApiKeyServiceImpl(
                mockApiKeyRepository,
                mockCurrentUserProvider,
                mockApiKeyEnvironment,
                mockScopeChecker,
            )

        afterEach {
            clearAllMocks()
        }

        describe("CreateApiKeyService 클래스의") {
            describe("execute 메서드는") {

                val mockAccount =
                    AccountJpaEntity().apply {
                        id = 1L
                        email = "test@gsm.hs.kr"
                    }
                val mockAuthentication = mockk<Authentication>()
                val mockSecurityContext = mockk<SecurityContext>()

                beforeEach {
                    mockkStatic(SecurityContextHolder::class)
                    every { SecurityContextHolder.getContext() } returns mockSecurityContext
                    every { mockSecurityContext.authentication } returns mockAuthentication
                    every { mockCurrentUserProvider.getCurrentAccount() } returns mockAccount
                    every { mockApiKeyEnvironment.expirationDays } returns 30L
                    every { mockApiKeyEnvironment.rateLimit.defaultCapacity } returns 100L
                    every { mockApiKeyEnvironment.rateLimit.defaultRefillTokens } returns 100L
                    every { mockApiKeyEnvironment.rateLimit.defaultRefillDurationSeconds } returns 60L
                    every { mockScopeChecker.hasScope(mockAuthentication, "admin:apikey") } returns false
                }

                context("기존 API 키가 없을 때") {
                    val reqDto =
                        CreateApiKeyReqDto(
                            scopes = setOf("student:read", "club:read"),
                            description = "테스트용 API 키",
                        )

                    beforeEach {
                        every { mockApiKeyRepository.findByAccount(mockAccount) } returns Optional.empty()
                        every { mockApiKeyRepository.save(any()) } answers {
                            val entity = firstArg<ApiKey>()
                            entity.apply { id = 1L }
                        }
                    }

                    it("새로운 API 키를 생성하고 반환해야 한다") {
                        val result = createApiKeyService.execute(reqDto)

                        result.id shouldBe 1L
                        result.apiKey shouldNotBe null
                        result.apiKey.isNotEmpty() shouldBe true
                        UUID.fromString(result.apiKey) shouldNotBe null // UUID 형식 검증
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
                            scopes = setOf("student:read"),
                            description = "테스트용",
                        )
                    val oldApiKeyValue = UUID.randomUUID()
                    val oldExpiresAt = LocalDateTime.now().plusDays(10)
                    val existingApiKey =
                        ApiKey().apply {
                            id = 1L
                            value = oldApiKeyValue
                            account = mockAccount
                            createdAt = LocalDateTime.now().minusDays(20)
                            updatedAt = LocalDateTime.now().minusDays(20)
                            expiresAt = oldExpiresAt
                        }

                    beforeEach {
                        every { mockApiKeyRepository.findByAccount(mockAccount) } returns
                            Optional.of(existingApiKey)
                    }

                    it("409 CONFLICT 예외가 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                createApiKeyService.execute(reqDto)
                            }

                        exception.statusCode.value() shouldBe 409
                        exception.message shouldBe "이미 API 키가 존재합니다."

                        verify(exactly = 1) { mockCurrentUserProvider.getCurrentAccount() }
                        verify(exactly = 1) { mockApiKeyRepository.findByAccount(mockAccount) }
                        verify(exactly = 0) { mockApiKeyRepository.save(any()) }
                    }
                }

                context("새로운 API 키 생성 시 만료일자가 설정될 때") {
                    val reqDto =
                        CreateApiKeyReqDto(
                            scopes = setOf("student:read"),
                            description = null,
                        )
                    val beforeExecution = LocalDateTime.now()

                    beforeEach {
                        every { mockApiKeyRepository.findByAccount(mockAccount) } returns Optional.empty()
                        every { mockApiKeyRepository.save(any()) } answers {
                            val entity = firstArg<ApiKey>()
                            entity.apply { id = 1L }
                        }
                    }

                    it("만료일자가 30일 후로 설정되어야 한다") {
                        val result = createApiKeyService.execute(reqDto)

                        val afterExecution = LocalDateTime.now()
                        val expectedMinExpiresAt = beforeExecution.plusDays(30)
                        val expectedMaxExpiresAt = afterExecution.plusDays(30)

                        result.expiresAt.isAfter(expectedMinExpiresAt.minusSeconds(1)) shouldBe true
                        result.expiresAt.isBefore(expectedMaxExpiresAt.plusSeconds(1)) shouldBe true

                        verify(exactly = 1) { mockApiKeyRepository.save(any()) }
                    }
                }

                context("API 키 생성 시 생성일자가 현재 시각으로 설정될 때") {
                    val reqDto =
                        CreateApiKeyReqDto(
                            scopes = setOf("student:read"),
                            description = "테스트",
                        )
                    lateinit var savedApiKey: ApiKey

                    beforeEach {
                        every { mockApiKeyRepository.findByAccount(mockAccount) } returns Optional.empty()
                        every { mockApiKeyRepository.save(any()) } answers {
                            savedApiKey = firstArg<ApiKey>()
                            savedApiKey.apply { id = 1L }
                        }
                    }

                    it("생성일자가 현재 시각으로 설정되어야 한다") {
                        val beforeExecution = LocalDateTime.now()
                        createApiKeyService.execute(reqDto)
                        val afterExecution = LocalDateTime.now()

                        savedApiKey.createdAt.isAfter(beforeExecution.minusSeconds(1)) shouldBe true
                        savedApiKey.createdAt.isBefore(afterExecution.plusSeconds(1)) shouldBe true
                    }
                }

                context("일반 사용자가 WRITE scope를 요청할 때") {
                    val reqDto =
                        CreateApiKeyReqDto(
                            scopes = setOf("student:write"),
                            description = "테스트",
                        )

                    beforeEach {
                        every { mockApiKeyRepository.findByAccount(mockAccount) } returns Optional.empty()
                    }

                    it("400 BAD_REQUEST 예외가 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                createApiKeyService.execute(reqDto)
                            }

                        exception.statusCode.value() shouldBe 400
                        exception.message shouldBe "일반 사용자는 READ scope만 사용 가능합니다. 사용 불가능한 scope: student:write"

                        verify(exactly = 1) { mockCurrentUserProvider.getCurrentAccount() }
                        verify(exactly = 1) { mockApiKeyRepository.findByAccount(mockAccount) }
                        verify(exactly = 0) { mockApiKeyRepository.save(any()) }
                    }
                }

                context("일반 사용자가 와일드카드 scope를 요청할 때") {
                    val reqDto =
                        CreateApiKeyReqDto(
                            scopes = setOf("student:*"),
                            description = "테스트",
                        )

                    beforeEach {
                        every { mockApiKeyRepository.findByAccount(mockAccount) } returns Optional.empty()
                    }

                    it("400 BAD_REQUEST 예외가 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                createApiKeyService.execute(reqDto)
                            }

                        exception.statusCode.value() shouldBe 400
                        exception.message shouldBe "일반 사용자는 READ scope만 사용 가능합니다. 사용 불가능한 scope: student:*"

                        verify(exactly = 0) { mockApiKeyRepository.save(any()) }
                    }
                }

                context("일반 사용자가 READ와 WRITE scope를 함께 요청할 때") {
                    val reqDto =
                        CreateApiKeyReqDto(
                            scopes = setOf("student:read", "club:write"),
                            description = "테스트",
                        )

                    beforeEach {
                        every { mockApiKeyRepository.findByAccount(mockAccount) } returns Optional.empty()
                    }

                    it("400 BAD_REQUEST 예외가 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                createApiKeyService.execute(reqDto)
                            }

                        exception.statusCode.value() shouldBe 400
                        exception.message shouldBe "일반 사용자는 READ scope만 사용 가능합니다. 사용 불가능한 scope: club:write"

                        verify(exactly = 0) { mockApiKeyRepository.save(any()) }
                    }
                }

                context("Admin 사용자가 모든 scope로 API 키를 생성할 때") {
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
                                    "admin:excel",
                                ),
                            description = "Admin API 키",
                        )

                    beforeEach {
                        every { mockScopeChecker.hasScope(mockAuthentication, "admin:apikey") } returns true
                        every { mockApiKeyEnvironment.adminExpirationDays } returns 365L
                        every { mockApiKeyRepository.findByAccount(mockAccount) } returns Optional.empty()
                        every { mockApiKeyRepository.save(any()) } answers {
                            val entity = firstArg<ApiKey>()
                            entity.apply { id = 1L }
                        }
                    }

                    it("모든 scope로 API 키를 생성하고 365일 만료기간을 설정해야 한다") {
                        val beforeExecution = LocalDateTime.now()
                        val result = createApiKeyService.execute(reqDto)
                        val afterExecution = LocalDateTime.now()

                        result.apiKey shouldNotBe null
                        result.apiKey.isNotEmpty() shouldBe true
                        UUID.fromString(result.apiKey) shouldNotBe null // UUID 형식 검증
                        result.scopes shouldBe reqDto.scopes
                        result.description shouldBe reqDto.description

                        val expectedMinExpiresAt = beforeExecution.plusDays(365)
                        val expectedMaxExpiresAt = afterExecution.plusDays(365)
                        result.expiresAt.isAfter(expectedMinExpiresAt.minusSeconds(1)) shouldBe true
                        result.expiresAt.isBefore(expectedMaxExpiresAt.plusSeconds(1)) shouldBe true

                        verify(exactly = 1) { mockApiKeyRepository.save(any()) }
                    }
                }

                context("Admin 사용자가 유효하지 않은 scope를 요청할 때") {
                    val reqDto =
                        CreateApiKeyReqDto(
                            scopes = setOf("invalid:scope"),
                            description = "테스트",
                        )

                    beforeEach {
                        every { mockScopeChecker.hasScope(mockAuthentication, "admin:apikey") } returns true
                        every { mockApiKeyRepository.findByAccount(mockAccount) } returns Optional.empty()
                    }

                    it("400 BAD_REQUEST 예외가 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                createApiKeyService.execute(reqDto)
                            }

                        exception.statusCode.value() shouldBe 400
                        exception.message shouldBe "유효하지 않은 scope입니다: invalid:scope"

                        verify(exactly = 0) { mockApiKeyRepository.save(any()) }
                    }
                }
            }
        }
    })
