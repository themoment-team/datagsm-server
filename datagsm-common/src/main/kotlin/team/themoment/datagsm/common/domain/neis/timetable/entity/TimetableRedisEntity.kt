package team.themoment.datagsm.common.domain.neis.timetable.entity

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.index.Indexed
import java.time.LocalDate

// 커스텀 리포지토리가 키스페이스 세트와 인덱스 키를 직접 읽으므로 같은 상수를 공유한다.
const val TIMETABLE_KEYSPACE = "timetable"

@RedisHash(value = TIMETABLE_KEYSPACE, timeToLive = 604800)
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
