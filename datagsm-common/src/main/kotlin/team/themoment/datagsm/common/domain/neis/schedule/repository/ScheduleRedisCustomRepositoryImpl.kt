package team.themoment.datagsm.common.domain.neis.schedule.repository

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository
import team.themoment.datagsm.common.domain.neis.schedule.entity.ScheduleRedisEntity
import java.time.LocalDate

@Repository
class ScheduleRedisCustomRepositoryImpl(
    private val redisTemplate: RedisTemplate<String, Any>,
) : ScheduleRedisCustomRepository {
    private val objectMapper =
        ObjectMapper().apply {
            registerModule(JavaTimeModule())
            registerKotlinModule()
        }

    override fun findByDateBetween(
        from: LocalDate,
        to: LocalDate,
    ): List<ScheduleRedisEntity> {
        val ids = collectIdsByDateRange(from, to)
        return findEntitiesByIds(ids)
    }

    override fun findByDateGreaterThanEqual(fromDate: LocalDate): List<ScheduleRedisEntity> {
        val toDate = fromDate.plusDays(30)
        val ids = collectIdsByDateRange(fromDate, toDate)
        return findEntitiesByIds(ids)
    }

    override fun findByDateLessThanEqual(toDate: LocalDate): List<ScheduleRedisEntity> {
        val fromDate = toDate.minusDays(30)
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
            dateIds?.forEach { id -> ids.add(id.toString()) }
            currentDate = currentDate.plusDays(1)
        }

        return ids
    }

    private fun findEntitiesByIds(ids: Set<String>): List<ScheduleRedisEntity> {
        if (ids.isEmpty()) return emptyList()

        return ids.mapNotNull { id ->
            val key = "schedule:$id"
            val hash = redisTemplate.opsForHash<String, Any>().entries(key)
            if (hash.isEmpty()) null else convertHashToEntity(hash)
        }
    }

    private fun convertHashToEntity(hash: Map<String, Any>): ScheduleRedisEntity =
        ScheduleRedisEntity(
            id = hash["id"]?.toString() ?: "",
            schoolCode = hash["schoolCode"]?.toString() ?: "",
            schoolName = hash["schoolName"]?.toString() ?: "",
            officeCode = hash["officeCode"]?.toString() ?: "",
            officeName = hash["officeName"]?.toString() ?: "",
            date = LocalDate.parse(hash["date"]?.toString() ?: LocalDate.now().toString()),
            academicYear = hash["academicYear"]?.toString() ?: "",
            eventName = hash["eventName"]?.toString() ?: "",
            eventContent = hash["eventContent"]?.toString(),
            dayCategory = hash["dayCategory"]?.toString(),
            schoolCourseType = hash["schoolCourseType"]?.toString(),
            dayNightType = hash["dayNightType"]?.toString(),
            targetGrades =
                hash["targetGrades"]?.let {
                    when (it) {
                        is String -> objectMapper.readValue(it, object : TypeReference<List<Int>>() {})
                        is List<*> -> it.filterIsInstance<Int>()
                        else -> emptyList()
                    }
                } ?: emptyList(),
        )
}
