package team.themoment.datagsm.common.domain.neis.meal.repository

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository
import team.themoment.datagsm.common.domain.neis.meal.entity.MealRedisEntity
import team.themoment.datagsm.common.domain.neis.meal.entity.constant.MealType
import java.time.LocalDate

@Repository
class MealRedisCustomRepositoryImpl(
    private val redisTemplate: RedisTemplate<String, Any>,
) : MealRedisCustomRepository {
    private val objectMapper =
        ObjectMapper().apply {
            registerModule(JavaTimeModule())
            registerKotlinModule()
        }

    override fun findByDateBetween(
        from: LocalDate,
        to: LocalDate,
    ): List<MealRedisEntity> {
        val ids = collectIdsByDateRange(from, to)
        return findEntitiesByIds(ids)
    }

    override fun findByDateGreaterThanEqual(fromDate: LocalDate): List<MealRedisEntity> {
        val toDate = fromDate.plusDays(30)
        val ids = collectIdsByDateRange(fromDate, toDate)
        return findEntitiesByIds(ids)
    }

    override fun findByDateLessThanEqual(toDate: LocalDate): List<MealRedisEntity> {
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
            val indexKey = "meal:date:$currentDate"
            val dateIds = redisTemplate.opsForSet().members(indexKey)
            dateIds?.forEach { id -> ids.add(id.toString()) }
            currentDate = currentDate.plusDays(1)
        }

        return ids
    }

    private fun findEntitiesByIds(ids: Set<String>): List<MealRedisEntity> {
        if (ids.isEmpty()) return emptyList()

        return ids.mapNotNull { id ->
            val key = "meal:$id"
            val hash = redisTemplate.opsForHash<String, Any>().entries(key)
            if (hash.isEmpty()) null else convertHashToEntity(hash)
        }
    }

    private fun convertHashToEntity(hash: Map<String, Any>): MealRedisEntity =
        MealRedisEntity(
            id = hash["id"]?.toString() ?: "",
            schoolCode = hash["schoolCode"]?.toString() ?: "",
            schoolName = hash["schoolName"]?.toString() ?: "",
            officeCode = hash["officeCode"]?.toString() ?: "",
            officeName = hash["officeName"]?.toString() ?: "",
            date = LocalDate.parse(hash["date"]?.toString() ?: LocalDate.now().toString()),
            type = MealType.valueOf(hash["type"]?.toString() ?: "LUNCH"),
            menu =
                hash["menu"]?.let {
                    when (it) {
                        is String -> objectMapper.readValue(it, object : TypeReference<List<String>>() {})
                        is List<*> -> it.filterIsInstance<String>()
                        else -> emptyList()
                    }
                } ?: emptyList(),
            allergyInfo =
                hash["allergyInfo"]?.let {
                    when (it) {
                        is String -> objectMapper.readValue(it, object : TypeReference<List<String>>() {})
                        is List<*> -> it.filterIsInstance<String>()
                        else -> null
                    }
                },
            calories = hash["calories"]?.toString(),
            originInfo = hash["originInfo"]?.toString(),
            nutritionInfo = hash["nutritionInfo"]?.toString(),
            serveCount = hash["serveCount"]?.toString()?.toIntOrNull(),
        )
}
