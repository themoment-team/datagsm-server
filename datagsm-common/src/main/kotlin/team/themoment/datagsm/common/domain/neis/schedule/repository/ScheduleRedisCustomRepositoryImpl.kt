package team.themoment.datagsm.common.domain.neis.schedule.repository

import org.springframework.data.redis.core.RedisKeyValueTemplate
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository
import team.themoment.datagsm.common.domain.neis.schedule.entity.ScheduleRedisEntity
import java.time.LocalDate

@Repository
class ScheduleRedisCustomRepositoryImpl(
    private val redisTemplate: RedisTemplate<String, String>,
    private val redisKeyValueTemplate: RedisKeyValueTemplate,
) : ScheduleRedisCustomRepository {
    override fun findByDateBetween(
        from: LocalDate,
        to: LocalDate,
    ): List<ScheduleRedisEntity> {
        val ids = collectIdsByDateRange(from, to)
        return findEntitiesByIds(ids)
    }

    override fun findByDateGreaterThanEqual(fromDate: LocalDate): List<ScheduleRedisEntity> {
        val toDate = fromDate.plusYears(1)
        val ids = collectIdsByDateRange(fromDate, toDate)
        return findEntitiesByIds(ids)
    }

    override fun findByDateLessThanEqual(toDate: LocalDate): List<ScheduleRedisEntity> {
        val fromDate = toDate.minusYears(1)
        val ids = collectIdsByDateRange(fromDate, toDate)
        return findEntitiesByIds(ids)
    }

    private fun collectIdsByDateRange(
        from: LocalDate,
        to: LocalDate,
    ): Set<String> {
        val ids = mutableSetOf<String>()
        var currentDate = from

        while (!currentDate.isAfter(to)) {
            val indexKey = "schedule:date:$currentDate"
            val dateIds = redisTemplate.opsForSet().members(indexKey)
            dateIds?.forEach { id -> ids.add(id) }
            currentDate = currentDate.plusDays(1)
        }

        return ids
    }

    // id 목록 조회만 직접 하고, 엔티티는 저장(saveAll)과 같은 Spring Data 변환기로 읽어야 목록 필드가 보존된다.
    private fun findEntitiesByIds(ids: Set<String>): List<ScheduleRedisEntity> =
        ids.mapNotNull { id -> redisKeyValueTemplate.findById(id, ScheduleRedisEntity::class.java).orElse(null) }
}
