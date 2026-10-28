package team.themoment.datagsm.common.domain.neis.schedule.entity

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.index.Indexed
import java.time.LocalDate

// 커스텀 리포지토리가 키스페이스 세트와 인덱스 키를 직접 읽으므로 같은 상수를 공유한다.
const val SCHEDULE_KEYSPACE = "schedule"

@RedisHash(value = SCHEDULE_KEYSPACE, timeToLive = 2592000)
data class ScheduleRedisEntity(
    @Id
    val id: String,
    val schoolCode: String,
    val schoolName: String,
    val officeCode: String,
    val officeName: String,
    @Indexed
    val date: LocalDate,
    val academicYear: String,
    val eventName: String,
    val eventContent: String?,
    val dayCategory: String?,
    val schoolCourseType: String?,
    val dayNightType: String?,
    // Spring Data Redis는 빈 목록을 저장하지 않아 읽을 때 값이 없으므로 기본값으로 복원한다.
    val targetGrades: List<Int> = emptyList(),
)
