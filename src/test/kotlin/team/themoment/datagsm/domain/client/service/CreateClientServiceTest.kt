package team.themoment.datagsm.domain.client.service

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.security.crypto.password.PasswordEncoder
import team.themoment.datagsm.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.domain.client.dto.req.CreateClientReqDto
import team.themoment.datagsm.domain.client.entity.ClientJpaEntity
import team.themoment.datagsm.domain.client.repository.ClientJpaRepository
import team.themoment.datagsm.domain.client.service.impl.CreateClientServiceImpl
import team.themoment.datagsm.global.security.provider.CurrentUserProvider

class CreateClientServiceTest :
    DescribeSpec({

        val mockCurrentUserProvider = mockk<CurrentUserProvider>()
        val mockPasswordEncoder = mockk<PasswordEncoder>()
        val mockClientRepository = mockk<ClientJpaRepository>()

        val createClientService =
            CreateClientServiceImpl(
                mockCurrentUserProvider,
                mockPasswordEncoder,
                mockClientRepository,
            )

        afterEach {
            clearAllMocks()
        }

        describe("CreateClientService 클래스의") {
            describe("execute 메서드는") {

                context("유효한 클라이언트 이름으로 생성 요청할 때") {
                    val createRequest = CreateClientReqDto(name = "테스트 클라이언트")

                    val currentAccount =
                        AccountJpaEntity().apply {
                            id = 1L
                            email = "test@gsm.hs.kr"
                            role = AccountRole.USER
                        }

                    beforeEach {
                        every { mockCurrentUserProvider.getCurrentAccount() } returns currentAccount
                        every { mockPasswordEncoder.encode(any()) } returns "encoded-secret"
                        every { mockClientRepository.save(any()) } answers {
                            val client = firstArg<ClientJpaEntity>()
                            client
                        }
                    }

                    it("새로운 클라이언트를 생성하고 저장 후 결과를 반환한다") {
                        val result = createClientService.execute(createRequest)

                        result.clientId shouldNotBe null
                        result.clientSecret shouldNotBe null
                        result.name shouldBe "테스트 클라이언트"
                        result.redirectUri shouldBe emptyList()

                        verify(exactly = 1) { mockCurrentUserProvider.getCurrentAccount() }
                        verify(exactly = 1) { mockPasswordEncoder.encode(any()) }
                        verify(exactly = 1) { mockClientRepository.save(any()) }
                    }
                }

                context("여러 클라이언트를 연속으로 생성할 때") {
                    val createRequest1 = CreateClientReqDto(name = "첫번째 클라이언트")
                    val createRequest2 = CreateClientReqDto(name = "두번째 클라이언트")

                    val currentAccount =
                        AccountJpaEntity().apply {
                            id = 1L
                            email = "test@gsm.hs.kr"
                            role = AccountRole.USER
                        }

                    beforeEach {
                        every { mockCurrentUserProvider.getCurrentAccount() } returns currentAccount
                        every { mockPasswordEncoder.encode(any()) } returns "encoded-secret"
                        every { mockClientRepository.save(any()) } answers {
                            val client = firstArg<ClientJpaEntity>()
                            client
                        }
                    }

                    it("각각 다른 클라이언트가 생성되어야 한다") {
                        val result1 = createClientService.execute(createRequest1)
                        val result2 = createClientService.execute(createRequest2)

                        result1.name shouldBe "첫번째 클라이언트"
                        result2.name shouldBe "두번째 클라이언트"
                        result1.clientId shouldNotBe result2.clientId

                        verify(exactly = 2) { mockCurrentUserProvider.getCurrentAccount() }
                        verify(exactly = 2) { mockPasswordEncoder.encode(any()) }
                        verify(exactly = 2) { mockClientRepository.save(any()) }
                    }
                }

                context("관리자 계정으로 클라이언트 생성 요청할 때") {
                    val createRequest = CreateClientReqDto(name = "관리자 클라이언트")

                    val adminAccount =
                        AccountJpaEntity().apply {
                            id = 2L
                            email = "admin@gsm.hs.kr"
                            role = AccountRole.ADMIN
                        }

                    beforeEach {
                        every { mockCurrentUserProvider.getCurrentAccount() } returns adminAccount
                        every { mockPasswordEncoder.encode(any()) } returns "encoded-secret"
                        every { mockClientRepository.save(any()) } answers {
                            val client = firstArg<ClientJpaEntity>()
                            client
                        }
                    }

                    it("관리자 계정으로 클라이언트가 생성되어야 한다") {
                        val result = createClientService.execute(createRequest)

                        result.name shouldBe "관리자 클라이언트"
                        result.clientId shouldNotBe null

                        verify(exactly = 1) { mockCurrentUserProvider.getCurrentAccount() }
                    }
                }
            }
        }
    })
