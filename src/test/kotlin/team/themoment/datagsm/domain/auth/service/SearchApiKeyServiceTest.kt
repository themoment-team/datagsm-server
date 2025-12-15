package team.themoment.datagsm.domain.auth.service

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import team.themoment.datagsm.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.domain.auth.entity.ApiKey
import team.themoment.datagsm.domain.auth.repository.ApiKeyJpaRepository
import team.themoment.datagsm.domain.auth.service.impl.SearchApiKeyServiceImpl
import java.time.LocalDateTime
import java.util.UUID

class SearchApiKeyServiceTest :
    DescribeSpec({

        val mockApiKeyRepository = mockk<ApiKeyJpaRepository>()

        val searchApiKeyService =
            SearchApiKeyServiceImpl(
                mockApiKeyRepository,
            )

        afterEach {
            clearAllMocks()
        }

        describe("SearchApiKeyService 클래스의") {
            describe("execute 메서드는") {

                val mockAccount1 =
                    AccountJpaEntity().apply {
                        id = 1L
                        email = "test1@gsm.hs.kr"
                    }

                val mockAccount2 =
                    AccountJpaEntity().apply {
                        id = 2L
                        email = "test2@gsm.hs.kr"
                    }

                val testUuid1 = UUID.fromString("550e8400-e29b-41d4-a716-446655440000")
                val testUuid2 = UUID.fromString("6ba7b810-9dad-11d1-80b4-00c04fd430c8")

                val apiKey1 =
                    ApiKey().apply {
                        id = 1L
                        value = testUuid1
                        account = mockAccount1
                        createdAt = LocalDateTime.now()
                        updatedAt = LocalDateTime.now()
                        expiresAt = LocalDateTime.now().plusDays(30)
                        updateScopes(setOf("student:read", "club:write"))
                        description = "테스트 API 키 1"
                    }

                val apiKey2 =
                    ApiKey().apply {
                        id = 2L
                        value = testUuid2
                        account = mockAccount2
                        createdAt = LocalDateTime.now()
                        updatedAt = LocalDateTime.now()
                        expiresAt = LocalDateTime.now().minusDays(5)
                        updateScopes(setOf("student:write"))
                        description = "테스트 API 키 2"
                    }

                context("필터 조건 없이 모든 API 키를 검색할 때") {
                    val apiKeys = listOf(apiKey1, apiKey2)
                    val pageable = PageRequest.of(0, 100)
                    val page = PageImpl(apiKeys, pageable, apiKeys.size.toLong())

                    beforeEach {
                        every {
                            mockApiKeyRepository.searchApiKeyWithPaging(
                                id = null,
                                accountId = null,
                                scope = null,
                                isExpired = null,
                                isRenewable = null,
                                pageable = pageable,
                            )
                        } returns page
                    }

                    it("모든 API 키 목록을 마스킹하여 반환해야 한다") {
                        val result =
                            searchApiKeyService.execute(
                                id = null,
                                accountId = null,
                                scope = null,
                                isExpired = null,
                                isRenewable = null,
                                page = 0,
                                size = 100,
                            )

                        result.totalPages shouldBe 1
                        result.totalElements shouldBe 2
                        result.apiKeys.size shouldBe 2

                        // API 키가 마스킹되었는지 확인
                        result.apiKeys[0].apiKey shouldBe "550e8400-****-****-****-********0000"
                        result.apiKeys[1].apiKey shouldBe "6ba7b810-****-****-****-********30c8"

                        // 마스킹된 키에 별표가 포함되어 있는지 확인
                        result.apiKeys[0].apiKey shouldContain "****"
                        result.apiKeys[1].apiKey shouldContain "****"

                        verify(exactly = 1) {
                            mockApiKeyRepository.searchApiKeyWithPaging(
                                id = null,
                                accountId = null,
                                scope = null,
                                isExpired = null,
                                isRenewable = null,
                                pageable = pageable,
                            )
                        }
                    }
                }

                context("특정 계정 ID로 필터링할 때") {
                    val apiKeys = listOf(apiKey1)
                    val pageable = PageRequest.of(0, 100)
                    val page = PageImpl(apiKeys, pageable, apiKeys.size.toLong())

                    beforeEach {
                        every {
                            mockApiKeyRepository.searchApiKeyWithPaging(
                                id = null,
                                accountId = 1L,
                                scope = null,
                                isExpired = null,
                                isRenewable = null,
                                pageable = pageable,
                            )
                        } returns page
                    }

                    it("해당 계정의 API 키만 마스킹하여 반환해야 한다") {
                        val result =
                            searchApiKeyService.execute(
                                id = null,
                                accountId = 1L,
                                scope = null,
                                isExpired = null,
                                isRenewable = null,
                                page = 0,
                                size = 100,
                            )

                        result.totalPages shouldBe 1
                        result.totalElements shouldBe 1
                        result.apiKeys.size shouldBe 1
                        result.apiKeys[0].apiKey shouldBe "550e8400-****-****-****-********0000"

                        verify(exactly = 1) {
                            mockApiKeyRepository.searchApiKeyWithPaging(
                                id = null,
                                accountId = 1L,
                                scope = null,
                                isExpired = null,
                                isRenewable = null,
                                pageable = pageable,
                            )
                        }
                    }
                }

                context("특정 scope로 필터링할 때") {
                    val apiKeys = listOf(apiKey1)
                    val pageable = PageRequest.of(0, 100)
                    val page = PageImpl(apiKeys, pageable, apiKeys.size.toLong())

                    beforeEach {
                        every {
                            mockApiKeyRepository.searchApiKeyWithPaging(
                                id = null,
                                accountId = null,
                                scope = "club:write",
                                isExpired = null,
                                isRenewable = null,
                                pageable = pageable,
                            )
                        } returns page
                    }

                    it("해당 scope를 가진 API 키만 마스킹하여 반환해야 한다") {
                        val result =
                            searchApiKeyService.execute(
                                id = null,
                                accountId = null,
                                scope = "club:write",
                                isExpired = null,
                                isRenewable = null,
                                page = 0,
                                size = 100,
                            )

                        result.totalPages shouldBe 1
                        result.totalElements shouldBe 1
                        result.apiKeys.size shouldBe 1
                        result.apiKeys[0].scopes shouldBe setOf("student:read", "club:write")
                        result.apiKeys[0].apiKey shouldBe "550e8400-****-****-****-********0000"

                        verify(exactly = 1) {
                            mockApiKeyRepository.searchApiKeyWithPaging(
                                id = null,
                                accountId = null,
                                scope = "club:write",
                                isExpired = null,
                                isRenewable = null,
                                pageable = pageable,
                            )
                        }
                    }
                }

                context("만료된 API 키만 검색할 때") {
                    val apiKeys = listOf(apiKey2)
                    val pageable = PageRequest.of(0, 100)
                    val page = PageImpl(apiKeys, pageable, apiKeys.size.toLong())

                    beforeEach {
                        every {
                            mockApiKeyRepository.searchApiKeyWithPaging(
                                id = null,
                                accountId = null,
                                scope = null,
                                isExpired = true,
                                isRenewable = null,
                                pageable = pageable,
                            )
                        } returns page
                    }

                    it("만료된 API 키만 마스킹하여 반환해야 한다") {
                        val result =
                            searchApiKeyService.execute(
                                id = null,
                                accountId = null,
                                scope = null,
                                isExpired = true,
                                isRenewable = null,
                                page = 0,
                                size = 100,
                            )

                        result.totalPages shouldBe 1
                        result.totalElements shouldBe 1
                        result.apiKeys.size shouldBe 1
                        result.apiKeys[0].apiKey shouldBe "6ba7b810-****-****-****-********30c8"

                        verify(exactly = 1) {
                            mockApiKeyRepository.searchApiKeyWithPaging(
                                id = null,
                                accountId = null,
                                scope = null,
                                isExpired = true,
                                isRenewable = null,
                                pageable = pageable,
                            )
                        }
                    }
                }

                context("조건에 맞는 API 키가 없을 때") {
                    val apiKeys = emptyList<ApiKey>()
                    val pageable = PageRequest.of(0, 100)
                    val page = PageImpl(apiKeys, pageable, 0L)

                    beforeEach {
                        every {
                            mockApiKeyRepository.searchApiKeyWithPaging(
                                id = 999L,
                                accountId = null,
                                scope = null,
                                isExpired = null,
                                isRenewable = null,
                                pageable = pageable,
                            )
                        } returns page
                    }

                    it("빈 목록을 반환해야 한다") {
                        val result =
                            searchApiKeyService.execute(
                                id = 999L,
                                accountId = null,
                                scope = null,
                                isExpired = null,
                                isRenewable = null,
                                page = 0,
                                size = 100,
                            )

                        result.totalPages shouldBe 0
                        result.totalElements shouldBe 0
                        result.apiKeys.size shouldBe 0

                        verify(exactly = 1) {
                            mockApiKeyRepository.searchApiKeyWithPaging(
                                id = 999L,
                                accountId = null,
                                scope = null,
                                isExpired = null,
                                isRenewable = null,
                                pageable = pageable,
                            )
                        }
                    }
                }
            }
        }
    })
