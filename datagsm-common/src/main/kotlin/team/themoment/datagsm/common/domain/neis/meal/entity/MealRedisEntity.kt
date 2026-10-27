package team.themoment.datagsm.common.domain.neis.meal.entity

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.index.Indexed
import team.themoment.datagsm.common.domain.neis.meal.entity.constant.MealType
import java.time.LocalDate

@RedisHash(value = "meal", timeToLive = 2592000)
data class MealRedisEntity(
    @Id
    val id: String,
    val schoolCode: String,
    val schoolName: String,
    val officeCode: String,
    val officeName: String,
    @Indexed
    val date: LocalDate,
    val type: MealType,
    // Spring Data Redis는 빈 목록을 저장하지 않아 읽을 때 값이 없으므로 기본값으로 복원한다.
    val menu: List<String> = emptyList(),
    val allergyInfo: List<String>?,
    val calories: String?,
    val originInfo: String?,
    val nutritionInfo: String?,
    val serveCount: Int?,
)
