package team.themoment.datagsm.common.domain.neis

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.springframework.data.redis.core.convert.MappingRedisConverter
import org.springframework.data.redis.core.convert.RedisData
import org.springframework.data.redis.core.mapping.RedisMappingContext
import team.themoment.datagsm.common.domain.neis.meal.entity.MealRedisEntity
import team.themoment.datagsm.common.domain.neis.meal.entity.constant.MealType
import team.themoment.datagsm.common.domain.neis.schedule.entity.ScheduleRedisEntity
import java.time.LocalDate

class NeisRedisEntityMappingTest :
    DescribeSpec({

        val converter = MappingRedisConverter(RedisMappingContext()).apply { afterPropertiesSet() }

        fun <T : Any> roundTrip(
            entity: T,
            type: Class<T>,
        ): T {
            val data = RedisData()
            converter.write(entity, data)
            return converter.read(type, data).shouldNotBeNull()
        }

        fun schedule(targetGrades: List<Int>) =
            ScheduleRedisEntity(
                id = "7380292_20261028",
                schoolCode = "7380292",
                schoolName = "광주소프트웨어마이스터고등학교",
                officeCode = "F10",
                officeName = "광주광역시교육청",
                date = LocalDate.of(2026, 10, 28),
                academicYear = "2026",
                eventName = "재량휴업일",
                eventContent = null,
                dayCategory = "휴업일",
                schoolCourseType = "고등학교",
                dayNightType = "주간",
                targetGrades = targetGrades,
            )

        fun meal(menu: List<String>) =
            MealRedisEntity(
                id = "7380292_20261028_2",
                schoolCode = "7380292",
                schoolName = "광주소프트웨어마이스터고등학교",
                officeCode = "F10",
                officeName = "광주광역시교육청",
                date = LocalDate.of(2026, 10, 28),
                type = MealType.LUNCH,
                menu = menu,
                allergyInfo = null,
                calories = null,
                originInfo = null,
                nutritionInfo = null,
                serveCount = null,
            )

        describe("ScheduleRedisEntity 클래스의") {

            context("targetGrades에 값이 있을 때") {
                it("Redis 형식으로 저장한 뒤 읽어도 목록이 그대로 복원되어야 한다") {
                    roundTrip(schedule(listOf(1, 2)), ScheduleRedisEntity::class.java).targetGrades shouldBe listOf(1, 2)
                }
            }

            context("targetGrades가 비어 있을 때") {
                it("저장되지 않은 목록을 빈 목록으로 복원해야 한다") {
                    roundTrip(schedule(emptyList()), ScheduleRedisEntity::class.java).targetGrades shouldBe emptyList()
                }
            }
        }

        describe("MealRedisEntity 클래스의") {

            context("menu에 값이 있을 때") {
                it("Redis 형식으로 저장한 뒤 읽어도 목록이 그대로 복원되어야 한다") {
                    roundTrip(meal(listOf("쌀밥", "김치")), MealRedisEntity::class.java).menu shouldBe listOf("쌀밥", "김치")
                }
            }

            context("menu가 비어 있을 때") {
                it("저장되지 않은 목록을 빈 목록으로 복원해야 한다") {
                    roundTrip(meal(emptyList()), MealRedisEntity::class.java).menu shouldBe emptyList()
                }
            }
        }
    })
