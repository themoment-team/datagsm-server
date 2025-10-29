package team.themoment.datagsm.domain.auth.service

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import team.themoment.datagsm.domain.auth.entity.ApiKey
import team.themoment.datagsm.domain.auth.repository.ApiKeyJpaRepository
import team.themoment.datagsm.domain.auth.service.impl.CreateApiKeyServiceImpl
import team.themoment.datagsm.domain.student.entity.StudentJpaEntity
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

        val createApiKeyService =
            CreateApiKeyServiceImpl(
                mockApiKeyRepository,
                mockCurrentUserProvider,
                mockApiKeyEnvironment,
            )

        afterEach {
            clearAllMocks()
        }

        describe("CreateApiKeyService 클래스의") {
            describe("execute 메서드는") {

                val mockStudent =
                    StudentJpaEntity().apply {
                        studentId = 1L
                        studentEmail = "test@gsm.hs.kr"
                    }

                beforeEach {
                    every { mockCurrentUserProvider.getCurrentStudent() } returns mockStudent
                    every { mockApiKeyEnvironment.expirationDays } returns 30L
                }

                context("기존 API 키가 없을 때") {
                    beforeEach {
                        every { mockApiKeyRepository.findByApiKeyStudent(mockStudent) } returns Optional.empty()
                        every { mockApiKeyRepository.save(any()) } answers {
                            val entity = firstArg<ApiKey>()
                            entity.apply { apiKeyId = 1L }
                        }
                    }

                    it("새로운 API 키를 생성하고 반환해야 한다") {
                        val result = createApiKeyService.execute()

                        result.apiKey shouldNotBe null
                        result.expiresAt shouldNotBe null

                        verify(exactly = 1) { mockCurrentUserProvider.getCurrentStudent() }
                        verify(exactly = 1) { mockApiKeyRepository.findByApiKeyStudent(mockStudent) }
                        verify(exactly = 1) { mockApiKeyRepository.save(any()) }
                    }
                }

                context("기존 API 키가 있을 때") {
                    val oldApiKeyValue = UUID.randomUUID()
                    val oldExpiresAt = LocalDateTime.now().plusDays(10)
                    val existingApiKey =
                        ApiKey().apply {
                            apiKeyId = 1L
                            apiKeyValue = oldApiKeyValue
                            apiKeyStudent = mockStudent
                            createdAt = LocalDateTime.now().minusDays(20)
                            updatedAt = LocalDateTime.now().minusDays(20)
                            expiresAt = oldExpiresAt
                        }

                    beforeEach {
                        every { mockApiKeyRepository.findByApiKeyStudent(mockStudent) } returns
                            Optional.of(existingApiKey)
                        every { mockApiKeyRepository.save(existingApiKey) } returns existingApiKey
                    }

                    it("기존 API 키를 갱신하고 반환해야 한다") {
                        val result = createApiKeyService.execute()

                        result.apiKey shouldNotBe oldApiKeyValue
                        result.expiresAt shouldNotBe oldExpiresAt
                        existingApiKey.apiKeyValue shouldNotBe oldApiKeyValue

                        verify(exactly = 1) { mockCurrentUserProvider.getCurrentStudent() }
                        verify(exactly = 1) { mockApiKeyRepository.findByApiKeyStudent(mockStudent) }
                        verify(exactly = 1) { mockApiKeyRepository.save(existingApiKey) }
                    }
                }

                context("새로운 API 키 생성 시 만료일자가 설정될 때") {
                    val beforeExecution = LocalDateTime.now()

                    beforeEach {
                        every { mockApiKeyRepository.findByApiKeyStudent(mockStudent) } returns Optional.empty()
                        every { mockApiKeyRepository.save(any()) } answers {
                            val entity = firstArg<ApiKey>()
                            entity.apply { apiKeyId = 1L }
                        }
                    }

                    it("만료일자가 30일 후로 설정되어야 한다") {
                        val result = createApiKeyService.execute()

                        val afterExecution = LocalDateTime.now()
                        val expectedMinExpiresAt = beforeExecution.plusDays(30)
                        val expectedMaxExpiresAt = afterExecution.plusDays(30)

                        result.expiresAt.isAfter(expectedMinExpiresAt.minusSeconds(1)) shouldBe true
                        result.expiresAt.isBefore(expectedMaxExpiresAt.plusSeconds(1)) shouldBe true

                        verify(exactly = 1) { mockApiKeyRepository.save(any()) }
                    }
                }

                context("API 키 생성 시 생성일자가 현재 시각으로 설정될 때") {
                    lateinit var savedApiKey: ApiKey

                    beforeEach {
                        every { mockApiKeyRepository.findByApiKeyStudent(mockStudent) } returns Optional.empty()
                        every { mockApiKeyRepository.save(any()) } answers {
                            savedApiKey = firstArg<ApiKey>()
                            savedApiKey.apply { apiKeyId = 1L }
                        }
                    }

                    it("생성일자가 현재 시각으로 설정되어야 한다") {
                        val beforeExecution = LocalDateTime.now()
                        createApiKeyService.execute()
                        val afterExecution = LocalDateTime.now()

                        savedApiKey.createdAt.isAfter(beforeExecution.minusSeconds(1)) shouldBe true
                        savedApiKey.createdAt.isBefore(afterExecution.plusSeconds(1)) shouldBe true
                    }
                }
            }
        }
    })
