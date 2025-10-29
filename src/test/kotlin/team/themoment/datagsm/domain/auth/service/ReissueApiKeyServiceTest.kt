package team.themoment.datagsm.domain.auth.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.web.server.ResponseStatusException
import team.themoment.datagsm.domain.auth.entity.ApiKey
import team.themoment.datagsm.domain.auth.repository.ApiKeyJpaRepository
import team.themoment.datagsm.domain.auth.service.impl.ReissueApiKeyServiceImpl
import team.themoment.datagsm.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.global.security.data.ApiKeyEnvironment
import team.themoment.datagsm.global.security.provider.CurrentUserProvider
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID

class ReissueApiKeyServiceTest :
    DescribeSpec({

        val mockApiKeyRepository = mockk<ApiKeyJpaRepository>()
        val mockCurrentUserProvider = mockk<CurrentUserProvider>()
        val mockApiKeyEnvironment = mockk<ApiKeyEnvironment>()

        val reissueApiKeyService =
            ReissueApiKeyServiceImpl(
                mockApiKeyRepository,
                mockCurrentUserProvider,
                mockApiKeyEnvironment,
            )

        afterEach {
            clearAllMocks()
        }

        describe("ReissueApiKeyService 클래스의") {
            describe("execute 메서드는") {

                val mockStudent =
                    StudentJpaEntity().apply {
                        studentId = 1L
                        studentEmail = "test@gsm.hs.kr"
                    }

                beforeEach {
                    every { mockCurrentUserProvider.getCurrentStudent() } returns mockStudent
                    every { mockApiKeyEnvironment.expirationDays } returns 30L
                    every { mockApiKeyEnvironment.renewalPeriodDays } returns 15L
                }

                context("API 키를 찾을 수 없을 때") {
                    beforeEach {
                        every { mockApiKeyRepository.findByApiKeyStudent(mockStudent) } returns Optional.empty()
                    }

                    it("ResponseStatusException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ResponseStatusException> {
                                reissueApiKeyService.execute()
                            }

                        exception.statusCode.value() shouldBe 404
                        exception.reason shouldBe "API 키를 찾을 수 없습니다."

                        verify(exactly = 1) { mockCurrentUserProvider.getCurrentStudent() }
                        verify(exactly = 1) { mockApiKeyRepository.findByApiKeyStudent(mockStudent) }
                        verify(exactly = 0) { mockApiKeyRepository.save(any()) }
                        verify(exactly = 0) { mockApiKeyRepository.delete(any()) }
                    }
                }

                context("갱신 기간 이전일 때") {
                    val now = LocalDateTime.now()
                    val expiresAt = now.plusDays(20)
                    val apiKey =
                        ApiKey().apply {
                            apiKeyId = 1L
                            apiKeyValue = UUID.randomUUID()
                            apiKeyStudent = mockStudent
                            createdAt = now.minusDays(10)
                            this.expiresAt = expiresAt
                        }

                    beforeEach {
                        every { mockApiKeyRepository.findByApiKeyStudent(mockStudent) } returns Optional.of(apiKey)
                    }

                    it("ResponseStatusException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ResponseStatusException> {
                                reissueApiKeyService.execute()
                            }

                        exception.statusCode.value() shouldBe 400
                        exception.reason shouldBe "API 키 갱신 기간이 아닙니다. 만료 15일 전부터 만료 15일 후까지만 갱신 가능합니다."

                        verify(exactly = 1) { mockApiKeyRepository.findByApiKeyStudent(mockStudent) }
                        verify(exactly = 0) { mockApiKeyRepository.save(any()) }
                        verify(exactly = 0) { mockApiKeyRepository.delete(any()) }
                    }
                }

                context("갱신 기간이 지났을 때") {
                    val now = LocalDateTime.now()
                    val expiresAt = now.minusDays(20)
                    val apiKey =
                        ApiKey().apply {
                            apiKeyId = 1L
                            apiKeyValue = UUID.randomUUID()
                            apiKeyStudent = mockStudent
                            createdAt = now.minusDays(50)
                            this.expiresAt = expiresAt
                        }

                    beforeEach {
                        every { mockApiKeyRepository.findByApiKeyStudent(mockStudent) } returns Optional.of(apiKey)
                        every { mockApiKeyRepository.delete(apiKey) } returns Unit
                    }

                    it("API 키가 삭제되고 410 GONE 예외가 발생해야 한다") {
                        val exception =
                            shouldThrow<ResponseStatusException> {
                                reissueApiKeyService.execute()
                            }

                        exception.statusCode.value() shouldBe 410
                        exception.reason shouldBe "API 키 갱신 기간이 지났습니다. 해당 API 키는 삭제되었습니다."

                        verify(exactly = 1) { mockApiKeyRepository.findByApiKeyStudent(mockStudent) }
                        verify(exactly = 1) { mockApiKeyRepository.delete(apiKey) }
                        verify(exactly = 0) { mockApiKeyRepository.save(any()) }
                    }
                }

                context("정상적으로 갱신할 때 (만료 15일 전)") {
                    val now = LocalDateTime.now()
                    val expiresAt = now.plusDays(10)
                    val oldApiKeyValue = UUID.randomUUID()
                    val apiKey =
                        ApiKey().apply {
                            apiKeyId = 1L
                            apiKeyValue = oldApiKeyValue
                            apiKeyStudent = mockStudent
                            createdAt = now.minusDays(20)
                            this.expiresAt = expiresAt
                        }

                    beforeEach {
                        every { mockApiKeyRepository.findByApiKeyStudent(mockStudent) } returns Optional.of(apiKey)
                        every { mockApiKeyRepository.save(apiKey) } returns apiKey
                    }

                    it("기존 API 키를 유지하고 만료일자만 갱신해야 한다") {
                        val result = reissueApiKeyService.execute()

                        result.apiKey shouldBe oldApiKeyValue
                        result.expiresAt shouldNotBe null

                        verify(exactly = 1) { mockApiKeyRepository.findByApiKeyStudent(mockStudent) }
                        verify(exactly = 1) { mockApiKeyRepository.save(apiKey) }
                        verify(exactly = 0) { mockApiKeyRepository.delete(any()) }
                    }
                }

                context("정상적으로 갱신할 때 (만료 후 5일)") {
                    val now = LocalDateTime.now()
                    val expiresAt = now.minusDays(5)
                    val oldApiKeyValue = UUID.randomUUID()
                    val apiKey =
                        ApiKey().apply {
                            apiKeyId = 1L
                            apiKeyValue = oldApiKeyValue
                            apiKeyStudent = mockStudent
                            createdAt = now.minusDays(35)
                            this.expiresAt = expiresAt
                        }

                    beforeEach {
                        every { mockApiKeyRepository.findByApiKeyStudent(mockStudent) } returns Optional.of(apiKey)
                        every { mockApiKeyRepository.save(apiKey) } returns apiKey
                    }

                    it("기존 API 키를 유지하고 만료일자만 갱신해야 한다") {
                        val result = reissueApiKeyService.execute()

                        result.apiKey shouldBe oldApiKeyValue
                        apiKey.apiKeyValue shouldBe oldApiKeyValue
                        apiKey.expiresAt shouldNotBe expiresAt

                        verify(exactly = 1) { mockApiKeyRepository.findByApiKeyStudent(mockStudent) }
                        verify(exactly = 1) { mockApiKeyRepository.save(apiKey) }
                        verify(exactly = 0) { mockApiKeyRepository.delete(any()) }
                    }
                }

                context("갱신 기간 경계값 테스트 - 정확히 만료 15일 전") {
                    val now = LocalDateTime.now()
                    val expiresAt = now.plusDays(15)
                    val apiKey =
                        ApiKey().apply {
                            apiKeyId = 1L
                            apiKeyValue = UUID.randomUUID()
                            apiKeyStudent = mockStudent
                            createdAt = now.minusDays(15)
                            this.expiresAt = expiresAt
                        }

                    beforeEach {
                        every { mockApiKeyRepository.findByApiKeyStudent(mockStudent) } returns Optional.of(apiKey)
                        every { mockApiKeyRepository.save(apiKey) } returns apiKey
                    }

                    it("갱신이 가능해야 한다") {
                        val result = reissueApiKeyService.execute()

                        result.apiKey shouldNotBe null

                        verify(exactly = 1) { mockApiKeyRepository.save(apiKey) }
                        verify(exactly = 0) { mockApiKeyRepository.delete(any()) }
                    }
                }

                context("갱신 기간 경계값 테스트 - 정확히 만료 15일 후") {
                    val now = LocalDateTime.now()
                    val expiresAt = now.minusDays(15)
                    val apiKey =
                        ApiKey().apply {
                            apiKeyId = 1L
                            apiKeyValue = UUID.randomUUID()
                            apiKeyStudent = mockStudent
                            createdAt = now.minusDays(45)
                            this.expiresAt = expiresAt
                        }

                    beforeEach {
                        every { mockApiKeyRepository.findByApiKeyStudent(mockStudent) } returns Optional.of(apiKey)
                        every { mockApiKeyRepository.delete(apiKey) } returns Unit
                    }

                    it("갱신 기간이 지나 삭제되어야 한다") {
                        val exception =
                            shouldThrow<ResponseStatusException> {
                                reissueApiKeyService.execute()
                            }

                        exception.statusCode.value() shouldBe 410

                        verify(exactly = 1) { mockApiKeyRepository.delete(apiKey) }
                        verify(exactly = 0) { mockApiKeyRepository.save(any()) }
                    }
                }
            }
        }
    })
