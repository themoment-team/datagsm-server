package team.themoment.datagsm.web.global.redis

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.data.redis.RedisSystemException
import org.springframework.data.redis.core.Cursor
import org.springframework.data.redis.core.RedisKeyValueAdapter
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.SetOperations
import org.springframework.data.redis.core.script.RedisScript

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

            describe("cleanUp 메서드는") {
                context("한 키스페이스의 정리가 중간 배치에서 실패하면") {
                    it("이미 제거한 개수를 보고하고 나머지 키스페이스는 계속 처리해야 한다") {
                        // Given
                        val redisTemplate = mockk<RedisTemplate<String, String>>(relaxed = true)
                        val setOperations = mockk<SetOperations<String, String>>(relaxed = true)
                        every { redisTemplate.opsForSet() } returns setOperations
                        // 배치 크기(200)를 넘겨 두 번째 배치에서 실패하게 한다.
                        val ids = (1..201).map { "id$it" }.iterator()
                        val cursor = mockk<Cursor<String>>(relaxed = true)
                        every { cursor.hasNext() } answers { ids.hasNext() }
                        every { cursor.next() } answers { ids.next() }
                        every { setOperations.scan("idp_session", any()) } returns cursor
                        every {
                            redisTemplate.execute(any<RedisScript<Long>>(), any<List<String>>(), *anyVararg<Any>())
                        } returns 5L andThenThrows RedisSystemException("Redis unavailable", RuntimeException())
                        val cleaner = ExpiredRedisEntryCleaner(RedisKeyValueAdapter(redisTemplate), redisTemplate)

                        // When
                        val result = cleaner.cleanUp()

                        // Then
                        result["idp_session"] shouldBe 5L
                        result.keys.size shouldBe 10
                    }
                }
            }
        }
    })
