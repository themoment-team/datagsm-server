package team.themoment.datagsm.common.domain.neis.meal.repository

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository
import team.themoment.datagsm.common.domain.neis.meal.entity.MealRedisEntity
import team.themoment.datagsm.common.domain.neis.meal.entity.constant.MealType
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.kotlinModule
import java.time.LocalDate

@Repository
class MealRedisCustomRepositoryImpl(
    private val redisTemplate: RedisTemplate<String, String>,
) : MealRedisCustomRepository {
    private val jsonMapper =
        JsonMapper
            .builder()
            .addModule(kotlinModule())
            .build()

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

    private fun findEntitiesByIds(ids: Set<String>): List<MealRedisEntity> {
        if (ids.isEmpty()) return emptyList()

        return ids.mapNotNull { id ->
            val key = "meal:$id"
            val hash = redisTemplate.opsForHash<String, String>().entries(key)
            if (hash.isEmpty()) null else convertHashToEntity(hash)
        }
    }

    private fun convertHashToEntity(hash: Map<String, String>): MealRedisEntity =
        MealRedisEntity(
            id = hash["id"] ?: throw IllegalArgumentException("MealRedisEntity ID cannot be null"),
            schoolCode = hash["schoolCode"] ?: throw IllegalArgumentException("MealRedisEntity schoolCode cannot be null"),
            schoolName = hash["schoolName"] ?: throw IllegalArgumentException("MealRedisEntity schoolName cannot be null"),
            officeCode = hash["officeCode"] ?: throw IllegalArgumentException("MealRedisEntity officeCode cannot be null"),
            officeName = hash["officeName"] ?: throw IllegalArgumentException("MealRedisEntity officeName cannot be null"),
            date = LocalDate.parse(hash["date"] ?: throw IllegalArgumentException("MealRedisEntity date cannot be null")),
            type = MealType.valueOf(hash["type"] ?: throw IllegalArgumentException("MealRedisEntity type cannot be null")),
            menu =
                hash["menu"]?.let {
                    jsonMapper.readValue(it, object : TypeReference<List<String>>() {})
                } ?: emptyList(),
            allergyInfo =
                hash["allergyInfo"]?.let {
                    jsonMapper.readValue(it, object : TypeReference<List<String>>() {})
                },
            calories = hash["calories"],
            originInfo = hash["originInfo"],
            nutritionInfo = hash["nutritionInfo"],
            serveCount = hash["serveCount"]?.toIntOrNull(),
        )
}
