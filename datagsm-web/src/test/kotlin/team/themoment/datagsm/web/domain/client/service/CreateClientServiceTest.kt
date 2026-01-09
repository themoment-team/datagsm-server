package team.themoment.datagsm.web.domain.client.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import team.themoment.datagsm.common.domain.account.AccountJpaEntity
import team.themoment.datagsm.common.domain.account.ApiScope
import team.themoment.datagsm.common.domain.client.ClientJpaEntity
import team.themoment.datagsm.common.domain.client.repository.ClientJpaRepository
import team.themoment.datagsm.common.dto.client.request.CreateClientReqDto
import team.themoment.datagsm.web.domain.client.service.GetAvailableOauthScopesService
import team.themoment.datagsm.web.domain.client.service.impl.CreateClientServiceImpl
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException

class CreateClientServiceTest :
    DescribeSpec({

        val mockCurrentUserProvider = mockk<CurrentUserProvider>()
        val mockPasswordEncoder = mockk<PasswordEncoder>()
        val mockClientJpaRepository = mockk<ClientJpaRepository>()
        val mockGetAvailableOauthScopesService = mockk<GetAvailableOauthScopesService>()

        val createClientService =
            CreateClientServiceImpl(
                mockCurrentUserProvider,
                mockPasswordEncoder,
                mockClientJpaRepository,
                mockGetAvailableOauthScopesService,
            )

        afterEach {
            clearAllMocks()
        }

        describe("CreateClientService 클래스의") {
            describe("execute 메서드는") {

                val mockAccount =
                    AccountJpaEntity().apply {
                        id = 1L
                        email = "test@gsm.hs.kr"
                    }

                context("유효한 scope로 클라이언트 생성 요청할 때") {
                    val reqDto =
                        CreateClientReqDto(
                            name = "Test OAuth Client",
                            scopes = setOf("self:read", "student:read"),
                        )

                    beforeEach {
                        every { mockGetAvailableOauthScopesService.execute() } returns
                            setOf("self:read", "student:read", "club:read")
                        every { mockCurrentUserProvider.getCurrentAccount() } returns mockAccount
                        every { mockPasswordEncoder.encode(any()) } returns "encoded_secret"
                        every { mockClientJpaRepository.save(any()) } answers {
                            firstArg<ClientJpaEntity>()
                        }
                    }

                    it("새로운 OAuth 클라이언트를 생성하고 반환해야 한다") {
                        val result = createClientService.execute(reqDto)

                        result.clientId shouldNotBe null
                        result.clientSecret shouldNotBe null
                        result.name shouldBe "Test OAuth Client"
                        result.redirectUrls shouldBe emptySet()

                        verify(exactly = 1) { mockGetAvailableOauthScopesService.execute() }
                        verify(exactly = 1) { mockCurrentUserProvider.getCurrentAccount() }
                        verify(exactly = 1) { mockPasswordEncoder.encode(any()) }
                        verify(exactly = 1) { mockClientJpaRepository.save(any()) }
                    }
                }

                context("허용되지 않는 scope를 포함하여 클라이언트 생성 요청할 때") {
                    val reqDto =
                        CreateClientReqDto(
                            name = "Invalid Client",
                            scopes = setOf("self:read", "invalid:scope"),
                        )

                    beforeEach {
                        every { mockGetAvailableOauthScopesService.execute() } returns
                            setOf("self:read", "student:read", "club:read")
                    }

                    it("400 BAD_REQUEST 예외가 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                createClientService.execute(reqDto)
                            }

                        exception.statusCode shouldBe HttpStatus.BAD_REQUEST
                        exception.message shouldBe "허용되지 않는 OAuth 권한이 포함되어 있습니다: [invalid:scope]"

                        verify(exactly = 1) { mockGetAvailableOauthScopesService.execute() }
                        verify(exactly = 0) { mockCurrentUserProvider.getCurrentAccount() }
                        verify(exactly = 0) { mockPasswordEncoder.encode(any()) }
                        verify(exactly = 0) { mockClientJpaRepository.save(any()) }
                    }
                }

                context("여러 개의 허용되지 않는 scope를 포함하여 클라이언트 생성 요청할 때") {
                    val reqDto =
                        CreateClientReqDto(
                            name = "Invalid Client",
                            scopes = setOf("self:read", "invalid:scope1", "invalid:scope2"),
                        )

                    beforeEach {
                        every { mockGetAvailableOauthScopesService.execute() } returns
                            setOf("self:read", "student:read")
                    }

                    it("400 BAD_REQUEST 예외가 발생하고 모든 유효하지 않은 scope를 포함해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                createClientService.execute(reqDto)
                            }

                        exception.statusCode shouldBe HttpStatus.BAD_REQUEST
                        exception.message shouldBe "허용되지 않는 OAuth 권한이 포함되어 있습니다: [invalid:scope1, invalid:scope2]"

                        verify(exactly = 1) { mockGetAvailableOauthScopesService.execute() }
                        verify(exactly = 0) { mockClientJpaRepository.save(any()) }
                    }
                }

                context("클라이언트 secret이 암호화되어 저장될 때") {
                    val reqDto =
                        CreateClientReqDto(
                            name = "Test Client",
                            scopes = setOf("self:read"),
                        )
                    lateinit var savedClient: ClientJpaEntity

                    beforeEach {
                        every { mockGetAvailableOauthScopesService.execute() } returns
                            setOf("self:read", "student:read")
                        every { mockCurrentUserProvider.getCurrentAccount() } returns mockAccount
                        every { mockPasswordEncoder.encode(any()) } returns "hashed_secret_value"
                        every { mockClientJpaRepository.save(any()) } answers {
                            savedClient = firstArg<ClientJpaEntity>()
                            savedClient
                        }
                    }

                    it("secret이 PasswordEncoder로 암호화되어 저장되어야 한다") {
                        val result = createClientService.execute(reqDto)

                        savedClient.secret shouldBe "hashed_secret_value"
                        result.clientSecret shouldNotBe "hashed_secret_value"

                        verify(exactly = 1) { mockPasswordEncoder.encode(any()) }
                    }
                }

                context("클라이언트 생성 시 redirectUrls가 빈 Set으로 초기화될 때") {
                    val reqDto =
                        CreateClientReqDto(
                            name = "Test Client",
                            scopes = setOf("self:read"),
                        )
                    lateinit var savedClient: ClientJpaEntity

                    beforeEach {
                        every { mockGetAvailableOauthScopesService.execute() } returns
                            setOf("self:read")
                        every { mockCurrentUserProvider.getCurrentAccount() } returns mockAccount
                        every { mockPasswordEncoder.encode(any()) } returns "encoded_secret"
                        every { mockClientJpaRepository.save(any()) } answers {
                            savedClient = firstArg<ClientJpaEntity>()
                            savedClient
                        }
                    }

                    it("redirectUrls가 빈 Set으로 설정되어야 한다") {
                        val result = createClientService.execute(reqDto)

                        savedClient.redirectUrls shouldBe emptySet()
                        result.redirectUrls shouldBe emptySet()

                        verify(exactly = 1) { mockClientJpaRepository.save(any()) }
                    }
                }

                context("클라이언트 생성 시 scope가 ApiScope enum으로 변환될 때") {
                    val reqDto =
                        CreateClientReqDto(
                            name = "Test Client",
                            scopes = setOf("self:read", "student:read"),
                        )
                    lateinit var savedClient: ClientJpaEntity

                    beforeEach {
                        every { mockGetAvailableOauthScopesService.execute() } returns
                            setOf("self:read", "student:read", "club:read")
                        every { mockCurrentUserProvider.getCurrentAccount() } returns mockAccount
                        every { mockPasswordEncoder.encode(any()) } returns "encoded_secret"
                        every { mockClientJpaRepository.save(any()) } answers {
                            savedClient = firstArg<ClientJpaEntity>()
                            savedClient
                        }
                    }

                    it("요청된 scope가 ApiScope enum으로 변환되어 저장되어야 한다") {
                        createClientService.execute(reqDto)

                        savedClient.scopes shouldBe setOf(ApiScope.SELF_READ, ApiScope.STUDENT_READ)

                        verify(exactly = 1) { mockClientJpaRepository.save(any()) }
                    }
                }

                context("클라이언트 생성 시 현재 사용자의 Account가 연결될 때") {
                    val reqDto =
                        CreateClientReqDto(
                            name = "Test Client",
                            scopes = setOf("self:read"),
                        )
                    lateinit var savedClient: ClientJpaEntity

                    beforeEach {
                        every { mockGetAvailableOauthScopesService.execute() } returns
                            setOf("self:read")
                        every { mockCurrentUserProvider.getCurrentAccount() } returns mockAccount
                        every { mockPasswordEncoder.encode(any()) } returns "encoded_secret"
                        every { mockClientJpaRepository.save(any()) } answers {
                            savedClient = firstArg<ClientJpaEntity>()
                            savedClient
                        }
                    }

                    it("현재 사용자의 Account가 클라이언트에 연결되어야 한다") {
                        createClientService.execute(reqDto)

                        savedClient.account shouldBe mockAccount
                        savedClient.account.id shouldBe 1L
                        savedClient.account.email shouldBe "test@gsm.hs.kr"

                        verify(exactly = 1) { mockCurrentUserProvider.getCurrentAccount() }
                    }
                }

                context("유효한 scope로 생성된 클라이언트 ID와 secret이 UUID 형식일 때") {
                    val reqDto =
                        CreateClientReqDto(
                            name = "UUID Test Client",
                            scopes = setOf("self:read"),
                        )

                    beforeEach {
                        every { mockGetAvailableOauthScopesService.execute() } returns
                            setOf("self:read")
                        every { mockCurrentUserProvider.getCurrentAccount() } returns mockAccount
                        every { mockPasswordEncoder.encode(any()) } returns "encoded_secret"
                        every { mockClientJpaRepository.save(any()) } answers {
                            firstArg<ClientJpaEntity>()
                        }
                    }

                    it("생성된 clientId와 clientSecret이 UUID 형식이어야 한다") {
                        val result = createClientService.execute(reqDto)

                        val uuidRegex =
                            Regex("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")
                        uuidRegex.matches(result.clientId) shouldBe true
                        uuidRegex.matches(result.clientSecret) shouldBe true
                    }
                }

                context("빈 scope 목록으로 클라이언트 생성 요청할 때") {
                    val reqDto =
                        CreateClientReqDto(
                            name = "Empty Scope Client",
                            scopes = emptySet(),
                        )

                    beforeEach {
                        every { mockGetAvailableOauthScopesService.execute() } returns
                            setOf("self:read", "student:read")
                        every { mockCurrentUserProvider.getCurrentAccount() } returns mockAccount
                        every { mockPasswordEncoder.encode(any()) } returns "encoded_secret"
                        every { mockClientJpaRepository.save(any()) } answers {
                            firstArg<ClientJpaEntity>()
                        }
                    }

                    it("빈 scope로 클라이언트를 생성해야 한다") {
                        val result = createClientService.execute(reqDto)

                        result.clientId shouldNotBe null
                        result.name shouldBe "Empty Scope Client"

                        verify(exactly = 1) { mockClientJpaRepository.save(any()) }
                    }
                }
            }
        }
    })
