package team.themoment.datagsm.domain.auth.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.web.server.ResponseStatusException
import team.themoment.datagsm.domain.auth.entity.ApiKey
import team.themoment.datagsm.domain.auth.repository.ApiKeyJpaRepository
import team.themoment.datagsm.domain.auth.service.impl.QueryApiKeyRenewableServiceImpl
import team.themoment.datagsm.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.global.security.data.ApiKeyEnvironment
import team.themoment.datagsm.global.security.provider.CurrentUserProvider
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID

class QueryApiKeyRenewableServiceTest :
    DescribeSpec({

        val mockApiKeyRepository = mockk<ApiKeyJpaRepository>()
        val mockCurrentUserProvider = mockk<CurrentUserProvider>()
        val mockApiKeyEnvironment = mockk<ApiKeyEnvironment>()

        val queryApiKeyRenewableService =
            QueryApiKeyRenewableServiceImpl(
                mockApiKeyRepository,
                mockCurrentUserProvider,
                mockApiKeyEnvironment,
            )

        afterEach {
            clearAllMocks()
        }

        describe("QueryApiKeyRenewableService 클래스의") {
            describe("execute 메서드는") {

                val mockStudent =
                    StudentJpaEntity().apply {
                        studentId = 1L
                        studentEmail = "test@gsm.hs.kr"
                    }

                beforeEach {
                    every { mockCurrentUserProvider.getCurrentStudent() } returns mockStudent
                    every { mockApiKeyEnvironment.renewalPeriodDays } returns 15L
                }

                context("API 키가 존재하지 않을 때") {
                    beforeEach {
                        every { mockApiKeyRepository.findByApiKeyStudent(mockStudent) } returns Optional.empty()
                    }

                    it("404 ResponseStatusException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ResponseStatusException> {
                                queryApiKeyRenewableService.execute()
                            }

                        exception.statusCode.value() shouldBe 404
                        exception.reason shouldBe "API 키를 찾을 수 없습니다."

                        verify(exactly = 1) { mockCurrentUserProvider.getCurrentStudent() }
                        verify(exactly = 1) { mockApiKeyRepository.findByApiKeyStudent(mockStudent) }
                    }
                }

                context("API 키가 갱신 가능할 때 (만료 10일 전)") {
                    val now = LocalDateTime.now()
                    val expiresAt = now.plusDays(10)
                    val apiKey =
                        ApiKey().apply {
                            apiKeyId = 1L
                            apiKeyValue = UUID.randomUUID()
                            apiKeyStudent = mockStudent
                            createdAt = now.minusDays(20)
                            updatedAt = now.minusDays(20)
                            this.expiresAt = expiresAt
                        }

                    beforeEach {
                        every { mockApiKeyRepository.findByApiKeyStudent(mockStudent) } returns Optional.of(apiKey)
                    }

                    it("renewable이 true를 반환해야 한다") {
                        val result = queryApiKeyRenewableService.execute()

                        result.renewable shouldBe true

                        verify(exactly = 1) { mockCurrentUserProvider.getCurrentStudent() }
                        verify(exactly = 1) { mockApiKeyRepository.findByApiKeyStudent(mockStudent) }
                    }
                }

                context("API 키가 갱신 불가능할 때 (만료 20일 전)") {
                    val now = LocalDateTime.now()
                    val expiresAt = now.plusDays(20)
                    val apiKey =
                        ApiKey().apply {
                            apiKeyId = 1L
                            apiKeyValue = UUID.randomUUID()
                            apiKeyStudent = mockStudent
                            createdAt = now.minusDays(10)
                            updatedAt = now.minusDays(10)
                            this.expiresAt = expiresAt
                        }

                    beforeEach {
                        every { mockApiKeyRepository.findByApiKeyStudent(mockStudent) } returns Optional.of(apiKey)
                    }

                    it("renewable이 false를 반환해야 한다") {
                        val result = queryApiKeyRenewableService.execute()

                        result.renewable shouldBe false

                        verify(exactly = 1) { mockCurrentUserProvider.getCurrentStudent() }
                        verify(exactly = 1) { mockApiKeyRepository.findByApiKeyStudent(mockStudent) }
                    }
                }

                context("API 키가 만료되었지만 갱신 가능할 때 (만료 5일 후)") {
                    val now = LocalDateTime.now()
                    val expiresAt = now.minusDays(5)
                    val apiKey =
                        ApiKey().apply {
                            apiKeyId = 1L
                            apiKeyValue = UUID.randomUUID()
                            apiKeyStudent = mockStudent
                            createdAt = now.minusDays(35)
                            updatedAt = now.minusDays(35)
                            this.expiresAt = expiresAt
                        }

                    beforeEach {
                        every { mockApiKeyRepository.findByApiKeyStudent(mockStudent) } returns Optional.of(apiKey)
                    }

                    it("renewable이 true를 반환해야 한다") {
                        val result = queryApiKeyRenewableService.execute()

                        result.renewable shouldBe true

                        verify(exactly = 1) { mockCurrentUserProvider.getCurrentStudent() }
                        verify(exactly = 1) { mockApiKeyRepository.findByApiKeyStudent(mockStudent) }
                    }
                }

                context("API 키가 만료되고 갱신 기간도 지났을 때 (만료 20일 후)") {
                    val now = LocalDateTime.now()
                    val expiresAt = now.minusDays(20)
                    val apiKey =
                        ApiKey().apply {
                            apiKeyId = 1L
                            apiKeyValue = UUID.randomUUID()
                            apiKeyStudent = mockStudent
                            createdAt = now.minusDays(50)
                            updatedAt = now.minusDays(50)
                            this.expiresAt = expiresAt
                        }

                    beforeEach {
                        every { mockApiKeyRepository.findByApiKeyStudent(mockStudent) } returns Optional.of(apiKey)
                    }

                    it("renewable이 false를 반환해야 한다") {
                        val result = queryApiKeyRenewableService.execute()

                        result.renewable shouldBe false

                        verify(exactly = 1) { mockCurrentUserProvider.getCurrentStudent() }
                        verify(exactly = 1) { mockApiKeyRepository.findByApiKeyStudent(mockStudent) }
                    }
                }
            }
        }
    })
