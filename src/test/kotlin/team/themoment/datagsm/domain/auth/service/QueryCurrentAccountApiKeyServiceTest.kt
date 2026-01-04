package team.themoment.datagsm.domain.auth.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import team.themoment.datagsm.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.domain.auth.entity.ApiKey
import team.themoment.datagsm.domain.auth.repository.ApiKeyJpaRepository
import team.themoment.datagsm.domain.auth.service.impl.QueryCurrentAccountApiKeyServiceImpl
import team.themoment.datagsm.global.exception.error.ExpectedException
import team.themoment.datagsm.global.security.provider.CurrentUserProvider
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID

class QueryCurrentAccountApiKeyServiceTest :
    DescribeSpec({

        val mockApiKeyRepository = mockk<ApiKeyJpaRepository>()
        val mockCurrentUserProvider = mockk<CurrentUserProvider>()

        val queryApiKeyService =
            QueryCurrentAccountApiKeyServiceImpl(
                mockApiKeyRepository,
                mockCurrentUserProvider,
            )

        afterEach {
            clearAllMocks()
        }

        describe("QueryCurrentAccountApiKeyService 클래스의") {
            describe("execute 메서드는") {

                val mockAccount =
                    AccountJpaEntity().apply {
                        id = 1L
                        email = "test@gsm.hs.kr"
                    }

                beforeEach {
                    every { mockCurrentUserProvider.getCurrentAccount() } returns mockAccount
                }

                context("API 키가 존재할 때") {
                    val apiKeyValue = UUID.randomUUID()
                    val expiresAt = LocalDateTime.now().plusDays(30)
                    val testScopes = setOf("student:read", "club:write")
                    val testDescription = "테스트용 API 키"
                    val apiKey =
                        ApiKey().apply {
                            id = 1L
                            this.value = apiKeyValue
                            account = mockAccount
                            createdAt = LocalDateTime.now()
                            updatedAt = LocalDateTime.now()
                            this.expiresAt = expiresAt
                            updateScopes(testScopes)
                            this.description = testDescription
                        }

                    beforeEach {
                        every { mockApiKeyRepository.findByAccount(mockAccount) } returns Optional.of(apiKey)
                    }

                    it("API 키 정보를 마스킹하여 반환해야 한다") {
                        val result = queryApiKeyService.execute()

                        result.id shouldBe 1L

                        val maskedPattern = Regex("^[0-9a-f]{8}-\\*{4}-\\*{4}-\\*{4}-\\*{8}[0-9a-f]{4}$")
                        result.apiKey.matches(maskedPattern) shouldBe true

                        result.apiKey.take(8) shouldBe apiKeyValue.toString().take(8)
                        result.apiKey.takeLast(4) shouldBe apiKeyValue.toString().takeLast(4)

                        result.expiresAt shouldBe expiresAt
                        result.scopes shouldBe testScopes
                        result.description shouldBe testDescription

                        verify(exactly = 1) { mockCurrentUserProvider.getCurrentAccount() }
                        verify(exactly = 1) { mockApiKeyRepository.findByAccount(mockAccount) }
                    }
                }

                context("API 키가 존재하지 않을 때") {
                    beforeEach {
                        every { mockApiKeyRepository.findByAccount(mockAccount) } returns Optional.empty()
                    }

                    it("404 ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                queryApiKeyService.execute()
                            }

                        exception.statusCode.value() shouldBe 404
                        exception.message shouldBe "API 키를 찾을 수 없습니다."

                        verify(exactly = 1) { mockCurrentUserProvider.getCurrentAccount() }
                        verify(exactly = 1) { mockApiKeyRepository.findByAccount(mockAccount) }
                    }
                }
            }
        }
    })
