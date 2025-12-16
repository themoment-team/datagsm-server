package team.themoment.datagsm.domain.neis.meal.entity

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import team.themoment.datagsm.domain.neis.meal.entity.constant.MealType
import java.time.LocalDate

@RedisHash(value = "meal", timeToLive = 2592000)
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
    val allergyInfo: List<String>?,
    val calories: String?,
    val originInfo: String?,
    val nutritionInfo: String?,
    val serveCount: Int?,
)
