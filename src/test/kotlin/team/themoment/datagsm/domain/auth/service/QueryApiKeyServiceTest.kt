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
import team.themoment.datagsm.domain.auth.service.impl.QueryApiKeyServiceImpl
import team.themoment.datagsm.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.global.security.provider.CurrentUserProvider
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID

class QueryApiKeyServiceTest :
    DescribeSpec({

        val mockApiKeyRepository = mockk<ApiKeyJpaRepository>()
        val mockCurrentUserProvider = mockk<CurrentUserProvider>()

        val queryApiKeyService =
            QueryApiKeyServiceImpl(
                mockApiKeyRepository,
                mockCurrentUserProvider,
            )

        afterEach {
            clearAllMocks()
        }

        describe("QueryApiKeyService 클래스의") {
            describe("execute 메서드는") {

                val mockStudent =
                    StudentJpaEntity().apply {
                        studentId = 1L
                        studentEmail = "test@gsm.hs.kr"
                    }

                beforeEach {
                    every { mockCurrentUserProvider.getCurrentStudent() } returns mockStudent
                }

                context("API 키가 존재할 때") {
                    val apiKeyValue = UUID.randomUUID()
                    val expiresAt = LocalDateTime.now().plusDays(30)
                    val apiKey =
                        ApiKey().apply {
                            apiKeyId = 1L
                            this.apiKeyValue = apiKeyValue
                            apiKeyStudent = mockStudent
                            createdAt = LocalDateTime.now()
                            updatedAt = LocalDateTime.now()
                            this.expiresAt = expiresAt
                        }

                    beforeEach {
                        every { mockApiKeyRepository.findByApiKeyStudent(mockStudent) } returns Optional.of(apiKey)
                    }

                    it("API 키 정보를 반환해야 한다") {
                        val result = queryApiKeyService.execute()

                        result.apiKey shouldBe apiKeyValue
                        result.expiresAt shouldBe expiresAt

                        verify(exactly = 1) { mockCurrentUserProvider.getCurrentStudent() }
                        verify(exactly = 1) { mockApiKeyRepository.findByApiKeyStudent(mockStudent) }
                    }
                }

                context("API 키가 존재하지 않을 때") {
                    beforeEach {
                        every { mockApiKeyRepository.findByApiKeyStudent(mockStudent) } returns Optional.empty()
                    }

                    it("404 ResponseStatusException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ResponseStatusException> {
                                queryApiKeyService.execute()
                            }

                        exception.statusCode.value() shouldBe 404
                        exception.reason shouldBe "API 키를 찾을 수 없습니다."

                        verify(exactly = 1) { mockCurrentUserProvider.getCurrentStudent() }
                        verify(exactly = 1) { mockApiKeyRepository.findByApiKeyStudent(mockStudent) }
                    }
                }
            }
        }
    })
