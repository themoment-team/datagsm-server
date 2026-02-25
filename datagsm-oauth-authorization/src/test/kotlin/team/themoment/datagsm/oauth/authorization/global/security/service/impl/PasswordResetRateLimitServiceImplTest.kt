package team.themoment.datagsm.oauth.authorization.global.security.service.impl

import io.github.bucket4j.distributed.proxy.ProxyManager
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import team.themoment.datagsm.common.global.data.PasswordResetRateLimitEnvironment
import team.themoment.datagsm.oauth.authorization.global.security.annotation.PasswordResetRateLimitType

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
                        )

                    it("bucket4j를 통해 토큰 소비를 시도하고 결과를 반환해야 한다") {
                        val service = PasswordResetRateLimitServiceImpl(mockProxyManager, environment)
                        val result = service.tryConsume("test@gsm.hs.kr", PasswordResetRateLimitType.SEND_EMAIL)

                        // relaxed mock으로 인해 probe.isConsumed=false, remainingTokens=0, nanosToWaitForRefill=0
                        result.consumed shouldBe false
                        result.remainingTokens shouldBe 0L
                        result.secondsToWaitForRefill shouldBe 0L
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
                    val environment =
                        PasswordResetRateLimitEnvironment(
                            enabled = false,
                            send = sendConfig,
                            verify = verifyConfig,
                            change = changeConfig,
                            signupSend = signupSendConfig,
                            signupCheck = signupCheckConfig,
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
                }
            }
        }
    })
