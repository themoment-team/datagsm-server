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
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.client.dto.request.CreateClientReqDto
import team.themoment.datagsm.common.domain.client.dto.response.OAuthScopeListResDto
import team.themoment.datagsm.common.domain.client.dto.response.OAuthScopeResDto
import team.themoment.datagsm.common.domain.client.entity.ClientJpaEntity
import team.themoment.datagsm.common.domain.client.repository.ClientJpaRepository
import team.themoment.datagsm.web.domain.client.service.impl.CreateClientServiceImpl
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException

class CreateClientServiceTest :
    DescribeSpec({

        val mockCurrentUserProvider = mockk<CurrentUserProvider>()
        val mockPasswordEncoder = mockk<PasswordEncoder>()
        val mockClientJpaRepository = mockk<ClientJpaRepository>()
        val mockQueryAvailableOauthScopesService = mockk<QueryAvailableOauthScopesService>()

        val createClientService =
            CreateClientServiceImpl(
                mockCurrentUserProvider,
                mockPasswordEncoder,
                mockClientJpaRepository,
                mockQueryAvailableOauthScopesService,
            )

        fun buildScopeList(vararg scopes: String): OAuthScopeListResDto =
            OAuthScopeListResDto(
                list = scopes.map { OAuthScopeResDto(scope = it, description = "", applicationName = "Test App") },
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
                            clientName = "Test OAuth Client",
                            serviceName = "테스트 서비스",
                            scopes = setOf("self:read"),
                            redirectUrls = emptySet(),
                        )

                    beforeEach {
                        every { mockQueryAvailableOauthScopesService.execute() } returns buildScopeList("self:read")
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
                        result.clientName shouldBe "Test OAuth Client"
                        result.serviceName shouldBe "테스트 서비스"
                        result.redirectUrls shouldBe emptySet()
                        result.scopes shouldBe setOf("self:read")

                        verify(exactly = 1) { mockQueryAvailableOauthScopesService.execute() }
                        verify(exactly = 1) { mockCurrentUserProvider.getCurrentAccount() }
                        verify(exactly = 1) { mockPasswordEncoder.encode(any()) }
                        verify(exactly = 1) { mockClientJpaRepository.save(any()) }
                    }
                }

                context("허용되지 않는 scope를 포함하여 클라이언트 생성 요청할 때") {
                    val reqDto =
                        CreateClientReqDto(
                            clientName = "Invalid Client",
                            serviceName = "잘못된 서비스",
                            scopes = setOf("self:read", "unknown"),
                            redirectUrls = emptySet(),
                        )

                    beforeEach {
                        every { mockQueryAvailableOauthScopesService.execute() } returns buildScopeList("self:read")
                    }

                    it("400 BAD_REQUEST 예외가 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                createClientService.execute(reqDto)
                            }

                        exception.statusCode shouldBe HttpStatus.BAD_REQUEST

                        verify(exactly = 1) { mockQueryAvailableOauthScopesService.execute() }
                        verify(exactly = 0) { mockCurrentUserProvider.getCurrentAccount() }
                        verify(exactly = 0) { mockPasswordEncoder.encode(any()) }
                        verify(exactly = 0) { mockClientJpaRepository.save(any()) }
                    }
                }

                context("가용하지 않은 OAuthScope를 포함하여 클라이언트 생성 요청할 때") {
                    val reqDto =
                        CreateClientReqDto(
                            clientName = "Invalid Client",
                            serviceName = "잘못된 서비스",
                            scopes = setOf("self:read", "appId:scopeName"),
                            redirectUrls = emptySet(),
                        )

                    beforeEach {
                        every { mockQueryAvailableOauthScopesService.execute() } returns buildScopeList("self:read")
                    }

                    it("400 BAD_REQUEST 예외가 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                createClientService.execute(reqDto)
                            }

                        exception.statusCode shouldBe HttpStatus.BAD_REQUEST

                        verify(exactly = 1) { mockQueryAvailableOauthScopesService.execute() }
                        verify(exactly = 0) { mockClientJpaRepository.save(any()) }
                    }
                }

                context("클라이언트 secret이 암호화되어 저장될 때") {
                    val reqDto =
                        CreateClientReqDto(
                            clientName = "Test Client",
                            serviceName = "테스트 서비스",
                            scopes = setOf("self:read"),
                            redirectUrls = emptySet(),
                        )
                    lateinit var savedClient: ClientJpaEntity

                    beforeEach {
                        every { mockQueryAvailableOauthScopesService.execute() } returns buildScopeList("self:read")
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
                            clientName = "Test Client",
                            serviceName = "테스트 서비스",
                            scopes = setOf("self:read"),
                            redirectUrls = emptySet(),
                        )
                    lateinit var savedClient: ClientJpaEntity

                    beforeEach {
                        every { mockQueryAvailableOauthScopesService.execute() } returns buildScopeList("self:read")
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

                context("클라이언트 생성 시 redirectUrls를 포함할 때") {
                    val reqDto =
                        CreateClientReqDto(
                            clientName = "Test Client with URLs",
                            serviceName = "테스트 서비스",
                            scopes = setOf("self:read"),
                            redirectUrls = setOf("https://example.com/callback", "https://app.example.com/oauth/callback"),
                        )
                    lateinit var savedClient: ClientJpaEntity

                    beforeEach {
                        every { mockQueryAvailableOauthScopesService.execute() } returns buildScopeList("self:read")
                        every { mockCurrentUserProvider.getCurrentAccount() } returns mockAccount
                        every { mockPasswordEncoder.encode(any()) } returns "encoded_secret"
                        every { mockClientJpaRepository.save(any()) } answers {
                            savedClient = firstArg<ClientJpaEntity>()
                            savedClient
                        }
                    }

                    it("redirectUrls가 요청한 값으로 설정되어야 한다") {
                        val result = createClientService.execute(reqDto)

                        savedClient.redirectUrls shouldBe reqDto.redirectUrls
                        result.redirectUrls shouldBe reqDto.redirectUrls

                        verify(exactly = 1) { mockClientJpaRepository.save(any()) }
                    }
                }

                context("클라이언트 생성 시 scope가 String으로 저장될 때") {
                    val reqDto =
                        CreateClientReqDto(
                            clientName = "Test Client",
                            serviceName = "테스트 서비스",
                            scopes = setOf("self:read"),
                            redirectUrls = emptySet(),
                        )
                    lateinit var savedClient: ClientJpaEntity

                    beforeEach {
                        every { mockQueryAvailableOauthScopesService.execute() } returns buildScopeList("self:read")
                        every { mockCurrentUserProvider.getCurrentAccount() } returns mockAccount
                        every { mockPasswordEncoder.encode(any()) } returns "encoded_secret"
                        every { mockClientJpaRepository.save(any()) } answers {
                            savedClient = firstArg<ClientJpaEntity>()
                            savedClient
                        }
                    }

                    it("요청된 scope가 String으로 저장되어야 한다") {
                        createClientService.execute(reqDto)

                        savedClient.scopes shouldBe setOf("self:read")

                        verify(exactly = 1) { mockClientJpaRepository.save(any()) }
                    }
                }

                context("클라이언트 생성 시 현재 사용자의 Account가 연결될 때") {
                    val reqDto =
                        CreateClientReqDto(
                            clientName = "Test Client",
                            serviceName = "테스트 서비스",
                            scopes = setOf("self:read"),
                            redirectUrls = emptySet(),
                        )
                    lateinit var savedClient: ClientJpaEntity

                    beforeEach {
                        every { mockQueryAvailableOauthScopesService.execute() } returns buildScopeList("self:read")
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
                            clientName = "UUID Test Client",
                            serviceName = "UUID 테스트 서비스",
                            scopes = setOf("self:read"),
                            redirectUrls = emptySet(),
                        )

                    beforeEach {
                        every { mockQueryAvailableOauthScopesService.execute() } returns buildScopeList("self:read")
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
                            clientName = "Empty Scope Client",
                            serviceName = "빈 스코프 서비스",
                            scopes = emptySet(),
                            redirectUrls = emptySet(),
                        )

                    beforeEach {
                        every { mockQueryAvailableOauthScopesService.execute() } returns buildScopeList("self:read")
                        every { mockCurrentUserProvider.getCurrentAccount() } returns mockAccount
                        every { mockPasswordEncoder.encode(any()) } returns "encoded_secret"
                        every { mockClientJpaRepository.save(any()) } answers {
                            firstArg<ClientJpaEntity>()
                        }
                    }

                    it("빈 scope로 클라이언트를 생성해야 한다") {
                        val result = createClientService.execute(reqDto)

                        result.clientId shouldNotBe null
                        result.clientName shouldBe "Empty Scope Client"

                        verify(exactly = 1) { mockClientJpaRepository.save(any()) }
                    }
                }
            }
        }
    })
