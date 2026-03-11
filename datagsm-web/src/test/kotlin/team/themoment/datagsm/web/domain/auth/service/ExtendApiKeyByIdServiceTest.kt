package team.themoment.datagsm.web.domain.auth.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.auth.entity.ApiKey
import team.themoment.datagsm.common.domain.auth.repository.ApiKeyJpaRepository
import team.themoment.datagsm.common.global.data.ApiKeyEnvironment
import team.themoment.datagsm.web.domain.auth.service.impl.ExtendApiKeyByIdServiceImpl
import team.themoment.sdk.exception.ExpectedException
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID

class ExtendApiKeyByIdServiceTest :
    DescribeSpec({

        val mockApiKeyRepository = mockk<ApiKeyJpaRepository>()
        val apiKeyEnvironment =
            ApiKeyEnvironment(
                expirationDays = 30L,
                renewalPeriodDays = 15L,
                adminExpirationDays = 365L,
                rateLimit = ApiKeyEnvironment.RateLimit(true, 100L, 100L, 60L),
            )

        val extendApiKeyByIdService =
            ExtendApiKeyByIdServiceImpl(
                mockApiKeyRepository,
                apiKeyEnvironment,
            )

        afterEach {
            clearAllMocks()
        }

        describe("ExtendApiKeyByIdService 클래스의") {
            describe("execute 메서드는") {

                val mockAccount =
                    AccountJpaEntity().apply {
                        id = 1L
                        email = "test@gsm.hs.kr"
                    }

                context("유효한 API 키 ID로 연장할 때") {
                    val apiKeyId = 1L
                    val apiKey =
                        ApiKey().apply {
                            id = apiKeyId
                            value = UUID.randomUUID()
                            account = mockAccount
                            createdAt = LocalDateTime.now().minusDays(10)
                            updatedAt = LocalDateTime.now().minusDays(10)
                            expiresAt = LocalDateTime.now().plusDays(5)
                            scopes = setOf("student:read", "club:read")
                            description = "테스트용 API 키"
                        }

                    beforeEach {
                        every { mockApiKeyRepository.findById(apiKeyId) } returns Optional.of(apiKey)
                    }

                    it("만료일이 현재 시각 기준 adminExpirationDays 후로 설정되고 마스킹된 값을 반환해야 한다") {
                        val beforeExecution = LocalDateTime.now()
                        val result = extendApiKeyByIdService.execute(apiKeyId)
                        val afterExecution = LocalDateTime.now()

                        result.id shouldBe apiKeyId
                        result.apiKey shouldBe apiKey.maskedValue
                        result.expiresInDays shouldBe 365L
                        result.scopes shouldBe apiKey.scopes
                        result.description shouldBe apiKey.description

                        val expectedMinExpiresAt = beforeExecution.plusDays(365)
                        val expectedMaxExpiresAt = afterExecution.plusDays(365)
                        result.expiresAt.isAfter(expectedMinExpiresAt.minusSeconds(1)) shouldBe true
                        result.expiresAt.isBefore(expectedMaxExpiresAt.plusSeconds(1)) shouldBe true

                        verify(exactly = 1) { mockApiKeyRepository.findById(apiKeyId) }
                    }
                }

                context("존재하지 않는 API 키 ID로 연장할 때") {
                    val nonExistentId = 999L

                    beforeEach {
                        every { mockApiKeyRepository.findById(nonExistentId) } returns Optional.empty()
                    }

                    it("404 ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                extendApiKeyByIdService.execute(nonExistentId)
                            }

                        exception.statusCode.value() shouldBe 404
                        exception.message shouldBe "API 키를 찾을 수 없습니다."

                        verify(exactly = 1) { mockApiKeyRepository.findById(nonExistentId) }
                    }
                }

                context("만료된 API 키를 연장할 때") {
                    val apiKeyId = 2L
                    val expiredApiKey =
                        ApiKey().apply {
                            id = apiKeyId
                            value = UUID.randomUUID()
                            account = mockAccount
                            createdAt = LocalDateTime.now().minusDays(60)
                            updatedAt = LocalDateTime.now().minusDays(60)
                            expiresAt = LocalDateTime.now().minusDays(1)
                            scopes = setOf("student:read")
                        }

                    beforeEach {
                        every { mockApiKeyRepository.findById(apiKeyId) } returns Optional.of(expiredApiKey)
                    }

                    it("400 ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                extendApiKeyByIdService.execute(apiKeyId)
                            }

                        exception.statusCode.value() shouldBe 400
                        exception.message shouldBe "만료된 API 키는 연장할 수 없습니다."

                        verify(exactly = 1) { mockApiKeyRepository.findById(apiKeyId) }
                    }
                }
            }
        }
    })
