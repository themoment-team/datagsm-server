package team.themoment.datagsm.web.domain.auth.service

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import team.themoment.datagsm.common.domain.auth.repository.ApiKeyJpaRepository
import team.themoment.datagsm.web.domain.auth.service.impl.ExpireApiKeyServiceImpl
import java.time.LocalDateTime

class ExpireApiKeyServiceTest :
    DescribeSpec({

        val mockApiKeyRepository = mockk<ApiKeyJpaRepository>()

        val expireApiKeyService = ExpireApiKeyServiceImpl(mockApiKeyRepository)

        afterEach {
            clearAllMocks()
        }

        describe("ExpireApiKeyService 클래스의") {
            describe("execute 메서드는") {

                context("만료된 키가 존재할 때") {
                    val cutoffDate = LocalDateTime.now()

                    beforeEach {
                        every { mockApiKeyRepository.deleteExpiredKeys(cutoffDate) } returns 3L
                    }

                    it("삭제된 건수(3L)를 반환해야 한다") {
                        val result = expireApiKeyService.execute(cutoffDate)

                        result shouldBe 3L
                        verify(exactly = 1) { mockApiKeyRepository.deleteExpiredKeys(cutoffDate) }
                    }
                }

                context("만료된 키가 없을 때") {
                    val cutoffDate = LocalDateTime.now()

                    beforeEach {
                        every { mockApiKeyRepository.deleteExpiredKeys(cutoffDate) } returns 0L
                    }

                    it("0L을 반환해야 한다") {
                        val result = expireApiKeyService.execute(cutoffDate)

                        result shouldBe 0L
                        verify(exactly = 1) { mockApiKeyRepository.deleteExpiredKeys(cutoffDate) }
                    }
                }

                context("cutoffDate가 과거 시점일 때") {
                    val pastCutoffDate = LocalDateTime.now().minusDays(30)

                    beforeEach {
                        every { mockApiKeyRepository.deleteExpiredKeys(pastCutoffDate) } returns 5L
                    }

                    it("해당 기준으로 삭제된 건수를 반환해야 한다") {
                        val result = expireApiKeyService.execute(pastCutoffDate)

                        result shouldBe 5L
                        verify(exactly = 1) { mockApiKeyRepository.deleteExpiredKeys(pastCutoffDate) }
                    }
                }
            }
        }
    })
