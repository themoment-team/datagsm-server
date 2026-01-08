package team.themoment.datagsm.resource.global.security.service

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import team.themoment.datagsm.common.domain.auth.entity.ApiKey
import team.themoment.datagsm.resource.global.security.data.ApiKeyEnvironment
import team.themoment.datagsm.resource.global.security.service.impl.RateLimitServiceImpl
import java.util.UUID

class RateLimitServiceTest :
    DescribeSpec({

        describe("RateLimitService 클래스의") {

            lateinit var mockProxyManager: io.github.bucket4j.distributed.proxy.ProxyManager<String>

            beforeEach {
                mockProxyManager = mockk<io.github.bucket4j.distributed.proxy.ProxyManager<String>>(relaxed = true)
            }

            val testApiKey =
                ApiKey().apply {
                    id = 1L
                    value = UUID.randomUUID()
                    rateLimitCapacity = 100
                    rateLimitRefillTokens = 100
                    rateLimitRefillDurationSeconds = 60
                }

            describe("tryConsume 메서드는") {

                context("Rate limit이 비활성화되어 있을 때") {
                    it("항상 true를 반환해야 한다") {
                        val apiKeyEnvironment =
                            ApiKeyEnvironment(
                                expirationDays = 30,
                                renewalPeriodDays = 7,
                                adminExpirationDays = 365,
                                rateLimit =
                                    ApiKeyEnvironment.RateLimit(
                                        enabled = false,
                                        defaultCapacity = 100,
                                        defaultRefillTokens = 100,
                                        defaultRefillDurationSeconds = 60,
                                    ),
                            )
                        val rateLimitService = RateLimitServiceImpl(mockProxyManager, apiKeyEnvironment)
                        val result = rateLimitService.tryConsume(testApiKey)

                        result shouldBe true
                    }
                }

                context("Rate limit이 활성화되어 있을 때") {
                    it("ProxyManager를 통해 bucket을 생성하고 tryConsume을 호출해야 한다") {
                        val apiKeyEnvironment =
                            ApiKeyEnvironment(
                                expirationDays = 30,
                                renewalPeriodDays = 7,
                                adminExpirationDays = 365,
                                rateLimit =
                                    ApiKeyEnvironment.RateLimit(
                                        enabled = true,
                                        defaultCapacity = 100,
                                        defaultRefillTokens = 100,
                                        defaultRefillDurationSeconds = 60,
                                    ),
                            )
                        val rateLimitService = RateLimitServiceImpl(mockProxyManager, apiKeyEnvironment)
                        val result = rateLimitService.tryConsume(testApiKey)

                        result shouldBe false
                    }
                }
            }

            describe("getRemainingTokens 메서드는") {

                context("Rate limit이 활성화되어 있을 때") {
                    it("bucket을 통해 남은 토큰을 조회해야 한다") {
                        val apiKeyEnvironment =
                            ApiKeyEnvironment(
                                expirationDays = 30,
                                renewalPeriodDays = 7,
                                adminExpirationDays = 365,
                                rateLimit =
                                    ApiKeyEnvironment.RateLimit(
                                        enabled = true,
                                        defaultCapacity = 100,
                                        defaultRefillTokens = 100,
                                        defaultRefillDurationSeconds = 60,
                                    ),
                            )
                        val rateLimitService = RateLimitServiceImpl(mockProxyManager, apiKeyEnvironment)
                        val result = rateLimitService.getRemainingTokens(testApiKey)

                        result shouldBe 0L
                    }
                }
            }

            describe("getSecondsUntilRefill 메서드는") {

                context("Rate limit이 활성화되어 있을 때") {
                    it("bucket을 통해 리필까지 남은 시간을 조회해야 한다") {
                        val apiKeyEnvironment =
                            ApiKeyEnvironment(
                                expirationDays = 30,
                                renewalPeriodDays = 7,
                                adminExpirationDays = 365,
                                rateLimit =
                                    ApiKeyEnvironment.RateLimit(
                                        enabled = true,
                                        defaultCapacity = 100,
                                        defaultRefillTokens = 100,
                                        defaultRefillDurationSeconds = 60,
                                    ),
                            )
                        val rateLimitService = RateLimitServiceImpl(mockProxyManager, apiKeyEnvironment)
                        val result = rateLimitService.getSecondsUntilRefill(testApiKey)

                        result shouldBe 0L
                    }
                }
            }

            describe("tryConsumeAndReturnRemaining 메서드는") {

                context("Rate limit이 비활성화되어 있을 때") {
                    it("consumed=true, remainingTokens=capacity를 반환해야 한다") {
                        val apiKeyEnvironment =
                            ApiKeyEnvironment(
                                expirationDays = 30,
                                renewalPeriodDays = 7,
                                adminExpirationDays = 365,
                                rateLimit =
                                    ApiKeyEnvironment.RateLimit(
                                        enabled = false,
                                        defaultCapacity = 100,
                                        defaultRefillTokens = 100,
                                        defaultRefillDurationSeconds = 60,
                                    ),
                            )
                        val rateLimitService = RateLimitServiceImpl(mockProxyManager, apiKeyEnvironment)
                        val result = rateLimitService.tryConsumeAndReturnRemaining(testApiKey)

                        result.consumed shouldBe true
                        result.remainingTokens shouldBe 100L
                        result.secondsToWaitForRefill shouldBe 0L
                    }
                }

                context("Rate limit이 활성화되어 있을 때") {
                    it("bucket을 통해 토큰 소비를 시도하고 결과를 반환해야 한다") {
                        val apiKeyEnvironment =
                            ApiKeyEnvironment(
                                expirationDays = 30,
                                renewalPeriodDays = 7,
                                adminExpirationDays = 365,
                                rateLimit =
                                    ApiKeyEnvironment.RateLimit(
                                        enabled = true,
                                        defaultCapacity = 100,
                                        defaultRefillTokens = 100,
                                        defaultRefillDurationSeconds = 60,
                                    ),
                            )
                        val rateLimitService = RateLimitServiceImpl(mockProxyManager, apiKeyEnvironment)
                        val result = rateLimitService.tryConsumeAndReturnRemaining(testApiKey)

                        result.consumed shouldBe false
                        result.remainingTokens shouldBe 0L
                        result.secondsToWaitForRefill shouldBe 0L
                    }
                }
            }
        }
    })
