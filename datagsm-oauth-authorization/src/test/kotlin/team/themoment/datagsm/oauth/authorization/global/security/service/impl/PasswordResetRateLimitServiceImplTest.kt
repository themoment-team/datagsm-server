package team.themoment.datagsm.oauth.authorization.global.security.service.impl

import io.github.bucket4j.BucketConfiguration
import io.github.bucket4j.ConsumptionProbe
import io.github.bucket4j.distributed.BucketProxy
import io.github.bucket4j.distributed.proxy.ProxyManager
import io.github.bucket4j.distributed.proxy.RemoteBucketBuilder
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import team.themoment.datagsm.common.global.data.PasswordResetRateLimitEnvironment
import team.themoment.datagsm.oauth.authorization.global.security.annotation.PasswordResetRateLimitType
import java.util.function.Supplier

class PasswordResetRateLimitServiceImplTest :
    DescribeSpec({

        val defaultConfig =
            PasswordResetRateLimitEnvironment.RateLimitConfig(
                capacity = 5L,
                refillTokens = 1L,
                refillDurationMinutes = 1L,
            )

        lateinit var mockProxyManager: ProxyManager<String>

        beforeEach {
            mockProxyManager = mockk(relaxed = true)
        }

        describe("PasswordResetRateLimitServiceImpl 클래스의") {
            describe("tryConsume 메서드는") {

                context("rate limit이 비활성화되어 있을 때") {
                    val environment =
                        PasswordResetRateLimitEnvironment(
                            enabled = false,
                            send = defaultConfig,
                            verify = defaultConfig,
                            change = defaultConfig,
                            signupSend = defaultConfig,
                            signupCheck = defaultConfig,
                            login = defaultConfig,
                        )

                    PasswordResetRateLimitType.entries.forEach { type ->
                        it("${type.name} 타입에서 항상 소비 성공 및 capacity만큼 남은 토큰을 반환해야 한다") {
                            val service = PasswordResetRateLimitServiceImpl(mockProxyManager, environment)
                            val result = service.tryConsume("test@gsm.hs.kr", type)

                            result.consumed shouldBe true
                            result.remainingTokens shouldBe defaultConfig.capacity
                            result.secondsToWaitForRefill shouldBe 0L
                        }
                    }
                }

                context("rate limit이 활성화되어 있을 때") {
                    val environment =
                        PasswordResetRateLimitEnvironment(
                            enabled = true,
                            send = defaultConfig,
                            verify = defaultConfig,
                            change = defaultConfig,
                            signupSend = defaultConfig,
                            signupCheck = defaultConfig,
                            login = defaultConfig,
                        )

                    lateinit var mockBucketBuilder: RemoteBucketBuilder<String>
                    lateinit var mockBucket: BucketProxy

                    beforeEach {
                        mockBucketBuilder = mockk()
                        mockBucket = mockk()
                        every { mockProxyManager.builder() } returns mockBucketBuilder
                        every { mockBucketBuilder.build(any<String>(), any<Supplier<BucketConfiguration>>()) } returns mockBucket
                    }

                    it("토큰 소비에 성공하면 consumed=true와 남은 토큰을 반환해야 한다") {
                        every { mockBucket.tryConsumeAndReturnRemaining(1) } returns ConsumptionProbe.consumed(4L, 0L)

                        val service = PasswordResetRateLimitServiceImpl(mockProxyManager, environment)
                        val result = service.tryConsume("test@gsm.hs.kr", PasswordResetRateLimitType.SEND_EMAIL)

                        result.consumed shouldBe true
                        result.remainingTokens shouldBe 4L
                        result.secondsToWaitForRefill shouldBe 0L
                    }

                    it("토큰이 소진되면 consumed=false와 대기 시간을 반환해야 한다") {
                        every { mockBucket.tryConsumeAndReturnRemaining(1) } returns
                            ConsumptionProbe.rejected(0L, 60_000_000_000L, 60_000_000_000L)

                        val service = PasswordResetRateLimitServiceImpl(mockProxyManager, environment)
                        val result = service.tryConsume("test@gsm.hs.kr", PasswordResetRateLimitType.SEND_EMAIL)

                        result.consumed shouldBe false
                        result.remainingTokens shouldBe 0L
                        result.secondsToWaitForRefill shouldBe 60L
                    }
                }

                context("각 타입별 config 매핑이 올바른지 검증할 때") {
                    val sendConfig =
                        PasswordResetRateLimitEnvironment.RateLimitConfig(
                            capacity = 3L,
                            refillTokens = 1L,
                            refillDurationMinutes = 5L,
                        )
                    val verifyConfig =
                        PasswordResetRateLimitEnvironment.RateLimitConfig(
                            capacity = 10L,
                            refillTokens = 5L,
                            refillDurationMinutes = 1L,
                        )
                    val changeConfig =
                        PasswordResetRateLimitEnvironment.RateLimitConfig(
                            capacity = 2L,
                            refillTokens = 1L,
                            refillDurationMinutes = 10L,
                        )
                    val signupSendConfig =
                        PasswordResetRateLimitEnvironment.RateLimitConfig(
                            capacity = 5L,
                            refillTokens = 2L,
                            refillDurationMinutes = 3L,
                        )
                    val signupCheckConfig =
                        PasswordResetRateLimitEnvironment.RateLimitConfig(
                            capacity = 8L,
                            refillTokens = 4L,
                            refillDurationMinutes = 2L,
                        )
                    val loginConfig =
                        PasswordResetRateLimitEnvironment.RateLimitConfig(
                            capacity = 12L,
                            refillTokens = 6L,
                            refillDurationMinutes = 10L,
                        )
                    val environment =
                        PasswordResetRateLimitEnvironment(
                            enabled = false,
                            send = sendConfig,
                            verify = verifyConfig,
                            change = changeConfig,
                            signupSend = signupSendConfig,
                            signupCheck = signupCheckConfig,
                            login = loginConfig,
                        )
                    val service = PasswordResetRateLimitServiceImpl(mockProxyManager, environment)

                    it("SEND_EMAIL 타입은 send config의 capacity를 사용해야 한다") {
                        val result = service.tryConsume("test@gsm.hs.kr", PasswordResetRateLimitType.SEND_EMAIL)
                        result.remainingTokens shouldBe sendConfig.capacity
                    }

                    it("CHECK_CODE 타입은 verify config의 capacity를 사용해야 한다") {
                        val result = service.tryConsume("test@gsm.hs.kr", PasswordResetRateLimitType.CHECK_CODE)
                        result.remainingTokens shouldBe verifyConfig.capacity
                    }

                    it("MODIFY_PASSWORD 타입은 change config의 capacity를 사용해야 한다") {
                        val result = service.tryConsume("test@gsm.hs.kr", PasswordResetRateLimitType.MODIFY_PASSWORD)
                        result.remainingTokens shouldBe changeConfig.capacity
                    }

                    it("SIGNUP_SEND_EMAIL 타입은 signupSend config의 capacity를 사용해야 한다") {
                        val result = service.tryConsume("test@gsm.hs.kr", PasswordResetRateLimitType.SIGNUP_SEND_EMAIL)
                        result.remainingTokens shouldBe signupSendConfig.capacity
                    }

                    it("SIGNUP_CHECK_CODE 타입은 signupCheck config의 capacity를 사용해야 한다") {
                        val result = service.tryConsume("test@gsm.hs.kr", PasswordResetRateLimitType.SIGNUP_CHECK_CODE)
                        result.remainingTokens shouldBe signupCheckConfig.capacity
                    }

                    it("LOGIN 타입은 login config의 capacity를 사용해야 한다") {
                        val result = service.tryConsume("test@gsm.hs.kr", PasswordResetRateLimitType.LOGIN)
                        result.remainingTokens shouldBe loginConfig.capacity
                    }
                }

                context("대소문자와 앞뒤 공백만 다른 이메일로 요청할 때") {
                    val environment =
                        PasswordResetRateLimitEnvironment(
                            enabled = true,
                            send = defaultConfig,
                            verify = defaultConfig,
                            change = defaultConfig,
                            signupSend = defaultConfig,
                            signupCheck = defaultConfig,
                            login = defaultConfig,
                        )

                    it("정규화된 같은 버킷 키를 사용해야 한다") {
                        val mockBucketBuilder = mockk<RemoteBucketBuilder<String>>()
                        val mockBucket = mockk<BucketProxy>()
                        val bucketKeys = mutableListOf<String>()
                        every { mockProxyManager.builder() } returns mockBucketBuilder
                        every { mockBucketBuilder.build(capture(bucketKeys), any<Supplier<BucketConfiguration>>()) } returns mockBucket
                        every { mockBucket.tryConsumeAndReturnRemaining(1) } returns ConsumptionProbe.consumed(4L, 0L)
                        val service = PasswordResetRateLimitServiceImpl(mockProxyManager, environment)

                        service.tryConsume("Test@GSM.hs.kr", PasswordResetRateLimitType.LOGIN)
                        service.tryConsume(" test@gsm.hs.kr ", PasswordResetRateLimitType.LOGIN)

                        bucketKeys shouldBe listOf("rate_limit:login:test@gsm.hs.kr", "rate_limit:login:test@gsm.hs.kr")
                    }
                }
            }

            describe("refund 메서드는") {
                fun environment(enabled: Boolean) =
                    PasswordResetRateLimitEnvironment(
                        enabled = enabled,
                        send = defaultConfig,
                        verify = defaultConfig,
                        change = defaultConfig,
                        signupSend = defaultConfig,
                        signupCheck = defaultConfig,
                        login = defaultConfig,
                    )

                context("rate limit이 활성화되어 있을 때") {
                    it("정규화된 이메일의 버킷에 토큰 1개를 되돌려야 한다") {
                        val mockBucketBuilder = mockk<RemoteBucketBuilder<String>>()
                        val mockBucket = mockk<BucketProxy>(relaxed = true)
                        every { mockProxyManager.builder() } returns mockBucketBuilder
                        every { mockBucketBuilder.build("rate_limit:login:test@gsm.hs.kr", any<Supplier<BucketConfiguration>>()) } returns
                            mockBucket
                        val service = PasswordResetRateLimitServiceImpl(mockProxyManager, environment(enabled = true))

                        service.refund("Test@gsm.hs.kr", PasswordResetRateLimitType.LOGIN)

                        verify(exactly = 1) { mockBucket.addTokens(1) }
                    }
                }

                context("rate limit이 비활성화되어 있을 때") {
                    it("버킷에 접근하지 않아야 한다") {
                        val service = PasswordResetRateLimitServiceImpl(mockProxyManager, environment(enabled = false))

                        service.refund("test@gsm.hs.kr", PasswordResetRateLimitType.LOGIN)

                        verify(exactly = 0) { mockProxyManager.builder() }
                    }
                }
            }
        }
    })
