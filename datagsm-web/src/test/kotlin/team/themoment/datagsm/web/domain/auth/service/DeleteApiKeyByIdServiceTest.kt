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
import team.themoment.datagsm.web.domain.auth.service.impl.DeleteApiKeyByIdServiceImpl
import team.themoment.sdk.exception.ExpectedException
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID

class DeleteApiKeyByIdServiceTest :
    DescribeSpec({

        val mockApiKeyRepository = mockk<ApiKeyJpaRepository>()

        val deleteApiKeyByIdService = DeleteApiKeyByIdServiceImpl(mockApiKeyRepository)

        afterEach {
            clearAllMocks()
        }

        describe("DeleteApiKeyByIdService 클래스의") {
            describe("execute 메서드는") {

                val mockAccount =
                    AccountJpaEntity().apply {
                        id = 1L
                        email = "test@gsm.hs.kr"
                    }

                context("존재하는 API 키 ID로 삭제할 때") {
                    val apiKeyId = 1L
                    val apiKey =
                        ApiKey().apply {
                            id = apiKeyId
                            value = UUID.randomUUID()
                            account = mockAccount
                            createdAt = LocalDateTime.now()
                            updatedAt = LocalDateTime.now()
                            expiresAt = LocalDateTime.now().plusDays(30)
                        }

                    beforeEach {
                        every { mockApiKeyRepository.findById(apiKeyId) } returns Optional.of(apiKey)
                        every { mockApiKeyRepository.delete(apiKey) } returns Unit
                    }

                    it("API 키를 삭제해야 한다") {
                        deleteApiKeyByIdService.execute(apiKeyId)

                        verify(exactly = 1) { mockApiKeyRepository.findById(apiKeyId) }
                        verify(exactly = 1) { mockApiKeyRepository.delete(apiKey) }
                    }
                }

                context("존재하지 않는 API 키 ID로 삭제할 때") {
                    val nonExistentId = 999L

                    beforeEach {
                        every { mockApiKeyRepository.findById(nonExistentId) } returns Optional.empty()
                    }

                    it("404 ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                deleteApiKeyByIdService.execute(nonExistentId)
                            }

                        exception.statusCode.value() shouldBe 404
                        exception.message shouldBe "API 키를 찾을 수 없습니다."

                        verify(exactly = 1) { mockApiKeyRepository.findById(nonExistentId) }
                        verify(exactly = 0) { mockApiKeyRepository.delete(any()) }
                    }
                }

                context("여러 개의 API 키를 순차적으로 삭제할 때") {
                    val apiKeyId1 = 1L
                    val apiKeyId2 = 2L
                    val apiKey1 =
                        ApiKey().apply {
                            id = apiKeyId1
                            value = UUID.randomUUID()
                            account = mockAccount
                            createdAt = LocalDateTime.now()
                            updatedAt = LocalDateTime.now()
                            expiresAt = LocalDateTime.now().plusDays(30)
                        }
                    val apiKey2 =
                        ApiKey().apply {
                            id = apiKeyId2
                            value = UUID.randomUUID()
                            account = mockAccount
                            createdAt = LocalDateTime.now()
                            updatedAt = LocalDateTime.now()
                            expiresAt = LocalDateTime.now().plusDays(30)
                        }

                    beforeEach {
                        every { mockApiKeyRepository.findById(apiKeyId1) } returns Optional.of(apiKey1)
                        every { mockApiKeyRepository.findById(apiKeyId2) } returns Optional.of(apiKey2)
                        every { mockApiKeyRepository.delete(apiKey1) } returns Unit
                        every { mockApiKeyRepository.delete(apiKey2) } returns Unit
                    }

                    it("각각의 API 키가 정상적으로 삭제되어야 한다") {
                        deleteApiKeyByIdService.execute(apiKeyId1)
                        deleteApiKeyByIdService.execute(apiKeyId2)

                        verify(exactly = 1) { mockApiKeyRepository.findById(apiKeyId1) }
                        verify(exactly = 1) { mockApiKeyRepository.findById(apiKeyId2) }
                        verify(exactly = 1) { mockApiKeyRepository.delete(apiKey1) }
                        verify(exactly = 1) { mockApiKeyRepository.delete(apiKey2) }
                    }
                }

                context("동일한 API 키 ID로 여러 번 삭제를 시도할 때") {
                    val apiKeyId = 1L
                    val apiKey =
                        ApiKey().apply {
                            id = apiKeyId
                            value = UUID.randomUUID()
                            account = mockAccount
                            createdAt = LocalDateTime.now()
                            updatedAt = LocalDateTime.now()
                            expiresAt = LocalDateTime.now().plusDays(30)
                        }

                    beforeEach {
                        every { mockApiKeyRepository.findById(apiKeyId) } returnsMany
                            listOf(
                                Optional.of(apiKey),
                                Optional.empty(),
                            )
                        every { mockApiKeyRepository.delete(apiKey) } returns Unit
                    }

                    it("첫 번째는 성공하고 두 번째는 404 예외가 발생해야 한다") {
                        deleteApiKeyByIdService.execute(apiKeyId)

                        val exception =
                            shouldThrow<ExpectedException> {
                                deleteApiKeyByIdService.execute(apiKeyId)
                            }

                        exception.statusCode.value() shouldBe 404
                        exception.message shouldBe "API 키를 찾을 수 없습니다."

                        verify(exactly = 2) { mockApiKeyRepository.findById(apiKeyId) }
                        verify(exactly = 1) { mockApiKeyRepository.delete(apiKey) }
                    }
                }
            }
        }
    })
