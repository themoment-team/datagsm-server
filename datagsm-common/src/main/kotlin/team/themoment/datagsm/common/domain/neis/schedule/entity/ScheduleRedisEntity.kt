package team.themoment.datagsm.common.domain.neis.schedule.entity

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import java.time.LocalDate

@RedisHash(value = "schedule", timeToLive = 2592000)
data class ScheduleRedisEntity(
    @Id
    val id: String,
    val schoolCode: String,
    val schoolName: String,
    val officeCode: String,
    val officeName: String,
    val date: LocalDate,
    val academicYear: String,
    val eventName: String,
    val eventContent: String?,
    val dayCategory: String?,
    val schoolCourseType: String?,
    val dayNightType: String?,
    val targetGrades: List<Int>,
)
