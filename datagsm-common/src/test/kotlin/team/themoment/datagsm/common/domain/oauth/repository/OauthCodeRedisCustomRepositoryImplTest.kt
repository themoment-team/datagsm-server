package team.themoment.datagsm.common.domain.oauth.repository

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations
import java.time.Duration

class OauthCodeRedisCustomRepositoryImplTest :
    DescribeSpec({

        val mockRedisTemplate = mockk<RedisTemplate<String, String>>()
        val mockValueOperations = mockk<ValueOperations<String, String>>()
        val repository = OauthCodeRedisCustomRepositoryImpl(mockRedisTemplate)

        beforeEach {
            every { mockRedisTemplate.opsForValue() } returns mockValueOperations
        }

        afterEach {
            clearAllMocks()
        }

        describe("OauthCodeRedisCustomRepositoryImpl 클래스의") {
            describe("claimUsage 메서드는") {
                context("아직 선점되지 않은 코드일 때") {
                    it("사용 표시 키를 NX로 저장하고 true를 반환해야 한다") {
                        every { mockValueOperations.setIfAbsent("oauth_code_used:code-1", "client-1", Duration.ofSeconds(300)) } returns
                            true

                        repository.claimUsage("code-1", "client-1", Duration.ofSeconds(300)) shouldBe true
                    }
                }

                context("이미 선점된 코드일 때") {
                    it("false를 반환해야 한다") {
                        every { mockValueOperations.setIfAbsent(any(), any(), any<Duration>()) } returns false

                        repository.claimUsage("code-1", "client-1", Duration.ofSeconds(300)) shouldBe false
                    }
                }

                // 트랜잭션·파이프라인 안에서는 결과가 null로 오므로, 선점 성공으로 오인하면 안 된다.
                context("Redis가 결과를 돌려주지 않을 때") {
                    it("false를 반환해야 한다") {
                        every { mockValueOperations.setIfAbsent(any(), any(), any<Duration>()) } returns null

                        repository.claimUsage("code-1", "client-1", Duration.ofSeconds(300)) shouldBe false
                    }
                }
            }

            describe("releaseUsage 메서드는") {
                it("사용 표시 키를 삭제해야 한다") {
                    every { mockRedisTemplate.delete("oauth_code_used:code-1") } returns true

                    repository.releaseUsage("code-1")

                    verify(exactly = 1) { mockRedisTemplate.delete("oauth_code_used:code-1") }
                }
            }
        }
    })
