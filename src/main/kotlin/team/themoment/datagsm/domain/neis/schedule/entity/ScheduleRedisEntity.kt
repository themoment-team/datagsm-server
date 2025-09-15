package team.themoment.datagsm.domain.neis.schedule.entity

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.TimeToLive
import java.time.LocalDate
import java.util.concurrent.TimeUnit

@RedisHash(value = "schedule", timeToLive = 2592000) // 30일
data class ScheduleRedisEntity(
    @Id
    val scheduleId: LocalDate,

    @TimeToLive(unit = TimeUnit.DAYS)
    val ttl: Long = 7
)
