package team.themoment.datagsm.common.domain.neis.meal.repository

import org.springframework.data.redis.core.RedisKeyValueTemplate
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository
import team.themoment.datagsm.common.domain.neis.meal.entity.MealRedisEntity
import java.time.LocalDate

@Repository
class MealRedisCustomRepositoryImpl(
    private val redisTemplate: RedisTemplate<String, String>,
    private val redisKeyValueTemplate: RedisKeyValueTemplate,
) : MealRedisCustomRepository {
    override fun findByDateBetween(
        from: LocalDate,
        to: LocalDate,
    ): List<MealRedisEntity> {
        val ids = collectIdsByDateRange(from, to)
        return findEntitiesByIds(ids)
    }

    override fun findByDateGreaterThanEqual(fromDate: LocalDate): List<MealRedisEntity> {
        val toDate = fromDate.plusYears(1)
        val ids = collectIdsByDateRange(fromDate, toDate)
        return findEntitiesByIds(ids)
    }

    override fun findByDateLessThanEqual(toDate: LocalDate): List<MealRedisEntity> {
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
            val indexKey = "meal:date:$currentDate"
            val dateIds = redisTemplate.opsForSet().members(indexKey)
            dateIds?.forEach { id -> ids.add(id) }
            currentDate = currentDate.plusDays(1)
        }

        return ids
    }

    // id 목록 조회만 직접 하고, 엔티티는 저장(saveAll)과 같은 Spring Data 변환기로 읽어야 목록 필드가 보존된다.
    private fun findEntitiesByIds(ids: Set<String>): List<MealRedisEntity> =
        ids.mapNotNull { id -> redisKeyValueTemplate.findById(id, MealRedisEntity::class.java).orElse(null) }
}
