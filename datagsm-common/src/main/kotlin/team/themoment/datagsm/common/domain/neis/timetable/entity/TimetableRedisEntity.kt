package team.themoment.datagsm.common.domain.neis.timetable.entity

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.index.Indexed
import java.time.LocalDate

@RedisHash(value = "timetable", timeToLive = 604800)
data class TimetableRedisEntity(
    @Id
    val id: String,
    val schoolCode: String,
    val schoolName: String,
    val officeCode: String,
    val officeName: String,
    @Indexed
    val date: LocalDate,
    val academicYear: String,
    val semester: String?,
    val grade: Int,
    val classNum: Int,
    val period: Int,
    val subject: String?,
)
