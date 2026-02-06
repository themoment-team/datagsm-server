package team.themoment.datagsm.common.domain.neis.schedule.repository

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository
import team.themoment.datagsm.common.domain.neis.schedule.entity.ScheduleRedisEntity
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.kotlinModule
import java.time.LocalDate

@Repository
class ScheduleRedisCustomRepositoryImpl(
    private val redisTemplate: RedisTemplate<String, String>,
) : ScheduleRedisCustomRepository {
    private val jsonMapper =
        JsonMapper
            .builder()
            .addModule(kotlinModule())
            .build()

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
            dateIds?.forEach { id -> ids.add(id) }
            currentDate = currentDate.plusDays(1)
        }

        return ids
    }

    private fun findEntitiesByIds(ids: Set<String>): List<ScheduleRedisEntity> {
        if (ids.isEmpty()) return emptyList()

        return ids.mapNotNull { id ->
            val key = "schedule:$id"
            val hash = redisTemplate.opsForHash<String, String>().entries(key)
            if (hash.isEmpty()) null else convertHashToEntity(hash)
        }
    }

    private fun convertHashToEntity(hash: Map<String, String>): ScheduleRedisEntity =
        ScheduleRedisEntity(
            id = hash["id"] ?: throw IllegalArgumentException("ScheduleRedisEntity ID cannot be null"),
            schoolCode = hash["schoolCode"] ?: throw IllegalArgumentException("ScheduleRedisEntity schoolCode cannot be null"),
            schoolName = hash["schoolName"] ?: throw IllegalArgumentException("ScheduleRedisEntity schoolName cannot be null"),
            officeCode = hash["officeCode"] ?: throw IllegalArgumentException("ScheduleRedisEntity officeCode cannot be null"),
            officeName = hash["officeName"] ?: throw IllegalArgumentException("ScheduleRedisEntity officeName cannot be null"),
            date = LocalDate.parse(hash["date"] ?: throw IllegalArgumentException("ScheduleRedisEntity date cannot be null")),
            academicYear = hash["academicYear"] ?: throw IllegalArgumentException("ScheduleRedisEntity academicYear cannot be null"),
            eventName = hash["eventName"] ?: throw IllegalArgumentException("ScheduleRedisEntity eventName cannot be null"),
            eventContent = hash["eventContent"],
            dayCategory = hash["dayCategory"],
            schoolCourseType = hash["schoolCourseType"],
            dayNightType = hash["dayNightType"],
            targetGrades =
                hash["targetGrades"]?.let {
                    jsonMapper.readValue(it, object : TypeReference<List<Int>>() {})
                } ?: emptyList(),
        )
}
