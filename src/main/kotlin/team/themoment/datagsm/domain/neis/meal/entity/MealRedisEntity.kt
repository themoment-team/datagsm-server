package team.themoment.datagsm.domain.neis.meal.entity

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.TimeToLive
import team.themoment.datagsm.domain.neis.meal.entity.constant.MealType
import java.time.LocalDate
import java.util.concurrent.TimeUnit

@RedisHash(value = "Meal")
data class MealRedisEntity(
    @Id
    val mealId: String,

    val mealType: MealType,

    val mealAllergyInfo: String,

    val mealDate: LocalDate,

    val mealMenu: String,

    val mealCalories: Double,

    @TimeToLive(unit = TimeUnit.DAYS)
    val mealTtl: Long = 30
)
