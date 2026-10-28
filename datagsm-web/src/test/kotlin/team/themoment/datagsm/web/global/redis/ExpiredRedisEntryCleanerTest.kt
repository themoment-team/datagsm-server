package team.themoment.datagsm.web.global.redis

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import org.springframework.data.redis.core.RedisKeyValueAdapter
import org.springframework.data.redis.core.RedisTemplate

class ExpiredRedisEntryCleanerTest :
    DescribeSpec({
        describe("ExpiredRedisEntryCleaner 클래스의") {
            describe("findExpiringKeyspaces 메서드는") {
                it("TTL이 있는 모든 Redis 엔티티의 키스페이스를 찾아야 한다") {
                    // Given
                    val redisTemplate = mockk<RedisTemplate<String, String>>(relaxed = true)
                    val cleaner = ExpiredRedisEntryCleaner(RedisKeyValueAdapter(redisTemplate), redisTemplate)

                    // When
                    val keyspaces = cleaner.findExpiringKeyspaces()

                    // Then
                    keyspaces shouldBe
                        listOf(
                            "emailCode",
                            "idp_session",
                            "idp_session_handoff",
                            "meal",
                            "oauthCode",
                            "oauthRefreshToken",
                            "oauth_authorize_state",
                            "passwordResetCode",
                            "schedule",
                            "timetable",
                        )
                }
            }
        }
    })
