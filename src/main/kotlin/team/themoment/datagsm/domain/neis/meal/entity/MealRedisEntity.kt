package team.themoment.datagsm.domain.neis.meal.entity

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.TimeToLive
import java.time.LocalDate
import java.util.concurrent.TimeUnit

@RedisHash(value = "Meal")
data class MealRedisEntity(
    @Id
    val mealId: LocalDate,

    val breakfast: String,

    val breakfastAlergy: String? = null,

    val breakfastCalorie: Double? = null,

    val lunch: String,

    val lunchAlergy: String? = null,

    val lunchCalorie: Double? = null,

    val dinner: String? = null,

    val dinnerAlergy: String? = null,

    val dinnerCalorie: Double? = null,

    @TimeToLive(unit = TimeUnit.DAYS)
    val ttl: Long = 30
)
