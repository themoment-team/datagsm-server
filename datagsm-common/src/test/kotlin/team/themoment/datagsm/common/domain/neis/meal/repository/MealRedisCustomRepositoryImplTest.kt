package team.themoment.datagsm.common.domain.neis.meal.repository

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.data.redis.core.RedisKeyValueTemplate
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.SetOperations
import team.themoment.datagsm.common.domain.neis.meal.entity.MealRedisEntity
import team.themoment.datagsm.common.domain.neis.meal.entity.constant.MealType
import java.time.LocalDate
import java.util.Optional

class MealRedisCustomRepositoryImplTest :
    DescribeSpec({

        fun meal(
            id: String,
            date: LocalDate,
        ) = MealRedisEntity(
            id = id,
            schoolCode = "7380292",
            schoolName = "광주소프트웨어마이스터고등학교",
            officeCode = "F10",
            officeName = "광주광역시교육청",
            date = date,
            type = MealType.LUNCH,
            menu = listOf("쌀밥", "김치찌개"),
            allergyInfo = listOf("5", "9"),
            calories = "800 Kcal",
            originInfo = null,
            nutritionInfo = null,
            serveCount = 400,
        )

        describe("MealRedisCustomRepositoryImpl 클래스의") {

            val setOperations = mockk<SetOperations<String, String>>()
            val redisTemplate = mockk<RedisTemplate<String, String>> { every { opsForSet() } returns setOperations }
            val redisKeyValueTemplate = mockk<RedisKeyValueTemplate>()
            val repository = MealRedisCustomRepositoryImpl(redisTemplate, redisKeyValueTemplate)

            describe("findByDateBetween 메서드는") {

                context("기간 안의 날짜 인덱스에 급식 id가 있을 때") {
                    it("저장할 때와 같은 변환기로 읽어 목록 필드를 그대로 반환해야 한다") {
                        val day1 = LocalDate.of(2026, 10, 27)
                        val day2 = day1.plusDays(1)
                        every { setOperations.members("meal:date:$day1") } returns setOf("m1")
                        every { setOperations.members("meal:date:$day2") } returns setOf("m2", "expired")
                        every { redisKeyValueTemplate.findById("m1", MealRedisEntity::class.java) } returns Optional.of(meal("m1", day1))
                        every { redisKeyValueTemplate.findById("m2", MealRedisEntity::class.java) } returns Optional.of(meal("m2", day2))
                        every { redisKeyValueTemplate.findById("expired", MealRedisEntity::class.java) } returns Optional.empty()

                        val result = repository.findByDateBetween(day1, day2)

                        result.map { it.id } shouldContainExactlyInAnyOrder listOf("m1", "m2")
                        result.first { it.id == "m1" }.menu shouldBe listOf("쌀밥", "김치찌개")
                        result.first { it.id == "m1" }.allergyInfo shouldBe listOf("5", "9")
                    }
                }

                context("기간 안에 급식이 없을 때") {
                    it("빈 목록을 반환해야 한다") {
                        val day = LocalDate.of(2026, 1, 1)
                        every { setOperations.members("meal:date:$day") } returns emptySet()

                        repository.findByDateBetween(day, day) shouldBe emptyList()
                    }
                }
            }
        }
    })
