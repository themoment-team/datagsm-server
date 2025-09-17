package team.themoment.datagsm.domain.neis.schedule.entity

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.TimeToLive
import java.time.LocalDate
import java.util.concurrent.TimeUnit

@RedisHash(value = "schedule", timeToLive = 2592000) // 30일
data class ScheduleRedisEntity(
    @Id
    val scheduleId: String,
    val scheduleGrade: Int,
    val scheduleClassNumber: Int,
    val scheduleDate: LocalDate,
    val scheduleContent: String,
    @TimeToLive(unit = TimeUnit.DAYS)
    val scheduleTtl: Long = 7,
)
