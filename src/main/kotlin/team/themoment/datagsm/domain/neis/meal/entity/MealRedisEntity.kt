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
    val id: String,
    val schoolCode: String,
    val schoolName: String,
    val officeCode: String,
    val officeName: String,
    val date: LocalDate,
    val type: MealType,
    val menu: List<String>,
    val allergyInfo: List<String>,
    val calories: String?,
    val originInfo: String?,
    val nutritionInfo: String?,
    val serveCount: Int?,
)
